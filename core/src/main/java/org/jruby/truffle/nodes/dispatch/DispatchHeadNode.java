/*
 * Copyright (c) 2013, 2014 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.nodes.dispatch;

import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.nodes.Node;
import org.jruby.truffle.runtime.*;
import org.jruby.truffle.runtime.core.*;

public class DispatchHeadNode extends Node {

    private final RubyContext context;
    private final boolean ignoreVisibility;
    private final Dispatch.MissingBehavior missingBehavior;

    @Child protected DispatchNode first;

    public DispatchHeadNode(RubyContext context) {
        this(context, false, Dispatch.MissingBehavior.CALL_METHOD_MISSING);
    }

    public DispatchHeadNode(RubyContext context, boolean ignoreVisibility, Dispatch.MissingBehavior missingBehavior) {
        this.context = context;
        this.ignoreVisibility = ignoreVisibility;
        this.missingBehavior = missingBehavior;
        first = new UnresolvedDispatchNode(context, ignoreVisibility, missingBehavior);
    }

    public Object call(
            VirtualFrame frame,
            Object receiverObject,
            Object methodName,
            RubyProc blockObject,
            Object... argumentsObjects) {
        return dispatch(
                frame,
                NilPlaceholder.INSTANCE,
                RubyArguments.getSelf(frame.getArguments()),
                receiverObject,
                methodName,
                blockObject,
                argumentsObjects,
                Dispatch.DispatchAction.CALL);
    }

    public boolean doesRespondTo(
            VirtualFrame frame,
            Object methodName,
            Object receiverObject) {
        return (boolean) dispatch(
                frame,
                NilPlaceholder.INSTANCE,
                RubyArguments.getSelf(frame.getArguments()),
                receiverObject,
                methodName,
                null,
                null,
                Dispatch.DispatchAction.RESPOND);
    }

    public Object dispatch(
            VirtualFrame frame,
            Object methodReceiverObject,
            Object callingSelf,
            Object receiverObject,
            Object methodName,
            Object blockObject,
            Object argumentsObjects,
            Dispatch.DispatchAction dispatchAction) {
        return first.executeDispatch(
                frame,
                methodReceiverObject,
                callingSelf,
                receiverObject,
                methodName,
                blockObject,
                argumentsObjects,
                dispatchAction);
    }

    public void reset(String reason) {
        first.replace(new UnresolvedDispatchNode(context, ignoreVisibility, missingBehavior), reason);
    }

    public DispatchNode getFirstDispatchNode() {
        return first;
    }

}
