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
import org.jruby.truffle.format.nodes.PackNode;
import org.jruby.truffle.runtime.RubyContext;

@NodeChildren({
        @NodeChild(value = "bytes", type = PackNode.class),
})
public abstract class DecodeInteger32BigNode extends PackNode {

    public DecodeInteger32BigNode(RubyContext context) {
        super(context);
    }

    @Specialization
    public int decode(VirtualFrame frame, byte[] bytes) {
        return bytes[3] | bytes[2] << 8 | bytes[1] << 16 | bytes[0] << 24;
    }

}
