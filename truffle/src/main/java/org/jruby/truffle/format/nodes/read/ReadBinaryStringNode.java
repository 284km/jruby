/*
 * Copyright (c) 2015 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.format.nodes.read;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import org.jruby.truffle.format.nodes.PackNode;
import org.jruby.truffle.format.nodes.SourceNode;
import org.jruby.truffle.format.runtime.MissingValue;
import org.jruby.truffle.runtime.RubyContext;
import org.jruby.truffle.runtime.layouts.Layouts;
import org.jruby.util.ByteList;
import org.jruby.util.StringSupport;

import java.util.Arrays;

@NodeChildren({
        @NodeChild(value = "source", type = SourceNode.class),
})
public abstract class ReadBinaryStringNode extends PackNode {

    final boolean readToEnd;
    final int count;
    final boolean trimTrailingSpaces;
    final boolean trimTrailingNulls;

    public ReadBinaryStringNode(RubyContext context, boolean readToEnd, int count, boolean trimTrailingSpaces, boolean trimTrailingNulls) {
        super(context);
        this.readToEnd = readToEnd;
        this.count = count;
        this.trimTrailingSpaces = trimTrailingSpaces;
        this.trimTrailingNulls = trimTrailingNulls;
    }

    @Specialization(guards = "isNull(source)")
    public void read(VirtualFrame frame, Object source) {
        CompilerDirectives.transferToInterpreter();

        // Advance will handle the error
        advanceSourcePosition(frame, count);

        throw new IllegalStateException();
    }

    @Specialization
    public Object read(VirtualFrame frame, byte[] source) {
        final int start = getSourcePosition(frame);

        int length;
        ByteList result;

        if (readToEnd) {
            length = 0;

            while (start + length < getSourceLength(frame)) {
                length++;
            }
        } else {
            length = count;

            if (start + length >= getSourceLength(frame)) {
                length = getSourceLength(frame) - start;
            }
        }

        int usedLength = length;

        while (usedLength > 0 && ((trimTrailingSpaces && source[start + usedLength - 1] == ' ') || (trimTrailingNulls && source[start + usedLength - 1] == 0))) {
            usedLength--;
        }

        result = new ByteList(source, start, usedLength, true);

        setSourcePosition(frame, start + length);

        return Layouts.STRING.createString(getContext().getCoreLibrary().getStringFactory(), result, StringSupport.CR_UNKNOWN, null);
    }

}
