/*
 * Copyright (c) 2014, 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.core.encoding;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.object.DynamicObject;
import org.jcodings.Encoding;
import org.jcodings.EncodingDB;
import org.jcodings.specific.USASCIIEncoding;
import org.jruby.truffle.Layouts;
import org.jruby.truffle.RubyContext;
import org.jruby.truffle.core.rope.Rope;
import org.jruby.truffle.core.string.StringOperations;
import org.jruby.util.ByteList;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EncodingManager {

    private final DynamicObject[] ENCODING_LIST = new DynamicObject[EncodingDB.getEncodings().size()];
    private final Map<String, DynamicObject> LOOKUP = new HashMap<>();

    private final RubyContext context;

    public EncodingManager(RubyContext context) {
        this.context = context;
    }

    @TruffleBoundary
    public static DynamicObject newRubyEncoding(RubyContext context, DynamicObject encodingClass, Encoding encoding, byte[] name, int p, int end, boolean dummy) {
        // TODO (nirvdrum 21-Jun-16): We probably don't need to create a ByteList and two Ropes. Without any guarantees on the code range of the encoding name, however, we must be conservative.
        final Rope rope = StringOperations.ropeFromByteList(new ByteList(name, p, end, USASCIIEncoding.INSTANCE, false));
        final Rope cachedRope = context.getRopeTable().getRope(rope.getBytes(), rope.getEncoding(), rope.getCodeRange());
        final DynamicObject string = context.getFrozenStrings().getFrozenString(cachedRope);

        return Layouts.ENCODING.createEncoding(Layouts.CLASS.getInstanceFactory(encodingClass), encoding, string, dummy);
    }

    public DynamicObject[] getUnsafeEncodingList() {
        return ENCODING_LIST;
    }

    @TruffleBoundary
    public DynamicObject getRubyEncoding(Encoding encoding) {
        return LOOKUP.get(new String(encoding.getName(), StandardCharsets.UTF_8).toLowerCase(Locale.ENGLISH));
    }

    @TruffleBoundary
    public DynamicObject getRubyEncoding(String name) {
        return LOOKUP.get(name.toLowerCase(Locale.ENGLISH));
    }

    @TruffleBoundary
    public DynamicObject getRubyEncoding(int encodingListIndex) {
        return ENCODING_LIST[encodingListIndex];
    }

    @TruffleBoundary
    public void defineEncoding(DynamicObject encodingClass, EncodingDB.Entry encodingEntry, byte[] name, int p, int end) {
        final DynamicObject rubyEncoding = newRubyEncoding(context, encodingClass, null, name, p, end, encodingEntry.isDummy());

        ENCODING_LIST[encodingEntry.getIndex()] = rubyEncoding;
        LOOKUP.put(Layouts.ENCODING.getName(rubyEncoding).toString().toLowerCase(Locale.ENGLISH), rubyEncoding);
    }

    @TruffleBoundary
    public void defineAlias(int encodingListIndex, String name) {
        final DynamicObject rubyEncoding = getRubyEncoding(encodingListIndex);

        LOOKUP.put(name.toLowerCase(Locale.ENGLISH), rubyEncoding);
    }

}
