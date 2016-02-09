/*
 * Copyright (c) 2015, 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.core.format.nodes.decode;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;
import org.jruby.truffle.RubyContext;
import org.jruby.truffle.core.format.nodes.PackNode;
import org.jruby.truffle.core.format.runtime.MissingValue;

@NodeChildren({
        @NodeChild(value = "value", type = PackNode.class),
})
public abstract class DecodeFloat32Node extends PackNode {

    public DecodeFloat32Node(RubyContext context) {
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
    public float decode(VirtualFrame frame, int value) {
        return Float.intBitsToFloat(value);
    }

}
