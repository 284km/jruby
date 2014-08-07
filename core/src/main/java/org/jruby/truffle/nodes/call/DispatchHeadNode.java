/*
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.nodes.call;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.frame.*;
import org.jruby.truffle.nodes.RubyNode;
import org.jruby.truffle.runtime.*;
import org.jruby.truffle.runtime.core.*;

/**
 * The head of a chain of dispatch nodes. Can be used with {@link RubyCallNode} or on its own.
 */
public class DispatchHeadNode extends DispatchNode {

    private final String name;
    private final boolean isSplatted;

    public static enum MissingBehavior {
        RETURN_MISSING,
        CALL_METHOD_MISSING
    }

    public static final Object MISSING = new Object();

    @Child protected UnboxedDispatchNode dispatch;

    public DispatchHeadNode(RubyContext context, String name, boolean isSplatted, MissingBehavior missingBehavior) {
        super(context);

        assert context != null;
        assert name != null;

        this.name = name;
        this.isSplatted = isSplatted;

        final UninitializedDispatchNode uninitializedDispatch = new UninitializedDispatchNode(context, name, missingBehavior);
        dispatch = new UninitializedBoxingDispatchNode(context, uninitializedDispatch);
    }


    public Object dispatch(VirtualFrame frame, Object receiverObject, RubyProc blockObject, Object... argumentsObjects) {
        return dispatch(frame, RubyArguments.getSelf(frame.getArguments()), receiverObject, blockObject, argumentsObjects);
    }

    public Object dispatch(VirtualFrame frame, Object callingSelf, Object receiverObject, RubyProc blockObject, Object... argumentsObjects) {
        assert RubyContext.shouldObjectBeVisible(receiverObject);
        assert RubyContext.shouldObjectsBeVisible(argumentsObjects);

        return dispatch.dispatch(frame, callingSelf, receiverObject, blockObject, argumentsObjects);
    }

    public boolean doesRespondTo(VirtualFrame frame, Object receiverObject) {
        return dispatch.doesRespondTo(frame, receiverObject);
    }

    /**
     * Replace the entire dispatch chain with a fresh chain. Used when the situation has changed in
     * such a significant way that it's best to start again rather than add new specializations to
     * the chain. Used for example when methods appear to have been monkey-patched.
     */
    public Object respecialize(VirtualFrame frame, String reason, Object receiverObject, RubyProc blockObject, Object... argumentObjects) {
        CompilerAsserts.neverPartOfCompilation();

        final DispatchHeadNode newHead = new DispatchHeadNode(getContext(), name, isSplatted, MissingBehavior.CALL_METHOD_MISSING);
        replace(newHead, reason);
        return newHead.dispatch(frame, receiverObject, blockObject, argumentObjects);
    }

    public boolean respecializeAndDoesRespondTo(VirtualFrame frame, String reason, Object receiverObject) {
        CompilerAsserts.neverPartOfCompilation();

        final DispatchHeadNode newHead = new DispatchHeadNode(getContext(), name, isSplatted, MissingBehavior.CALL_METHOD_MISSING);
        replace(newHead, reason);
        return newHead.doesRespondTo(frame, receiverObject);
    }

    public UnboxedDispatchNode getDispatch() {
        return dispatch;
    }

    public String getName() {
        return name;
    }

}
