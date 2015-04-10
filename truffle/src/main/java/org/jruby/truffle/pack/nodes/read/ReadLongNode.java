/*
 * Copyright (c) 2015 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.pack.nodes.read;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import org.jruby.RubyBignum;
import org.jruby.RubyInteger;
import org.jruby.runtime.builtin.IRubyObject;
import org.jruby.truffle.pack.nodes.PackNode;
import org.jruby.truffle.pack.nodes.SourceNode;
import org.jruby.truffle.pack.nodes.type.ToLongNode;
import org.jruby.truffle.pack.nodes.type.ToLongNodeGen;
import org.jruby.truffle.pack.nodes.write.NullNode;
import org.jruby.truffle.runtime.RubyContext;

/**
 * Read a {@code long} value from the source, converting if needed. We use
 * {@code} long to represent all integers as the longest value we use is 64bit.
 */
@NodeChildren({
        @NodeChild(value = "source", type = SourceNode.class),
})
public abstract class ReadLongNode extends PackNode {

    private final RubyContext context;

    @Child private ToLongNode toLongNode;

    public ReadLongNode(RubyContext context) {
        this.context = context;
    }

    @Specialization(guards = "isNull(source)")
    public long read(VirtualFrame frame, Object source) {
        CompilerDirectives.transferToInterpreter();

        // Advance will handle the error
        advanceSourcePosition(frame);

        throw new IllegalStateException();
    }

    @Specialization
    public long read(VirtualFrame frame, int[] source) {
        return source[advanceSourcePosition(frame)];
    }

    @Specialization
    public long read(VirtualFrame frame, long[] source) {
        return source[advanceSourcePosition(frame)];
    }

    @Specialization
    public long read(VirtualFrame frame, double[] source) {
        return (long) source[advanceSourcePosition(frame)];
    }

    @Specialization(guards = "!isIRubyArray(source)")
    public long read(VirtualFrame frame, Object[] source) {
        if (toLongNode == null) {
            CompilerDirectives.transferToInterpreter();
            toLongNode = insert(ToLongNodeGen.create(context, new NullNode()));
        }

        return toLongNode.executeToLong(frame, source[advanceSourcePosition(frame)]);
    }

    @Specialization
    public long read(VirtualFrame frame, IRubyObject[] source) {
        return toLong(source[advanceSourcePosition(frame)]);
    }

    @CompilerDirectives.TruffleBoundary
    private long toLong(IRubyObject object) {
        final RubyInteger integer = object.convertToInteger();

        if (integer instanceof RubyBignum) {
            return integer.getBigIntegerValue().longValue();
        } else {
            return integer.getLongValue();
        }
    }

}
