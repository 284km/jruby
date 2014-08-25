/*
 * Copyright (c) 2014 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.nodes.respondto;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.source.SourceSection;
import org.jruby.truffle.nodes.RubyNode;
import org.jruby.truffle.nodes.dispatch.Dispatch;
import org.jruby.truffle.nodes.dispatch.DispatchHeadNode;
import org.jruby.truffle.runtime.RubyContext;

public class RespondToNode extends RubyNode {

    private final String methodName;

    @Child protected RubyNode child;
    @Child protected DispatchHeadNode dispatch;

    public RespondToNode(RubyContext context, SourceSection sourceSection, RubyNode child, String methodName) {
        super(context, sourceSection);
        this.methodName = methodName;
        this.child = child;
        dispatch = new DispatchHeadNode(context, false, Dispatch.MissingBehavior.RETURN_MISSING);
    }

    public boolean executeBoolean(VirtualFrame frame) {
        return dispatch.doesRespondTo(frame, methodName, child.execute(frame));
    }

    @Override
    public Object execute(VirtualFrame frame) {
        return executeBoolean(frame);
    }

}
