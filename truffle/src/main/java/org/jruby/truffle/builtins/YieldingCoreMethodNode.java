/*
 * Copyright (c) 2013, 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.builtins;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.source.SourceSection;
import org.jruby.truffle.RubyContext;
import org.jruby.truffle.core.cast.BooleanCastNode;
import org.jruby.truffle.core.cast.BooleanCastNodeGen;
import org.jruby.truffle.language.SourceIndexLength;
import org.jruby.truffle.language.yield.YieldNode;

public abstract class YieldingCoreMethodNode extends CoreMethodArrayArgumentsNode {

    @Child private YieldNode dispatchNode;
    @Child private BooleanCastNode booleanCastNode;

    public YieldingCoreMethodNode() {
        this(null, (SourceSection) null);
    }

    public YieldingCoreMethodNode(RubyContext context, SourceSection sourceSection) {
        super(context, sourceSection);
        dispatchNode = new YieldNode(context);
    }

    public YieldingCoreMethodNode(RubyContext context, SourceIndexLength sourceSection) {
        super(context, sourceSection);
        dispatchNode = new YieldNode(context);
    }

    public Object yield(VirtualFrame frame, DynamicObject block, Object... arguments) {
        return dispatchNode.dispatch(frame, block, arguments);
    }

    public Object yieldWithBlock(VirtualFrame frame, DynamicObject block, DynamicObject blockArgument, Object... arguments) {
        return dispatchNode.dispatchWithBlock(frame, block, blockArgument, arguments);
    }

    public boolean yieldIsTruthy(VirtualFrame frame, DynamicObject block, Object... arguments) {
        return booleanCast(yield(frame, block, arguments));
    }

    private boolean booleanCast(Object value) {
        if (booleanCastNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            booleanCastNode = insert(BooleanCastNodeGen.create(null));
        }
        return booleanCastNode.executeToBoolean(value);
    }

}
