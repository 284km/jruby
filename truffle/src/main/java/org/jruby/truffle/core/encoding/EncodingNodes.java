/*
 * Copyright (c) 2014, 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 *
 * Contains code modified from JRuby's RubyConverter.java
 */
package org.jruby.truffle.core.encoding;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.source.SourceSection;
import org.jcodings.Encoding;
import org.jcodings.EncodingDB;
import org.jcodings.specific.ASCIIEncoding;
import org.jcodings.specific.USASCIIEncoding;
import org.jcodings.specific.UTF8Encoding;
import org.jcodings.util.CaseInsensitiveBytesHash;
import org.jcodings.util.Hash;
import org.jruby.truffle.Layouts;
import org.jruby.truffle.RubyContext;
import org.jruby.truffle.builtins.CoreClass;
import org.jruby.truffle.builtins.CoreMethod;
import org.jruby.truffle.builtins.CoreMethodArrayArgumentsNode;
import org.jruby.truffle.builtins.NonStandard;
import org.jruby.truffle.builtins.Primitive;
import org.jruby.truffle.builtins.PrimitiveArrayArgumentsNode;
import org.jruby.truffle.builtins.UnaryCoreMethodNode;
import org.jruby.truffle.core.cast.ToEncodingNode;
import org.jruby.truffle.core.cast.ToEncodingNodeGen;
import org.jruby.truffle.core.cast.ToStrNode;
import org.jruby.truffle.core.cast.ToStrNodeGen;
import org.jruby.truffle.core.rope.CodeRange;
import org.jruby.truffle.core.rope.Rope;
import org.jruby.truffle.core.rope.RopeOperations;
import org.jruby.truffle.core.string.StringOperations;
import org.jruby.truffle.language.RubyGuards;
import org.jruby.truffle.language.control.RaiseException;
import org.jruby.truffle.language.dispatch.CallDispatchHeadNode;
import org.jruby.truffle.language.dispatch.DispatchHeadNodeFactory;
import org.jruby.util.ByteList;

@CoreClass("Encoding")
public abstract class EncodingNodes {

    @CoreMethod(names = "ascii_compatible?")
    public abstract static class AsciiCompatibleNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "encoding == cachedEncoding", limit = "getCacheLimit()")
        public boolean isCompatibleCached(DynamicObject encoding,
                                          @Cached("encoding") DynamicObject cachedEncoding,
                                          @Cached("isAsciiCompatible(cachedEncoding)") boolean isAsciiCompatible) {
            return isAsciiCompatible;
        }

        @Specialization(contains = "isCompatibleCached")
        public boolean isCompatibleUncached(DynamicObject encoding) {
            return isAsciiCompatible(encoding);
        }

        protected int getCacheLimit() {
            return getContext().getOptions().ENCODING_LOADED_CLASSES_CACHE;
        }

        protected static boolean isAsciiCompatible(DynamicObject encoding) {
            assert RubyGuards.isRubyEncoding(encoding);

            return EncodingOperations.getEncoding(encoding).isAsciiCompatible();
        }
    }

    @CoreMethod(names = "compatible?", needsSelf = false, onSingleton = true, required = 2)
    public abstract static class CompatibleQueryNode extends CoreMethodArrayArgumentsNode {

        @Child private ToEncodingNode toEncodingNode;

        @Specialization(guards = {
                "bothAreStrings(first, second)",
                "firstEncoding == secondEncoding",
                "extractEncoding(first) == firstEncoding",
                "extractEncoding(second) == secondEncoding"
        }, limit = "getCacheLimit()")
        public DynamicObject isCompatibleStringStringCached(DynamicObject first, DynamicObject second,
                                                     @Cached("getEncoding(first)") Encoding firstEncoding,
                                                     @Cached("getEncoding(second)") Encoding secondEncoding,
                                                     @Cached("isCompatibleStringStringUncached(first, second)") DynamicObject rubyEncoding) {
            return rubyEncoding;
        }

        @Specialization(guards = "bothAreStrings(first, second)", contains =  "isCompatibleStringStringCached")
        public DynamicObject isCompatibleStringStringUncached(DynamicObject first, DynamicObject second) {
            final Encoding compatibleEncoding = compatibleEncodingForStrings(first, second);

            if (compatibleEncoding != null) {
                return getContext().getEncodingManager().getRubyEncoding(compatibleEncoding);
            } else {
                return nil();
            }
        }

        // TODO (nirvdrum 22-Jun-16): Reorder these guards so the cheap check is first after the new Truffle DSL generator is live -- the current one has a bug that's mitigated by reordering the guards.
        @Specialization(guards = {
                "firstEncoding != null",
                "secondEncoding != null",
                "getEncoding(first) == firstEncoding",
                "getEncoding(second) == secondEncoding",
                "!bothAreStrings(first, second)"
        }, limit = "getCacheLimit()")
        public DynamicObject isCompatibleEncodingCached(DynamicObject first, DynamicObject second,
                                                 @Cached("getEncoding(first)") Encoding firstEncoding,
                                                 @Cached("getEncoding(second)") Encoding secondEncoding,
                                                 @Cached("getCompatibleEncoding(getContext(), firstEncoding, secondEncoding)") DynamicObject result) {

            return result;
        }

        @Specialization(guards = "!bothAreStrings(first, second)", contains = "isCompatibleEncodingCached")
        public DynamicObject isCompatibleEncodingUncached(DynamicObject first, DynamicObject second) {
            final Encoding firstEncoding = getEncoding(first);
            final Encoding secondEncoding = getEncoding(second);

            if (firstEncoding == null || secondEncoding == null) {
                return nil();
            }

            return getCompatibleEncoding(getContext(), firstEncoding, secondEncoding);
        }

        public static Encoding compatibleEncodingForStrings(DynamicObject first, DynamicObject second) {
            // Taken from org.jruby.RubyEncoding#areCompatible.

            assert RubyGuards.isRubyString(first);
            assert RubyGuards.isRubyString(second);

            final Rope firstRope = StringOperations.rope(first);
            final Rope secondRope = StringOperations.rope(second);

            return compatibleEncodingForRopes(firstRope, secondRope);
        }

        @TruffleBoundary
        public static Encoding compatibleEncodingForRopes(Rope firstRope, Rope secondRope) {
            // Taken from org.jruby.RubyEncoding#areCompatible.

            final Encoding firstEncoding = firstRope.getEncoding();
            final Encoding secondEncoding = secondRope.getEncoding();

            if (firstEncoding == null || secondEncoding == null) return null;
            if (firstEncoding == secondEncoding) return firstEncoding;

            if (secondRope.isEmpty()) return firstEncoding;
            if (firstRope.isEmpty()) {
                return firstEncoding.isAsciiCompatible() && isAsciiOnly(secondRope) ? firstEncoding : secondEncoding;
            }

            if (!firstEncoding.isAsciiCompatible() || !secondEncoding.isAsciiCompatible()) return null;

            if (firstRope.getCodeRange() != secondRope.getCodeRange()) {
                if (firstRope.getCodeRange() == CodeRange.CR_7BIT) return secondEncoding;
                if (secondRope.getCodeRange() == CodeRange.CR_7BIT) return firstEncoding;
            }
            if (secondRope.getCodeRange() == CodeRange.CR_7BIT) return firstEncoding;
            if (firstRope.getCodeRange() == CodeRange.CR_7BIT) return secondEncoding;

            return null;
        }

        @TruffleBoundary
        public static Encoding areCompatible(Encoding enc1, Encoding enc2) {
            if (enc1 == null || enc2 == null) return null;
            if (enc1 == enc2) return enc1;

            if (!enc1.isAsciiCompatible() || !enc2.isAsciiCompatible()) return null;

            if (enc2 instanceof USASCIIEncoding) return enc1;
            if (enc1 instanceof USASCIIEncoding) return enc2;

            return null;
        }

        public static DynamicObject getCompatibleEncoding(RubyContext context, Encoding first, Encoding second) {
            final Encoding compatibleEncoding = areCompatible(first, second);

            if (compatibleEncoding != null) {
                return context.getEncodingManager().getRubyEncoding(compatibleEncoding);
            } else {
                return context.getCoreLibrary().getNilObject();
            }
        }

        @TruffleBoundary
        private static boolean isAsciiOnly(Rope rope) {
            return rope.getEncoding().isAsciiCompatible() && rope.getCodeRange() == CodeRange.CR_7BIT;
        }

        protected Encoding extractEncoding(DynamicObject string) {
            if (RubyGuards.isRubyString(string)) {
                return Layouts.STRING.getRope(string).getEncoding();
            }

            return null;
        }

        protected static boolean bothAreStrings(DynamicObject first, DynamicObject second) {
            return RubyGuards.isRubyString(first) && RubyGuards.isRubyString(second);
        }

        protected Encoding getEncoding(DynamicObject value) {
            if (toEncodingNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                toEncodingNode = insert(ToEncodingNode.create());
            }

            return toEncodingNode.executeToEncoding(value);
        }

        protected int getCacheLimit() {
            return getContext().getOptions().ENCODING_COMPATIBILE_QUERY_CACHE;
        }

    }

    @NonStandard
    @CoreMethod(names = "default_external_jruby=", onSingleton = true, required = 1)
    public abstract static class SetDefaultExternalNode extends CoreMethodArrayArgumentsNode {

        @Child private ToStrNode toStrNode;

        @TruffleBoundary
        @Specialization(guards = "isRubyEncoding(encoding)")
        public DynamicObject defaultExternalEncoding(DynamicObject encoding) {
            getContext().getJRubyRuntime().setDefaultExternalEncoding(EncodingOperations.getEncoding(encoding));

            return encoding;
        }

        @TruffleBoundary
        @Specialization(guards = "isRubyString(encodingString)")
        public DynamicObject defaultExternal(DynamicObject encodingString) {
            final DynamicObject rubyEncoding = getContext().getEncodingManager().getRubyEncoding(encodingString.toString());
            getContext().getJRubyRuntime().setDefaultExternalEncoding(EncodingOperations.getEncoding(rubyEncoding));

            return rubyEncoding;
        }

        @TruffleBoundary
        @Specialization(guards = "isNil(nil)")
        public DynamicObject defaultExternal(Object nil) {
            throw new RaiseException(coreExceptions().argumentError("default external can not be nil", this));
        }

        @Specialization(guards = { "!isRubyEncoding(encoding)", "!isRubyString(encoding)", "!isNil(encoding)" })
        public DynamicObject defaultExternal(VirtualFrame frame, Object encoding) {
            if (toStrNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                toStrNode = insert(ToStrNodeGen.create(getContext(), getSourceSection(), null));
            }

            return defaultExternal(toStrNode.executeToStr(frame, encoding));
        }

    }

    @NonStandard
    @CoreMethod(names = "default_internal_jruby=", onSingleton = true, required = 1)
    public abstract static class SetDefaultInternalNode extends CoreMethodArrayArgumentsNode {

        @Child private ToStrNode toStrNode;

        @TruffleBoundary
        @Specialization(guards = "isRubyEncoding(encoding)")
        public DynamicObject defaultInternal(DynamicObject encoding) {
            getContext().getJRubyRuntime().setDefaultInternalEncoding(EncodingOperations.getEncoding(encoding));

            return encoding;
        }

        @TruffleBoundary
        @Specialization(guards = "isNil(encoding)")
        public DynamicObject defaultInternal(Object encoding) {
            getContext().getJRubyRuntime().setDefaultInternalEncoding(null);

            return nil();
        }

        @Specialization(guards = { "!isRubyEncoding(encoding)", "!isNil(encoding)" })
        public DynamicObject defaultInternal(VirtualFrame frame, Object encoding) {
            if (toStrNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                toStrNode = insert(ToStrNodeGen.create(getContext(), getSourceSection(), null));
            }

            final DynamicObject encodingName = toStrNode.executeToStr(frame, encoding);
            getContext().getJRubyRuntime().setDefaultInternalEncoding(EncodingOperations.getEncoding(getContext().getEncodingManager().getRubyEncoding(encodingName.toString())));

            return encodingName;
        }

    }

    @CoreMethod(names = "list", onSingleton = true)
    public abstract static class ListNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        public DynamicObject list() {
            final DynamicObject[] encodings = getContext().getEncodingManager().getUnsafeEncodingList();
            final Object[] arrayStore = new Object[encodings.length];

            System.arraycopy(encodings, 0, arrayStore, 0, encodings.length);

            return Layouts.ARRAY.createArray(coreLibrary().getArrayFactory(), arrayStore, arrayStore.length);
        }
    }


    @CoreMethod(names = "locale_charmap", onSingleton = true)
    public abstract static class LocaleCharacterMapNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public DynamicObject localeCharacterMap() {
            final DynamicObject rubyEncoding = getContext().getEncodingManager().getRubyEncoding(getContext().getEncodingManager().getLocaleEncoding());

            return Layouts.ENCODING.getName(rubyEncoding);
        }
    }

    @CoreMethod(names = "dummy?")
    public abstract static class DummyNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "encoding == cachedEncoding", limit = "getCacheLimit()")
        public boolean isDummyCached(DynamicObject encoding,
                                     @Cached("encoding") DynamicObject cachedEncoding,
                                     @Cached("isDummy(cachedEncoding)") boolean isDummy) {
            return isDummy;
        }

        @Specialization(contains = "isDummyCached")
        public boolean isDummyUncached(DynamicObject encoding) {
            return isDummy(encoding);
        }

        protected int getCacheLimit() {
            return getContext().getOptions().ENCODING_LOADED_CLASSES_CACHE;
        }

        protected static boolean isDummy(DynamicObject encoding) {
            assert RubyGuards.isRubyEncoding(encoding);

            return Layouts.ENCODING.getDummy(encoding);
        }
    }

    @NonStandard
    @CoreMethod(names = "encoding_map", onSingleton = true)
    public abstract static class EncodingMapNode extends CoreMethodArrayArgumentsNode {

        @Child private CallDispatchHeadNode upcaseNode;
        @Child private CallDispatchHeadNode toSymNode;
        @Child private CallDispatchHeadNode newLookupTableNode;
        @Child private CallDispatchHeadNode lookupTableWriteNode;
        @Child private CallDispatchHeadNode newTupleNode;

        public EncodingMapNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
            upcaseNode = DispatchHeadNodeFactory.createMethodCall(context);
            toSymNode = DispatchHeadNodeFactory.createMethodCall(context);
            newLookupTableNode = DispatchHeadNodeFactory.createMethodCall(context);
            lookupTableWriteNode = DispatchHeadNodeFactory.createMethodCall(context);
            newTupleNode = DispatchHeadNodeFactory.createMethodCall(context);
        }

        @Specialization
        public Object encodingMap(VirtualFrame frame) {
            Object ret = newLookupTableNode.call(frame, coreLibrary().getLookupTableClass(), "new", null);

            final DynamicObject[] encodings = getContext().getEncodingManager().getUnsafeEncodingList();
            for (int i = 0; i < encodings.length; i++) {
                final Object upcased = upcaseNode.call(frame, Layouts.ENCODING.getName(encodings[i]), "upcase", null);
                final Object key = toSymNode.call(frame, upcased, "to_sym", null);
                final Object value = newTupleNode.call(frame, coreLibrary().getTupleClass(), "create", null, nil(), i);

                lookupTableWriteNode.call(frame, ret, "[]=", null, key, value);
            }

            final Hash<EncodingDB.Entry>.HashEntryIterator i = EncodingDB.getAliases().entryIterator();
            while (i.hasNext()) {
                final CaseInsensitiveBytesHash.CaseInsensitiveBytesHashEntry<EncodingDB.Entry> e =
                        ((CaseInsensitiveBytesHash.CaseInsensitiveBytesHashEntry<EncodingDB.Entry>) i.next());

                final Object upcased = upcaseNode.call(frame, createString(new ByteList(e.bytes, e.p, e.end - e.p)), "upcase", null);
                final Object key = toSymNode.call(frame, upcased, "to_sym", null);
                final DynamicObject alias = createString(new ByteList(e.bytes, e.p, e.end - e.p));
                final int index = e.value.getIndex();


                final Object value = newTupleNode.call(frame, coreLibrary().getTupleClass(), "create", null, alias, index);
                lookupTableWriteNode.call(frame, ret, "[]=", null, key, value);
            }

            final Encoding defaultInternalEncoding = getContext().getJRubyRuntime().getDefaultInternalEncoding();
            final Object internalTuple = makeTuple(frame, newTupleNode, create7BitString("internal", UTF8Encoding.INSTANCE), indexLookup(encodings, defaultInternalEncoding));
            lookupTableWriteNode.call(frame, ret, "[]=", null, getSymbol("INTERNAL"), internalTuple);

            final Encoding defaultExternalEncoding = getContext().getJRubyRuntime().getDefaultExternalEncoding();
            final Object externalTuple = makeTuple(frame, newTupleNode, create7BitString("external", UTF8Encoding.INSTANCE), indexLookup(encodings, defaultExternalEncoding));
            lookupTableWriteNode.call(frame, ret, "[]=", null, getSymbol("EXTERNAL"), externalTuple);

            final Encoding localeEncoding = getContext().getEncodingManager().getLocaleEncoding();
            final Object localeTuple = makeTuple(frame, newTupleNode, create7BitString("locale", UTF8Encoding.INSTANCE), indexLookup(encodings, localeEncoding));
            lookupTableWriteNode.call(frame, ret, "[]=", null, getSymbol("LOCALE"), localeTuple);

            final Encoding filesystemEncoding = getContext().getEncodingManager().getLocaleEncoding();
            final Object filesystemTuple = makeTuple(frame, newTupleNode, create7BitString("filesystem", UTF8Encoding.INSTANCE), indexLookup(encodings, filesystemEncoding));
            lookupTableWriteNode.call(frame, ret, "[]=", null, getSymbol("FILESYSTEM"), filesystemTuple);

            return ret;
        }

        private Object makeTuple(VirtualFrame frame, CallDispatchHeadNode newTupleNode, Object... values) {
            return newTupleNode.call(frame, coreLibrary().getTupleClass(), "create", null, values);
        }

        @TruffleBoundary
        public Object indexLookup(DynamicObject[] encodings, Encoding encoding) {
            // TODO (nirvdrum 25-Mar-15): Build up this lookup table in RubyEncoding as we register encodings.
            if (encoding == null) {
                return nil();
            }

            final Rope encodingNameRope = RopeOperations.create(encoding.getName(), ASCIIEncoding.INSTANCE, CodeRange.CR_UNKNOWN);

            for (int i = 0; i < encodings.length; i++) {
                final Rope nameRope = StringOperations.rope(Layouts.ENCODING.getName(encodings[i]));
                if (nameRope.equals(encodingNameRope)) {
                    return i;
                }
            }

            throw new UnsupportedOperationException(String.format("Could not find encoding %s in the registered encoding list", encoding.toString()));
        }
    }

    @CoreMethod(names = { "name", "to_s" })
    public abstract static class ToSNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public DynamicObject toS(DynamicObject encoding) {
            return Layouts.ENCODING.getName(encoding);
        }

    }

    @CoreMethod(names = "allocate", constructor = true)
    public abstract static class AllocateNode extends UnaryCoreMethodNode {

        @TruffleBoundary
        @Specialization
        public DynamicObject allocate(DynamicObject rubyClass) {
            throw new RaiseException(coreExceptions().typeErrorAllocatorUndefinedFor(rubyClass, this));
        }

    }

    @Primitive(name = "encoding_get_object_encoding", needsSelf = false)
    public static abstract class EncodingGetObjectEncodingNode extends PrimitiveArrayArgumentsNode {

        @Specialization(guards = "isRubyString(string)")
        public DynamicObject encodingGetObjectEncodingString(DynamicObject string) {
            return getContext().getEncodingManager().getRubyEncoding(Layouts.STRING.getRope(string).getEncoding());
        }

        @Specialization(guards = "isRubySymbol(symbol)")
        public DynamicObject encodingGetObjectEncodingSymbol(DynamicObject symbol) {
            return getContext().getEncodingManager().getRubyEncoding(Layouts.SYMBOL.getRope(symbol).getEncoding());
        }

        @Specialization(guards = "isRubyEncoding(encoding)")
        public DynamicObject encodingGetObjectEncoding(DynamicObject encoding) {
            return encoding;
        }

        @Specialization(guards = "isRubyRegexp(regexp)")
        public DynamicObject encodingGetObjectEncodingRegexp(DynamicObject regexp) {
            return getContext().getEncodingManager().getRubyEncoding(Layouts.REGEXP.getSource(regexp).getEncoding());
        }

        @Specialization(guards = {"!isRubyString(object)", "!isRubySymbol(object)", "!isRubyEncoding(object)", "!isRubyRegexp(object)"})
        public DynamicObject encodingGetObjectEncodingNil(DynamicObject object) {
            // TODO(CS, 26 Jan 15) something to do with __encoding__ here?
            return nil();
        }

    }

}
