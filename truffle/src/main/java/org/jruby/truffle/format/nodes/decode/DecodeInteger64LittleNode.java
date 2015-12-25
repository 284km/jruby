/*
 * Copyright (c) 2015 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.format.nodes.decode;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;
import org.jruby.truffle.format.nodes.PackNode;
import org.jruby.truffle.format.runtime.MissingValue;
import org.jruby.truffle.runtime.RubyContext;

@NodeChildren({
        @NodeChild(value = "bytes", type = PackNode.class),
})
public abstract class DecodeInteger64LittleNode extends PackNode {

    public DecodeInteger64LittleNode(RubyContext context) {
        super(context);
    }

    @Specialization
    public MissingValue decode(VirtualFrame frame, MissingValue missingValue) {
        return missingValue;
    }

    @Specialization(guards = "isNil(nil)")
    public DynamicObject decode(VirtualFrame frame, DynamicObject nil) {
        return nil;
    }

    @Specialization
    public long decode(VirtualFrame frame, byte[] bytes) {
        return bytes[0] | bytes[1] << 8 | bytes[2] << 16 | bytes[3] << 24 | bytes[4] << 32 | bytes[5] << 40 | bytes[6] << 48 | bytes[7] << 56;
    }

}
