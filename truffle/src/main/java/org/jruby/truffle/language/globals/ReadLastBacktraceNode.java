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

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.source.SourceSection;
import org.jcodings.specific.UTF8Encoding;
import org.jruby.truffle.RubyContext;
import org.jruby.truffle.language.RubyNode;
import org.jruby.truffle.language.dispatch.CallDispatchHeadNode;
import org.jruby.truffle.language.dispatch.DispatchHeadNodeFactory;

public class ReadLastBacktraceNode extends RubyNode {

    @Child private ReadThreadLocalGlobalVariableNode getLastExceptionNode;
    @Child private CallDispatchHeadNode getBacktraceNode;

    public ReadLastBacktraceNode(RubyContext context, SourceSection sourceSection) {
        super(context, sourceSection);
        getLastExceptionNode = new ReadThreadLocalGlobalVariableNode(context, sourceSection, "$!");
        getBacktraceNode = DispatchHeadNodeFactory.createMethodCall(getContext());
    }

    @Override
    public Object isDefined(VirtualFrame frame) {
        return create7BitString("global-variable", UTF8Encoding.INSTANCE);
    }

    @Override
    public Object execute(VirtualFrame frame) {
        final Object lastException = getLastExceptionNode.execute(frame);

        if (lastException == nil()) {
            return nil();
        }

        return getBacktraceNode.call(frame, lastException, "backtrace", null);
    }
}
