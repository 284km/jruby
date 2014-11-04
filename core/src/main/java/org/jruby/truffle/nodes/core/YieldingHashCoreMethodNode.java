/*
 * Copyright (c) 2013, 2014 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.nodes.core;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.source.SourceSection;
import org.jruby.truffle.nodes.RubyNode;
import org.jruby.truffle.nodes.yield.YieldDispatchHeadNode;
import org.jruby.truffle.runtime.DebugOperations;
import org.jruby.truffle.runtime.RubyContext;
import org.jruby.truffle.runtime.core.RubyProc;
import org.jruby.truffle.runtime.core.RubyTrueClass;

public abstract class YieldingHashCoreMethodNode extends HashCoreMethodNode {

    @Child protected YieldDispatchHeadNode dispatchNode;

    public YieldingHashCoreMethodNode(RubyContext context, SourceSection sourceSection) {
        super(context, sourceSection);
        dispatchNode = new YieldDispatchHeadNode(context);
    }

    public YieldingHashCoreMethodNode(YieldingHashCoreMethodNode prev) {
        super(prev);
        dispatchNode = prev.dispatchNode;
    }

    public Object yield(VirtualFrame frame, RubyProc block, Object... arguments) {
        return dispatchNode.dispatch(frame, block, arguments);
    }

}
