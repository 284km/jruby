/*
 * Copyright (c) 2015, 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */

package org.jruby.truffle.language.globals;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.source.SourceSection;
import org.jruby.truffle.RubyContext;
import org.jruby.truffle.language.RubyNode;
import org.jruby.truffle.language.control.RaiseException;
import org.jruby.truffle.language.dispatch.CallDispatchHeadNode;
import org.jruby.truffle.language.dispatch.DispatchHeadNodeFactory;

public class UpdateLastBacktraceNode extends RubyNode {

    @Child private RubyNode child;
    @Child private ReadThreadLocalGlobalVariableNode getLastExceptionNode;
    @Child private CallDispatchHeadNode setBacktraceNode;

    public UpdateLastBacktraceNode(RubyContext context, SourceSection sourceSection, RubyNode child) {
        super(context, sourceSection);
        this.child = child;
        getLastExceptionNode = new ReadThreadLocalGlobalVariableNode(context, sourceSection, "$!");
        setBacktraceNode = DispatchHeadNodeFactory.createMethodCall(getContext());
    }

    @Override
    public Object execute(VirtualFrame frame) {
        final Object lastException = getLastExceptionNode.execute(frame);

        if (lastException == nil()) {
            CompilerDirectives.transferToInterpreter();
            throw new RaiseException(getContext().getCoreLibrary().argumentError("$! is not set", this));
        }

        final Object newBacktrace = child.execute(frame);
        setBacktraceNode.call(frame, lastException, "set_backtrace", null, newBacktrace);

        return newBacktrace;
    }
}
