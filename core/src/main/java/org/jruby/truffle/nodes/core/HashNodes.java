/*
 * Copyright (c) 2013, 2014 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.nodes.core;

import java.util.*;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.source.*;
import com.oracle.truffle.api.dsl.*;
import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.utilities.BranchProfile;
import org.jruby.runtime.Visibility;
import org.jruby.truffle.nodes.RubyRootNode;
import org.jruby.truffle.nodes.dispatch.DispatchHeadNode;
import org.jruby.truffle.nodes.dispatch.PredicateDispatchHeadNode;
import org.jruby.truffle.nodes.yield.YieldDispatchHeadNode;
import org.jruby.truffle.runtime.*;
import org.jruby.truffle.runtime.core.*;
import org.jruby.truffle.runtime.core.RubyArray;
import org.jruby.truffle.runtime.core.RubyHash;

@CoreClass(name = "Hash")
public abstract class HashNodes {

    @CoreMethod(names = "==", required = 1)
    public abstract static class EqualNode extends HashCoreMethodNode {

        @Child protected PredicateDispatchHeadNode equalNode;

        public EqualNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
            equalNode = new PredicateDispatchHeadNode(context);
        }

        public EqualNode(EqualNode prev) {
            super(prev);
            equalNode = prev.equalNode;
        }

        @Specialization(guards = {"isNull", "isOtherNull"})
        public boolean equalNull(RubyHash a, RubyHash b) {
            return true;
        }

        @Specialization(guards = {"isObjectArray", "isOtherObjectArray"})
        public boolean equalObjectArray(VirtualFrame frame, RubyHash a, RubyHash b) {
            notDesignedForCompilation();

            if (a == b) {
                return true;
            }

            final Object[] aStore = (Object[]) a.getStore();
            final int aSize = a.getStoreSize();

            final Object[] bStore = (Object[]) b.getStore();
            final int bSize = b.getStoreSize();

            if (aSize != bSize) {
                return false;
            }

            // TODO(CS): this is badly broken - I think it assumes the hash is ordered?

            for (int n = 0; n < aSize * 2; n++) {
                if (!equalNode.call(frame, aStore[n], "==", null, bStore[n])) {
                    return false;
                }
            }

            return true;
        }

        @Specialization(guards = {"isObjectLinkedHashMap", "isOtherObjectLinkedHashMap"})
        public boolean equalObjectLinkedHashMap(RubyHash a, RubyHash b) {
            notDesignedForCompilation();
            throw new UnsupportedOperationException();
        }

        @Specialization(guards = {"isObjectLinkedHashMap", "isOtherObjectArray"})
        public boolean equalObjectLinkedHashMapArray(VirtualFrame frame, RubyHash a, RubyHash b) {
            notDesignedForCompilation();

            final LinkedHashMap<Object, Object> aStore = (LinkedHashMap<Object, Object>) a.getStore();
            final int aSize = a.getStoreSize();

            final Object[] bStore = (Object[]) b.getStore();
            final int bSize = b.getStoreSize();

            if (aSize != bSize) {
                return false;
            }

            // TODO(CS): this is crap - doesn't check for duplicates or anything - badly need to improve the Hash stuff

            for (Map.Entry<Object, Object> entry : aStore.entrySet()) {
                boolean match = false;

                for (int n = 0; n < aSize * 2; n += 1) {
                    if (equalNode.call(frame, entry.getKey(), "==", null, bStore[n]) && equalNode.call(frame, entry.getValue(), "==", null, bStore[n + 1])) {
                        match = true;
                    }
                }

                if (!match) {
                    return false;
                }
            }

            return true;
        }

        @Specialization(guards = "!isHash(arguments[1])")
        public boolean equal(RubyHash a, Object b) {
            notDesignedForCompilation();
            return false;
        }

        protected boolean isHash(Object object) {
            return object instanceof RubyHash;
        }
    }

    @CoreMethod(names = "[]", onSingleton = true, argumentsAsArray = true)
    public abstract static class ConstructNode extends HashCoreMethodNode {

        private final BranchProfile singleObject = new BranchProfile();
        private final BranchProfile singleArray = new BranchProfile();
        private final BranchProfile objectArray = new BranchProfile();
        private final BranchProfile smallObjectArray = new BranchProfile();
        private final BranchProfile largeObjectArray = new BranchProfile();
        private final BranchProfile otherArray = new BranchProfile();
        private final BranchProfile singleOther = new BranchProfile();
        private final BranchProfile keyValues = new BranchProfile();

        public ConstructNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public ConstructNode(ConstructNode prev) {
            super(prev);
        }

        @ExplodeLoop
        @Specialization
        public RubyHash construct(Object[] args) {
            if (args.length == 1) {
                singleObject.enter();

                final Object arg = args[0];

                if (arg instanceof RubyArray) {
                    singleArray.enter();

                    final RubyArray array = (RubyArray) arg;

                    if (array.getStore() instanceof Object[]) {
                        objectArray.enter();

                        final Object[] store = (Object[]) array.getStore();

                        // TODO(CS): zero length arrays might be a good specialisation

                        if (store.length <= RubyHash.HASHES_SMALL) {
                            smallObjectArray.enter();

                            final int size = store.length;
                            final Object[] newStore = new Object[RubyHash.HASHES_SMALL * 2];

                            for (int n = 0; n < RubyHash.HASHES_SMALL; n++) {
                                if (n < size) {
                                    final Object pair = store[n];

                                    if (!(pair instanceof RubyArray)) {
                                        CompilerDirectives.transferToInterpreter();
                                        throw new UnsupportedOperationException();
                                    }

                                    final RubyArray pairArray = (RubyArray) pair;

                                    if (!(pairArray.getStore() instanceof Object[])) {
                                        CompilerDirectives.transferToInterpreter();
                                        throw new UnsupportedOperationException();
                                    }

                                    final Object[] pairStore = (Object[]) pairArray.getStore();

                                    newStore[n * 2] = pairStore[0];
                                    newStore[n * 2 + 1] = pairStore[1];
                                }
                            }

                            return new RubyHash(getContext().getCoreLibrary().getHashClass(), null, null, newStore, size);
                        } else {
                            largeObjectArray.enter();
                            throw new UnsupportedOperationException();
                        }
                    } else {
                        otherArray.enter();
                        throw new UnsupportedOperationException();
                    }
                } else {
                    singleOther.enter();
                    throw new UnsupportedOperationException();
                }
            } else {
                keyValues.enter();
                // Slow because we don't want the PE to see the hash map at all
                return constructObjectLinkedMapMap(args);
            }
        }

        @CompilerDirectives.SlowPath
        public RubyHash constructObjectLinkedMapMap(Object[] args) {
            final LinkedHashMap<Object, Object> store = new LinkedHashMap<>();

            for (int n = 0; n < args.length; n += 2) {
                store.put(args[n], args[n + 1]);
            }

            return new RubyHash(getContext().getCoreLibrary().getHashClass(), null, null, store, 0);
        }

    }

    @CoreMethod(names = "[]", required = 1)
    public abstract static class GetIndexNode extends HashCoreMethodNode {

        @Child protected PredicateDispatchHeadNode eqlNode;
        @Child protected YieldDispatchHeadNode yield;

        private final BranchProfile notInHashProfile = new BranchProfile();
        private final BranchProfile useDefaultProfile = new BranchProfile();

        public GetIndexNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
            eqlNode = new PredicateDispatchHeadNode(context);
            yield = new YieldDispatchHeadNode(context);
        }

        public GetIndexNode(GetIndexNode prev) {
            super(prev);
            eqlNode = prev.eqlNode;
            yield = prev.yield;
        }

        @Specialization(guards = "isNull")
        public Object getNull(VirtualFrame frame, RubyHash hash, Object key) {
            notDesignedForCompilation();

            if (hash.getDefaultBlock() != null) {
                return yield.dispatch(frame, hash.getDefaultBlock(), hash, key);
            } else if (hash.getDefaultValue() != null) {
                return hash.getDefaultValue();
            } else {
                return getContext().getCoreLibrary().getNilObject();
            }
        }

        @ExplodeLoop
        @Specialization(guards = "isObjectArray")
        public Object getObjectArray(VirtualFrame frame, RubyHash hash, Object key) {
            final Object[] store = (Object[]) hash.getStore();
            final int size = hash.getStoreSize();

            for (int n = 0; n < RubyHash.HASHES_SMALL; n++) {
                if (n < size && eqlNode.call(frame, store[n * 2], "eql?", null, key)) {
                    return store[n * 2 + 1];
                }
            }

            notInHashProfile.enter();

            if (hash.getDefaultBlock() != null) {
                useDefaultProfile.enter();
                return yield.dispatch(frame, hash.getDefaultBlock(), hash, key);
            }

            if (hash.getDefaultValue() != null) {
                return hash.getDefaultValue();
            }

            return getContext().getCoreLibrary().getNilObject();

        }

        @Specialization(guards = "isObjectLinkedHashMap")
        public Object getObjectLinkedHashMap(VirtualFrame frame, RubyHash hash, Object key) {
            notDesignedForCompilation();

            final LinkedHashMap<Object, Object> store = (LinkedHashMap<Object, Object>) hash.getStore();

            // TODO(CS): not correct - using Java's Object#equals

            final Object value = store.get(key);

            if (value == null) {
                if (hash.getDefaultBlock() != null) {
                    return yield.dispatch(frame, hash.getDefaultBlock(), hash, key);
                } else if (hash.getDefaultValue() != null) {
                    return hash.getDefaultValue();
                } else {
                    return getContext().getCoreLibrary().getNilObject();
                }
            }

            return value;
        }

    }

    @CoreMethod(names = "[]=", required = 2)
    public abstract static class SetIndexNode extends HashCoreMethodNode {

        @Child protected PredicateDispatchHeadNode eqlNode;

        private final BranchProfile considerExtendProfile = new BranchProfile();
        private final BranchProfile extendProfile = new BranchProfile();
        private final BranchProfile transitionToLinkedHashMapProfile = new BranchProfile();

        public SetIndexNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
            eqlNode = new PredicateDispatchHeadNode(context);
        }

        public SetIndexNode(SetIndexNode prev) {
            super(prev);
            eqlNode = prev.eqlNode;
        }

        @Specialization(guards = "isNull")
        public Object setNull(RubyHash hash, Object key, Object value) {
            hash.checkFrozen(this);
            final Object[] store = new Object[RubyHash.HASHES_SMALL * 2];
            store[0] = key;
            store[1] = value;
            hash.setStore(store, 1);
            return value;
        }

        @ExplodeLoop
        @Specialization(guards = "isObjectArray")
        public Object setObjectArray(VirtualFrame frame, RubyHash hash, Object key, Object value) {
            hash.checkFrozen(this);

            final Object[] store = (Object[]) hash.getStore();
            final int size = hash.getStoreSize();

            for (int n = 0; n < RubyHash.HASHES_SMALL; n++) {
                if (n < size && eqlNode.call(frame, store[n * 2], "eql?", null, key)) {
                    store[n * 2 + 1] = value;
                    return value;
                }
            }

            considerExtendProfile.enter();

            final int newSize = size + 1;

            if (newSize <= RubyHash.HASHES_SMALL) {
                extendProfile.enter();
                store[size * 2] = key;
                store[size * 2 + 1] = value;
                hash.setStoreSize(newSize);
                return value;
            }


            transitionToLinkedHashMapProfile.enter();

            transitionToLinkedHashMap(hash, store, key, value);
            return value;
        }

        @CompilerDirectives.SlowPath
        private void transitionToLinkedHashMap(RubyHash hash, Object[] oldStore, Object key, Object value) {
            final LinkedHashMap<Object, Object> newStore = new LinkedHashMap<>();

            for (int n = 0; n < oldStore.length; n += 2) {
                newStore.put(oldStore[n], oldStore[n + 1]);
            }

            newStore.put(key, value);
            hash.setStore(newStore, 0);
        }

        @Specialization(guards = "isObjectLinkedHashMap")
        public Object setObjectLinkedHashMap(RubyHash hash, Object key, Object value) {
            notDesignedForCompilation();

            hash.checkFrozen(this);

            // TODO(CS): not correct - using Java's Object#equals

            final LinkedHashMap<Object, Object> store = (LinkedHashMap<Object, Object>) hash.getStore();
            store.put(key, value);
            return value;
        }

    }

    @CoreMethod(names = "delete", required = 1)
    public abstract static class DeleteNode extends HashCoreMethodNode {

        public DeleteNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public DeleteNode(DeleteNode prev) {
            super(prev);
        }

        @Specialization(guards = "isNull")
        public RubyNilClass deleteNull(RubyHash hash, Object key) {
            hash.checkFrozen(this);
            return getContext().getCoreLibrary().getNilObject();
        }

        @Specialization(guards = "isObjectArray")
        public Object deleteObjectArray(RubyHash hash, Object key) {
            notDesignedForCompilation();

            // TODO(CS): seriously not correct

            hash.checkFrozen(this);

            final Object[] oldStore = (Object[]) hash.getStore();

            final LinkedHashMap<Object, Object> newStore = new LinkedHashMap<>();
            hash.setStore(newStore, 0);

            for (int n = 0; n < hash.getStoreSize(); n++) {
                newStore.put(oldStore[n * 2], oldStore[n * 2 + 1]);
            }

            // TODO(CS): seriously not correct - using Java's Object#equals

            final Object removed = newStore.remove(key);

            if (removed == null) {
                return getContext().getCoreLibrary().getNilObject();
            } else {
                return removed;
            }
        }

        @Specialization(guards = "isObjectLinkedHashMap")
        public Object delete(RubyHash hash, Object key) {
            notDesignedForCompilation();

            hash.checkFrozen(this);

            final LinkedHashMap<Object, Object> store = (LinkedHashMap<Object, Object>) hash.getStore();

            // TODO(CS): seriously not correct - using Java's Object#equals

            final Object removed = store.remove(key);

            if (removed == null) {
                return getContext().getCoreLibrary().getNilObject();
            } else {
                return removed;
            }
        }

    }

    @CoreMethod(names = "each", needsBlock = true)
    @ImportGuards(HashGuards.class)
    public abstract static class EachNode extends YieldingCoreMethodNode {

        public EachNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public EachNode(EachNode prev) {
            super(prev);
        }

        @Specialization(guards = "isNull")
        public RubyHash eachNull(RubyHash hash, RubyProc block) {
            notDesignedForCompilation();

            return hash;
        }

        @ExplodeLoop
        @Specialization(guards = "isObjectArray")
        public RubyHash eachObjectArray(VirtualFrame frame, RubyHash hash, RubyProc block) {
            notDesignedForCompilation();

            final Object[] store = (Object[]) hash.getStore();
            final int size = hash.getStoreSize();

            int count = 0;

            try {
                for (int n = 0; n < RubyHash.HASHES_SMALL; n++) {
                    if (CompilerDirectives.inInterpreter()) {
                        count++;
                    }

                    if (n < size) {
                        yield(frame, block, RubyArray.fromObjects(getContext().getCoreLibrary().getArrayClass(), store[n * 2], store[n * 2 + 1]));
                    }
                }
            } finally {
                if (CompilerDirectives.inInterpreter()) {
                    ((RubyRootNode) getRootNode()).reportLoopCountThroughBlocks(count);
                }
            }

            return hash;
        }

        @Specialization(guards = "isObjectLinkedHashMap")
        public RubyHash eachObjectLinkedHashMap(VirtualFrame frame, RubyHash hash, RubyProc block) {
            notDesignedForCompilation();

            final LinkedHashMap<Object, Object> store = (LinkedHashMap<Object, Object>) hash.getStore();

            int count = 0;

            try {
                for (Map.Entry<Object, Object> entry : store.entrySet()) {
                    if (CompilerDirectives.inInterpreter()) {
                        count++;
                    }

                    yield(frame, block, RubyArray.fromObjects(getContext().getCoreLibrary().getArrayClass(), entry.getKey(), entry.getValue()));
                }
            } finally {
                if (CompilerDirectives.inInterpreter()) {
                    ((RubyRootNode) getRootNode()).reportLoopCountThroughBlocks(count);
                }
            }

            return hash;
        }

    }

    @CoreMethod(names = "empty?")
    public abstract static class EmptyNode extends HashCoreMethodNode {

        public EmptyNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public EmptyNode(EmptyNode prev) {
            super(prev);
        }

        @Specialization(guards = "isNull")
        public boolean emptyNull(RubyHash hash) {
            return true;
        }

        @Specialization(guards = "isObjectArray")
        public boolean emptyObjectArray(RubyHash hash) {
            return hash.getStoreSize() == 0;
        }

        @Specialization(guards = "isObjectLinkedHashMap")
        public boolean emptyObjectLinkedHashMap(RubyHash hash) {
            notDesignedForCompilation();

            final LinkedHashMap<Object, Object> store = (LinkedHashMap<Object, Object>) hash.getStore();
            return store.isEmpty();
        }

    }

    @CoreMethod(names = "initialize", needsBlock = true, optional = 1)
    public abstract static class InitializeNode extends HashCoreMethodNode {

        public InitializeNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public InitializeNode(InitializeNode prev) {
            super(prev);
        }

        @Specialization
        public RubyNilClass initialize(RubyHash hash, UndefinedPlaceholder defaultValue, UndefinedPlaceholder block) {
            notDesignedForCompilation();
            hash.setStore(null, 0);
            hash.setDefaultBlock(null);
            return getContext().getCoreLibrary().getNilObject();
        }

        @Specialization
        public RubyNilClass initialize(RubyHash hash, UndefinedPlaceholder defaultValue, RubyProc block) {
            notDesignedForCompilation();
            hash.setStore(null, 0);
            hash.setDefaultBlock(block);
            return getContext().getCoreLibrary().getNilObject();
        }

        @Specialization
        public RubyNilClass initialize(RubyHash hash, Object defaultValue, UndefinedPlaceholder block) {
            notDesignedForCompilation();
            hash.setDefaultValue(defaultValue);
            return getContext().getCoreLibrary().getNilObject();
        }

    }

    @CoreMethod(names = "initialize_copy", visibility = Visibility.PRIVATE, required = 1)
    public abstract static class InitializeCopyNode extends HashCoreMethodNode {

        public InitializeCopyNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public InitializeCopyNode(InitializeCopyNode prev) {
            super(prev);
        }

        @Specialization(guards = "isOtherNull")
        public RubyHash dupNull(RubyHash self, RubyHash from) {
            notDesignedForCompilation();

            if (self == from) {
                return self;
            }

            self.setDefaultBlock(from.getDefaultBlock());
            self.setDefaultValue(from.getDefaultValue());
            self.setStore(null, 0);

            return self;
        }

        @Specialization(guards = "isOtherObjectArray")
        public RubyHash dupObjectArray(RubyHash self, RubyHash from) {
            notDesignedForCompilation();

            if (self == from) {
                return self;
            }

            final Object[] store = (Object[]) from.getStore();
            self.setStore(Arrays.copyOf(store, RubyHash.HASHES_SMALL * 2), store.length);
            self.setDefaultBlock(from.getDefaultBlock());
            self.setDefaultValue(from.getDefaultValue());

            return self;
        }

        @Specialization(guards = "isOtherObjectLinkedHashMap")
        public RubyHash dupObjectLinkedHashMap(RubyHash self, RubyHash from) {
            notDesignedForCompilation();

            if (self == from) {
                return self;
            }

            final LinkedHashMap<Object, Object> store = (LinkedHashMap<Object, Object>) from.getStore();
            self.setStore(new LinkedHashMap<>(store), store.size());
            self.setDefaultBlock(from.getDefaultBlock());
            self.setDefaultValue(from.getDefaultValue());

            return self;
        }

    }

    @CoreMethod(names = {"inspect", "to_s"})
    public abstract static class InspectNode extends HashCoreMethodNode {

        @Child protected DispatchHeadNode inspect;

        public InspectNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
            inspect = new DispatchHeadNode(context);
        }

        public InspectNode(InspectNode prev) {
            super(prev);
            inspect = prev.inspect;
        }

        @Specialization(guards = "isNull")
        public RubyString inspectNull(RubyHash hash) {
            notDesignedForCompilation();

            return getContext().makeString("{}");
        }

        @Specialization(guards = "isObjectArray")
        public RubyString inspectObjectArray(VirtualFrame frame, RubyHash hash) {
            notDesignedForCompilation();

            final Object[] store = (Object[]) hash.getStore();

            final StringBuilder builder = new StringBuilder();

            builder.append("{");

            for (int n = 0; n < hash.getStoreSize(); n++) {
                if (n > 0) {
                    builder.append(", ");
                }

                // TODO(CS): to string

                builder.append(inspect.call(frame, store[n * 2], "inspect", null));
                builder.append("=>");
                builder.append(inspect.call(frame, store[n * 2 + 1], "inspect", null));
            }

            builder.append("}");

            return getContext().makeString(builder.toString());
        }

        @Specialization(guards = "isObjectLinkedHashMap")
        public RubyString inspectObjectLinkedHashMap(VirtualFrame frame, RubyHash hash) {
            notDesignedForCompilation();

            final LinkedHashMap<Object, Object> store = (LinkedHashMap<Object, Object>) hash.getStore();

            final StringBuilder builder = new StringBuilder();

            builder.append("{");

            boolean first = true;

            for (Map.Entry<Object, Object> entry : store.entrySet()) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }

                builder.append(inspect.call(frame, entry.getKey(), "inspect", null));
                builder.append("=>");
                builder.append(inspect.call(frame, entry.getValue(), "inspect", null));
            }

            builder.append("}");

            return getContext().makeString(builder.toString());
        }

    }

    @CoreMethod(names = { "has_key?", "key?" }, required = 1)
    public abstract static class KeyNode extends HashCoreMethodNode {

        @Child protected PredicateDispatchHeadNode eqlNode;

        public KeyNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
            eqlNode = new PredicateDispatchHeadNode(context);
        }

        public KeyNode(KeyNode prev) {
            super(prev);
            eqlNode = prev.eqlNode;
        }

        @Specialization(guards = "isNull")
        public boolean keyNull(RubyHash hash, Object key) {
            return false;
        }

        @Specialization(guards = "isObjectArray")
        public boolean keyObjectArray(VirtualFrame frame, RubyHash hash, Object key) {
            notDesignedForCompilation();

            final int size = hash.getStoreSize();
            final Object[] store = (Object[]) hash.getStore();

            for (int n = 0; n < store.length; n += 2) {
                if (n < size && eqlNode.call(frame, store[n], "eql?", null, key)) {
                    return true;
                }
            }

            return false;
        }

        @Specialization(guards = "isObjectLinkedHashMap")
        public boolean keyObjectLinkedHashMap(RubyHash hash, Object key) {
            notDesignedForCompilation();

            final LinkedHashMap<Object, Object> store = (LinkedHashMap<Object, Object>) hash.getStore();

            // TODO(CS): seriously not correct - using Java's Object#equals

            return store.containsKey(key);
        }

    }

    @CoreMethod(names = "keys")
    public abstract static class KeysNode extends HashCoreMethodNode {

        public KeysNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public KeysNode(KeysNode prev) {
            super(prev);
        }

        @Specialization(guards = "isNull")
        public RubyArray keysNull(RubyHash hash) {
            return new RubyArray(getContext().getCoreLibrary().getArrayClass(), null, 0);
        }

        @Specialization(guards = "isObjectArray")
        public RubyArray keysObjectArray(RubyHash hash) {
            notDesignedForCompilation();

            final Object[] store = (Object[]) hash.getStore();

            final Object[] keys = new Object[hash.getStoreSize()];

            for (int n = 0; n < keys.length; n++) {
                keys[n] = store[n * 2];
            }

            return new RubyArray(getContext().getCoreLibrary().getArrayClass(), keys, keys.length);
        }

        @Specialization(guards = "isObjectLinkedHashMap")
        public RubyArray keysObjectLinkedHashMap(RubyHash hash) {
            notDesignedForCompilation();

            final LinkedHashMap<Object, Object> store = (LinkedHashMap<Object, Object>) hash.getStore();

            final Object[] keys = new Object[store.size()];

            int n = 0;

            for (Object key : store.keySet()) {
                keys[n] = key;
                n++;
            }

            return new RubyArray(getContext().getCoreLibrary().getArrayClass(), keys, keys.length);
        }

    }

    @CoreMethod(names = {"map", "collect"}, needsBlock = true)
    @ImportGuards(HashGuards.class)
    public abstract static class MapNode extends YieldingCoreMethodNode {

        public MapNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public MapNode(MapNode prev) {
            super(prev);
        }

        @ExplodeLoop
        @Specialization(guards = "isObjectArray")
        public RubyArray mapObjectArray(VirtualFrame frame, RubyHash hash, RubyProc block) {
            final Object[] store = (Object[]) hash.getStore();
            final int size = hash.getStoreSize();

            final int resultSize = store.length / 2;
            final Object[] result = new Object[resultSize];

            int count = 0;

            try {
                for (int n = 0; n < RubyHash.HASHES_SMALL; n++) {
                    if (n < size) {
                        final Object key = store[n * 2];
                        final Object value = store[n * 2 + 1];
                        result[n] = yield(frame, block, key, value);

                        if (CompilerDirectives.inInterpreter()) {
                            count++;
                        }
                    }
                }
            } finally {
                if (CompilerDirectives.inInterpreter()) {
                    ((RubyRootNode) getRootNode()).reportLoopCountThroughBlocks(count);
                }
            }

            return new RubyArray(getContext().getCoreLibrary().getArrayClass(), result, resultSize);
        }

        @Specialization(guards = "isObjectLinkedHashMap")
        public RubyArray mapObjectLinkedHashMap(VirtualFrame frame, RubyHash hash, RubyProc block) {
            notDesignedForCompilation();

            final LinkedHashMap<Object, Object> store = (LinkedHashMap<Object, Object>) hash.getStore();

            final RubyArray result = new RubyArray(getContext().getCoreLibrary().getArrayClass());

            int count = 0;

            try {
                for (Map.Entry<Object, Object> entry : store.entrySet()) {
                    if (CompilerDirectives.inInterpreter()) {
                        count++;
                    }

                    result.slowPush(yield(frame, block, entry.getKey(), entry.getValue()));
                }
            } finally {
                if (CompilerDirectives.inInterpreter()) {
                    ((RubyRootNode) getRootNode()).reportLoopCountThroughBlocks(count);
                }
            }

            return result;
        }

    }

    @CoreMethod(names = "merge", required = 1)
    public abstract static class MergeNode extends HashCoreMethodNode {

        @Child protected PredicateDispatchHeadNode eqlNode;

        private final BranchProfile nothingFromFirstProfile = new BranchProfile();
        private final BranchProfile considerNothingFromSecondProfile = new BranchProfile();
        private final BranchProfile nothingFromSecondProfile = new BranchProfile();
        private final BranchProfile considerResultIsSmallProfile = new BranchProfile();
        private final BranchProfile resultIsSmallProfile = new BranchProfile();

        private final int smallHashSize = RubyHash.HASHES_SMALL;

        public MergeNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
            eqlNode = new PredicateDispatchHeadNode(context);
        }

        public MergeNode(MergeNode prev) {
            super(prev);
            eqlNode = prev.eqlNode;
        }

        @Specialization(guards = {"isObjectArray", "isOtherNull"})
        public RubyHash mergeObjectArrayNull(RubyHash hash, RubyHash other) {
            final Object[] store = (Object[]) hash.getStore();
            final Object[] copy = Arrays.copyOf(store, RubyHash.HASHES_SMALL * 2);

            return new RubyHash(getContext().getCoreLibrary().getHashClass(), hash.getDefaultBlock(), hash.getDefaultValue(), copy, hash.getStoreSize());
        }

        @ExplodeLoop
        @Specialization(guards = {"isObjectArray", "isOtherObjectArray"})
        public RubyHash mergeObjectArrayObjectArray(VirtualFrame frame, RubyHash hash, RubyHash other) {
            // TODO(CS): what happens with the default block here? Which side does it get merged from?

            final Object[] storeA = (Object[]) hash.getStore();
            final int storeASize = hash.getStoreSize();

            final Object[] storeB = (Object[]) other.getStore();
            final int storeBSize = hash.getStoreSize();

            final boolean[] mergeFromA = new boolean[storeASize];
            int mergeFromACount = 0;

            for (int a = 0; a < RubyHash.HASHES_SMALL; a++) {
                if (a < storeASize) {
                    boolean merge = true;

                    for (int b = 0; b < RubyHash.HASHES_SMALL; b++) {
                        if (b < storeBSize) {
                            if (eqlNode.call(frame, storeA[a * 2], "eql?", null, storeB[b * 2])) {
                                merge = false;
                                break;
                            }
                        }
                    }

                    if (merge) {
                        mergeFromACount++;
                    }

                    mergeFromA[a] = merge;
                }
            }

            if (mergeFromACount == 0) {
                nothingFromFirstProfile.enter();
                return new RubyHash(getContext().getCoreLibrary().getHashClass(), hash.getDefaultBlock(), hash.getDefaultValue(), Arrays.copyOf(storeB, RubyHash.HASHES_SMALL * 2), storeBSize);
            }

            considerNothingFromSecondProfile.enter();

            if (mergeFromACount == storeB.length) {
                nothingFromSecondProfile.enter();
                return new RubyHash(getContext().getCoreLibrary().getHashClass(), hash.getDefaultBlock(), hash.getDefaultValue(), Arrays.copyOf(storeB, RubyHash.HASHES_SMALL * 2), storeBSize);
            }

            considerResultIsSmallProfile.enter();

            final int mergedSize = storeBSize + mergeFromACount;

            if (storeBSize + mergeFromACount <= smallHashSize) {
                resultIsSmallProfile.enter();

                final Object[] merged = new Object[RubyHash.HASHES_SMALL * 2];

                int index = 0;

                for (int n = 0; n < storeASize; n++) {
                    if (mergeFromA[n]) {
                        merged[index] = storeA[n * 2];
                        merged[index + 1] = storeA[n * 2 + 1];
                        index += 2;
                    }
                }

                for (int n = 0; n < storeBSize; n++) {
                    merged[index] = storeB[n * 2];
                    merged[index + 1] = storeB[n * 2 + 1];
                    index += 2;
                }

                return new RubyHash(getContext().getCoreLibrary().getHashClass(), hash.getDefaultBlock(), hash.getDefaultValue(), merged, mergedSize);
            }

            CompilerDirectives.transferToInterpreter();
            throw new UnsupportedOperationException();
        }

    }

    @CoreMethod(names = "default", optional = 1)
    public abstract static class DefaultNode extends HashCoreMethodNode {

        public DefaultNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public DefaultNode(DefaultNode prev) {
            super(prev);
        }

        @Specialization
        public Object defaultElement(VirtualFrame frame, RubyHash hash, UndefinedPlaceholder undefined) {
            Object ret = hash.getDefaultValue();

            // TODO (nirvdrum Dec. 1, 2014): This needs to evaluate the defaultProc if it exists before it tries defaultValue.
            if (ret != null) {
                return ret;
            } else {
                return getContext().getCoreLibrary().getNilObject();
            }
        }

        @Specialization
        public Object defaultElement(VirtualFrame frame, RubyHash hash, Object key) {
            Object ret = hash.getDefaultValue();

            // TODO (nirvdrum Dec. 1, 2014): This really needs to do something with the key.  Dummy stub for now.
            if (ret != null) {
                return ret;
            } else {
                return getContext().getCoreLibrary().getNilObject();
            }
        }
    }

    @CoreMethod(names = "size")
    public abstract static class SizeNode extends HashCoreMethodNode {

        public SizeNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public SizeNode(SizeNode prev) {
            super(prev);
        }

        @Specialization(guards = "isNull")
        public int sizeNull(RubyHash hash) {
            return 0;
        }

        @Specialization(guards = "isObjectArray")
        public int sizeObjectArray(RubyHash hash) {
            return hash.getStoreSize();
        }

        @Specialization(guards = "isObjectLinkedHashMap")
        public int sizeObjectLinkedHashMap(RubyHash hash) {
            notDesignedForCompilation();
            return ((LinkedHashMap<Object, Object>) hash.getStore()).size();
        }

    }

    @CoreMethod(names = "values")
    public abstract static class ValuesNode extends HashCoreMethodNode {

        public ValuesNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public ValuesNode(ValuesNode prev) {
            super(prev);
        }

        @Specialization(guards = "isNull")
        public RubyArray valuesNull(RubyHash hash) {
            return new RubyArray(getContext().getCoreLibrary().getArrayClass(), null, 0);
        }

        @Specialization(guards = "isObjectArray")
        public RubyArray valuesObjectArray(RubyHash hash) {
            final Object[] store = (Object[]) hash.getStore();

            final Object[] values = new Object[hash.getStoreSize()];

            for (int n = 0; n < values.length; n++) {
                values[n] = store[n * 2 + 1];
            }

            return new RubyArray(getContext().getCoreLibrary().getArrayClass(), values, values.length);
        }

        @Specialization(guards = "isObjectLinkedHashMap")
        public RubyArray valuesObjectLinkedHashMap(RubyHash hash) {
            notDesignedForCompilation();

            final LinkedHashMap<Object, Object> store = (LinkedHashMap<Object, Object>) hash.getStore();

            final Object[] values = new Object[store.size()];

            int n = 0;

            for (Object value : store.values()) {
                values[n] = value;
                n++;
            }

            return new RubyArray(getContext().getCoreLibrary().getArrayClass(), values, values.length);
        }

    }

    @CoreMethod(names = "to_a")
    public abstract static class ToArrayNode extends HashCoreMethodNode {

        public ToArrayNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public ToArrayNode(ToArrayNode prev) {
            super(prev);
        }

        @Specialization(guards = "isNull")
        public RubyArray toArrayNull(RubyHash hash) {
            notDesignedForCompilation();

            return new RubyArray(getContext().getCoreLibrary().getArrayClass(), null, 0);
        }

        @Specialization(guards = "isObjectArray")
        public RubyArray toArrayObjectArray(RubyHash hash) {
            notDesignedForCompilation();

            final Object[] store = (Object[]) hash.getStore();
            final int size = hash.getStoreSize();
            final Object[] pairs = new Object[size];

            for (int n = 0; n < size; n++) {
                pairs[n] = RubyArray.fromObjects(getContext().getCoreLibrary().getArrayClass(), store[n * 2], store[n * 2 + 1]);
            }

            return new RubyArray(getContext().getCoreLibrary().getArrayClass(), pairs, size);
        }

        @Specialization(guards = "isObjectLinkedHashMap")
        public RubyArray toArrayLinkedHashMap(RubyHash hash) {
            notDesignedForCompilation();

            final LinkedHashMap<Object, Object> store = (LinkedHashMap<Object, Object>) hash.getStore();
            final int size = hash.getStoreSize();
            final Object[] pairs = new Object[size];
            int n = 0;

            for (Map.Entry<Object, Object> pair : store.entrySet()) {
                pairs[n] = RubyArray.fromObjects(getContext().getCoreLibrary().getArrayClass(), pair.getKey(), pair.getValue());
                n += 1;
            }

            return new RubyArray(getContext().getCoreLibrary().getArrayClass(), pairs, size);
        }

    }

}
