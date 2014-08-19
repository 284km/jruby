/*
 * Copyright (c) 2014 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.nodes.methods;

import com.oracle.truffle.api.SourceSection;
import com.oracle.truffle.api.frame.VirtualFrame;
import org.jruby.truffle.nodes.RubyNode;
import org.jruby.truffle.nodes.call.DispatchHeadNode;
import org.jruby.truffle.runtime.RubyArguments;
import org.jruby.truffle.runtime.RubyContext;

import java.util.Arrays;

public class SymbolProcNode extends RubyNode {

    @Child protected DispatchHeadNode dispatch;

    public SymbolProcNode(RubyContext context, SourceSection sourceSection, String symbol) {
        super(context, sourceSection);
        dispatch = new DispatchHeadNode(context, symbol, false, DispatchHeadNode.MissingBehavior.CALL_METHOD_MISSING);
    }

    @Override
    public Object execute(VirtualFrame frame) {
        notDesignedForCompilation();

        final Object[] args = frame.getArguments();
        final Object receiver = RubyArguments.getUserArgument(args, 0);
        final Object[] arguments = RubyArguments.extractUserArguments(args);
        final Object[] sendArgs = Arrays.copyOfRange(arguments, 1, arguments.length);
        return dispatch.dispatch(frame, receiver, RubyArguments.getBlock(args), sendArgs);
    }

}
