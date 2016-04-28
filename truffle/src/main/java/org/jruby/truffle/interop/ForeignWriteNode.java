/*
 * Copyright (c) 2013, 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.interop;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.AcceptMessage;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.Node.Child;
import com.oracle.truffle.api.object.DynamicObject;
import org.jruby.truffle.RubyContext;
import org.jruby.truffle.RubyLanguage;
import org.jruby.truffle.core.rope.Rope;
import org.jruby.truffle.core.rope.RopeOperations;
import org.jruby.truffle.core.string.StringCachingGuards;
import org.jruby.truffle.language.RubyNode;
import org.jruby.truffle.language.RubyObjectType;
import org.jruby.truffle.language.dispatch.CallDispatchHeadNode;
import org.jruby.truffle.language.dispatch.DispatchHeadNodeFactory;
import org.jruby.truffle.language.dispatch.DoesRespondDispatchHeadNode;
import org.jruby.truffle.language.objects.WriteObjectFieldNode;
import org.jruby.truffle.language.objects.WriteObjectFieldNodeGen;

@AcceptMessage(value = "WRITE", receiverType = RubyObjectType.class, language = RubyLanguage.class)
public final class ForeignWriteNode extends ForeignWriteBaseNode {

    @Child private Node findContextNode;
    @Child private ForeignWriteStringCachingHelperNode helperNode;

    @Override
    public Object access(VirtualFrame frame, DynamicObject object, Object name, Object value) {
        return getHelperNode().executeStringCachingHelper(frame, object, name, value);
    }

    private ForeignWriteStringCachingHelperNode getHelperNode() {
        if (helperNode == null) {
            CompilerDirectives.transferToInterpreter();
            findContextNode = insert(RubyLanguage.INSTANCE.unprotectedCreateFindContextNode());
            final RubyContext context = RubyLanguage.INSTANCE.unprotectedFindContext(findContextNode);
            helperNode = insert(ForeignWriteNodeFactory.ForeignWriteStringCachingHelperNodeGen.create(context, null, null, null));
        }

        return helperNode;
    }

    @ImportStatic(StringCachingGuards.class)
    @NodeChildren({
            @NodeChild("receiver"),
            @NodeChild("name"),
            @NodeChild("value")
    })
    protected static abstract class ForeignWriteStringCachingHelperNode extends RubyNode {

        public ForeignWriteStringCachingHelperNode(RubyContext context) {
            super(context, null);
        }

        public abstract Object executeStringCachingHelper(VirtualFrame frame, DynamicObject receiver,
                                                          Object name, Object value);

        @Specialization(
                guards = {
                        "isRubyString(name)",
                        "ropesEqual(name, cachedRope)"
                },
                limit = "getCacheLimit()"
        )
        public Object cacheStringAndForward(
                VirtualFrame frame,
                DynamicObject receiver,
                DynamicObject name,
                Object value,
                @Cached("privatizeRope(name)") Rope cachedRope,
                @Cached("ropeToString(cachedRope)") String cachedString,
                @Cached("startsWithAt(cachedString)") boolean cachedStartsWithAt,
                @Cached("createNextHelper()") ForeignWriteStringCachedHelperNode nextHelper) {
            return nextHelper.executeStringCachedHelper(frame, receiver, name, cachedString, cachedStartsWithAt, value);
        }

        @Specialization(
                guards = "isRubyString(name)",
                contains = "cacheStringAndForward"
        )
        public Object uncachedStringAndForward(
                VirtualFrame frame,
                DynamicObject receiver,
                DynamicObject name,
                Object value,
                @Cached("createNextHelper()") ForeignWriteStringCachedHelperNode nextHelper) {
            final String nameString = objectToString(name);
            return nextHelper.executeStringCachedHelper(frame, receiver, name, nameString,
                    startsWithAt(nameString), value);
        }

        @Specialization(
                guards = {
                        "isRubySymbol(name)",
                        "name == cachedName"
                },
                limit = "getCacheLimit()"
        )
        public Object cacheSymbolAndForward(
                VirtualFrame frame,
                DynamicObject receiver,
                DynamicObject name,
                Object value,
                @Cached("name") DynamicObject cachedName,
                @Cached("objectToString(cachedName)") String cachedString,
                @Cached("startsWithAt(cachedString)") boolean cachedStartsWithAt,
                @Cached("createNextHelper()") ForeignWriteStringCachedHelperNode nextHelper) {
            return nextHelper.executeStringCachedHelper(frame, receiver, cachedName, cachedString,
                    cachedStartsWithAt, value);
        }

        @Specialization(
                guards = "isRubySymbol(name)",
                contains = "cacheSymbolAndForward"
        )
        public Object uncachedSymbolAndForward(
                VirtualFrame frame,
                DynamicObject receiver,
                DynamicObject name,
                Object value,
                @Cached("createNextHelper()") ForeignWriteStringCachedHelperNode nextHelper) {
            final String nameString = objectToString(name);
            return nextHelper.executeStringCachedHelper(frame, receiver, name, nameString,
                    startsWithAt(nameString), value);
        }

        @Specialization(
                guards = "name == cachedName",
                limit = "getCacheLimit()"
        )
        public Object cacheJavaStringAndForward(
                VirtualFrame frame,
                DynamicObject receiver,
                String name,
                Object value,
                @Cached("name") String cachedName,
                @Cached("startsWithAt(cachedName)") boolean cachedStartsWithAt,
                @Cached("createNextHelper()") ForeignWriteStringCachedHelperNode nextHelper) {
            return nextHelper.executeStringCachedHelper(frame, receiver, cachedName, cachedName,
                    cachedStartsWithAt, value);
        }

        @Specialization(contains = "cacheJavaStringAndForward")
        public Object uncachedJavaStringAndForward(
                VirtualFrame frame,
                DynamicObject receiver,
                String name,
                Object value,
                @Cached("createNextHelper()") ForeignWriteStringCachedHelperNode nextHelper) {
            return nextHelper.executeStringCachedHelper(frame, receiver, name, name, startsWithAt(name), value);
        }

        protected ForeignWriteStringCachedHelperNode createNextHelper() {
            return ForeignWriteNodeFactory.ForeignWriteStringCachedHelperNodeGen.create(null, null, null, null, null);
        }

        @CompilerDirectives.TruffleBoundary
        protected String objectToString(DynamicObject string) {
            return string.toString();
        }

        protected String ropeToString(Rope rope) {
            return RopeOperations.decodeRope(getContext().getJRubyRuntime(), rope);
        }

        @CompilerDirectives.TruffleBoundary
        protected boolean startsWithAt(String name) {
            return !name.isEmpty() && name.charAt(0) == '@';
        }

        protected int getCacheLimit() {
            return getContext().getOptions().INTEROP_WRITE_CACHE;
        }

    }

    @NodeChildren({
            @NodeChild("receiver"),
            @NodeChild("name"),
            @NodeChild("stringName"),
            @NodeChild("startsAt"),
            @NodeChild("value")
    })
    protected static abstract class ForeignWriteStringCachedHelperNode extends RubyNode {

        @Child private DoesRespondDispatchHeadNode definedNode;
        @Child private DoesRespondDispatchHeadNode indexDefinedNode;
        @Child private CallDispatchHeadNode callNode;

        protected final static String INDEX_METHOD_NAME = "[]=";

        public abstract Object executeStringCachedHelper(VirtualFrame frame, DynamicObject receiver, Object name,
                                                         String stringName, boolean startsAt, Object value);

        @Specialization(guards = "startsAt(startsAt)")
        public Object readInstanceVariable(
                DynamicObject receiver,
                Object name,
                String stringName,
                boolean startsAt,
                Object value,
                @Cached("createWriteObjectFieldNode(stringName)") WriteObjectFieldNode writeObjectFieldNode) {
            writeObjectFieldNode.execute(receiver, value);
            return value;
        }

        protected boolean startsAt(boolean startsAt) {
            return startsAt;
        }

        protected WriteObjectFieldNode createWriteObjectFieldNode(String name) {
            return WriteObjectFieldNodeGen.create(name);
        }

        @Specialization(
                guards = {
                        "notStartsAt(startsAt)",
                        "methodDefined(frame, receiver, writeMethodName, getDefinedNode())"
                }
        )
        public Object callMethod(
                VirtualFrame frame,
                DynamicObject receiver,
                Object name,
                String stringName,
                boolean startsAt,
                Object value,
                @Cached("createWriteMethodName(stringName)") String writeMethodName) {
            return getCallNode().call(frame, receiver, writeMethodName, null, value);
        }

        protected String createWriteMethodName(String name) {
            return name + "=";
        }

        @Specialization(
                guards = {
                        "notStartsAt(startsAt)",
                        "!methodDefined(frame, receiver, writeMethodName, getDefinedNode())",
                        "methodDefined(frame, receiver, INDEX_METHOD_NAME, getIndexDefinedNode())"
                }
        )
        public Object index(
                VirtualFrame frame,
                DynamicObject receiver,
                Object name,
                String stringName,
                boolean startsAt,
                Object value,
                @Cached("createWriteMethodName(stringName)") String writeMethodName) {
            return getCallNode().call(frame, receiver, "[]", null, name, value);
        }

        protected boolean notStartsAt(boolean startsAt) {
            return !startsAt;
        }

        protected DoesRespondDispatchHeadNode getDefinedNode() {
            if (definedNode == null) {
                CompilerDirectives.transferToInterpreter();
                definedNode = insert(new DoesRespondDispatchHeadNode(getContext(), true));
            }

            return definedNode;
        }

        protected DoesRespondDispatchHeadNode getIndexDefinedNode() {
            if (indexDefinedNode == null) {
                CompilerDirectives.transferToInterpreter();
                indexDefinedNode = insert(new DoesRespondDispatchHeadNode(getContext(), true));
            }

            return indexDefinedNode;
        }

        protected boolean methodDefined(VirtualFrame frame, DynamicObject receiver, String stringName,
                                        DoesRespondDispatchHeadNode definedNode) {
            return definedNode.doesRespondTo(frame, stringName, receiver);
        }

        protected CallDispatchHeadNode getCallNode() {
            if (callNode == null) {
                CompilerDirectives.transferToInterpreter();
                callNode = insert(DispatchHeadNodeFactory.createMethodCall(getContext(), true));
            }

            return callNode;
        }

    }

}
