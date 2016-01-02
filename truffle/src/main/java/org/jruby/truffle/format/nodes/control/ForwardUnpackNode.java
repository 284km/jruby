/*
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.format.nodes.control;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import org.jruby.truffle.format.nodes.PackNode;
import org.jruby.truffle.format.runtime.exceptions.OutsideOfStringException;
import org.jruby.truffle.runtime.RubyContext;

public class ForwardUnpackNode extends PackNode {

    public ForwardUnpackNode(RubyContext context) {
        super(context);
    }

    @Override
    public Object execute(VirtualFrame frame) {
        setSourcePosition(frame, getSourcePosition(frame) + 1);
        return null;
    }

}
