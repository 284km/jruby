/*
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.nodes.dispatch;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.nodes.Node;
import org.jruby.truffle.runtime.*;
import org.jruby.truffle.runtime.control.RaiseException;
import org.jruby.truffle.runtime.core.*;
import org.jruby.truffle.runtime.methods.RubyMethod;

/**
 * The head of a chain of dispatch nodes. Can be used with {@link org.jruby.truffle.nodes.RubyCallNode} or on its own.
 */
public class DispatchHeadNode extends Node {

    private final RubyContext context;
    private final boolean ignoreVisibility;
    private final String cachedMethodName;

    public static enum MissingBehavior {
        RETURN_MISSING,
        CALL_METHOD_MISSING
    }

    public static enum DispatchAction {
        DISPATCH,
        RESPOND
    }

    public static final Object MISSING = new Object();

    @Child protected DispatchNode newDispatch;

    public DispatchHeadNode(RubyContext context, String name, MissingBehavior missingBehavior) {
        this(context, false, name, missingBehavior);
    }

    public DispatchHeadNode(RubyContext context, boolean ignoreVisibility, String cachedMethodName, MissingBehavior missingBehavior) {
        this.context = context;
        this.ignoreVisibility = ignoreVisibility;
        this.cachedMethodName = cachedMethodName;
        newDispatch = new UnresolvedDispatchNode(context, ignoreVisibility, missingBehavior);
    }

    public DispatchHeadNode(RubyContext context, boolean ignoreVisibility, MissingBehavior missingBehavior) {
        this(context, ignoreVisibility, null, missingBehavior);
    }

    public Object dispatch(VirtualFrame frame, Object receiverObject, RubyProc blockObject, Object... argumentsObjects) {
        return dispatch(frame, NilPlaceholder.INSTANCE, RubyArguments.getSelf(frame.getArguments()), receiverObject, blockObject, argumentsObjects);
    }

    public Object dispatch(VirtualFrame frame, Object callingSelf, Object receiverObject, RubyProc blockObject, Object... argumentsObjects) {
        return dispatch(frame, NilPlaceholder.INSTANCE, callingSelf, receiverObject, blockObject, argumentsObjects);
    }

    public Object dispatch(VirtualFrame frame, Object methodReceiverObject, Object callingSelf, Object receiverObject, RubyProc blockObject, Object... argumentsObjects) {
        return dispatch(frame, methodReceiverObject, callingSelf, receiverObject, cachedMethodName, blockObject, argumentsObjects);
    }

    public Object dispatch(VirtualFrame frame, Object methodReceiverObject, Object callingSelf, Object receiverObject, String methodName, RubyProc blockObject, Object... argumentsObjects) {
        return newDispatch.executeDispatch(frame, methodReceiverObject, callingSelf, receiverObject, methodName, blockObject, argumentsObjects, DispatchAction.DISPATCH);
    }

    public Object dispatch(VirtualFrame frame, Object methodReceiverObject, Object callingSelf, Object receiverObject, RubySymbol methodName, RubyProc blockObject, Object... argumentsObjects) {
        return newDispatch.executeDispatch(frame, methodReceiverObject, callingSelf, receiverObject, methodName, blockObject, argumentsObjects, DispatchAction.DISPATCH);
    }

    public Object dispatch(VirtualFrame frame, Object methodReceiverObject, Object callingSelf, Object receiverObject, RubyString methodName, RubyProc blockObject, Object... argumentsObjects) {
        return newDispatch.executeDispatch(frame, methodReceiverObject, callingSelf, receiverObject, methodName, blockObject, argumentsObjects, DispatchAction.DISPATCH);
    }

    public boolean doesRespondTo(VirtualFrame frame, Object receiverObject) {
        return doesRespondTo(frame, RubyArguments.getSelf(frame.getArguments()), cachedMethodName, receiverObject);
    }

    public boolean doesRespondTo(VirtualFrame frame, Object callingSelf, String methodName, Object receiverObject) {
        return (boolean) newDispatch.executeDispatch(frame, NilPlaceholder.INSTANCE, callingSelf, receiverObject, methodName, null, null, DispatchAction.RESPOND);
    }

    public boolean doesRespondTo(VirtualFrame frame, Object callingSelf, RubySymbol methodName, Object receiverObject) {
        return (boolean) newDispatch.executeDispatch(frame, NilPlaceholder.INSTANCE, callingSelf, receiverObject, methodName, null, null, DispatchAction.RESPOND);
    }

    public boolean doesRespondTo(VirtualFrame frame, Object callingSelf, RubyString methodName, Object receiverObject) {
        return (boolean) newDispatch.executeDispatch(frame, NilPlaceholder.INSTANCE, callingSelf, receiverObject, methodName, null, null, DispatchAction.RESPOND);
    }


    /**
     * Replace the entire dispatch chain with a fresh chain. Used when the situation has changed in
     * such a significant way that it's best to start again rather than add new specializations to
     * the chain. Used for example when methods appear to have been monkey-patched.
     */
    public Object respecialize(VirtualFrame frame, String reason, Object methodReceiverObject, Object callingSelf, Object receiverObject, Object methodName, RubyProc blockObject, Object[] argumentObjects, DispatchHeadNode.DispatchAction dispatchAction) {
        CompilerAsserts.neverPartOfCompilation();

        final DispatchHeadNode newHead = new DispatchHeadNode(getContext(), getIgnoreVisibility(), cachedMethodName, MissingBehavior.CALL_METHOD_MISSING);
        replace(newHead, reason);
        return newHead.newDispatch.executeDispatch(frame, methodReceiverObject, callingSelf, receiverObject, methodName, blockObject, argumentObjects, dispatchAction);
    }

    public String getName() {
        return cachedMethodName;
    }
    /**
     * Get the depth of this node in the dispatch chain. The first node below
     * {@link DispatchHeadNode} is at depth 1.
     */
    public int getDepth() {
        // TODO: can we use findParent instead?

        int depth = 1;
        Node parent = this.getParent();

        while (!(parent instanceof DispatchHeadNode)) {
            parent = parent.getParent();
            depth++;
        }

        return depth;
    }

    protected RubyMethod lookup(RubyBasicObject boxedCallingSelf, RubyBasicObject receiverBasicObject, String name) throws UseMethodMissingException {
        CompilerAsserts.neverPartOfCompilation();

        // TODO(CS): why are we using an exception to convey method missing here?

        RubyMethod method = receiverBasicObject.getLookupNode().lookupMethod(name);

        // If no method was found, use #method_missing

        if (method == null) {
            throw new UseMethodMissingException();
        }

        // Check for methods that are explicitly undefined

        if (method.isUndefined()) {
            throw new RaiseException(context.getCoreLibrary().noMethodError(name, receiverBasicObject.toString(), this));
        }

        // Check visibility

        if (boxedCallingSelf == receiverBasicObject.getRubyClass()){
            return method;
        }

        if (!ignoreVisibility && !method.isVisibleTo(this, boxedCallingSelf, receiverBasicObject)) {
            CompilerDirectives.transferToInterpreter();
            throw new RaiseException(context.getCoreLibrary().privateNoMethodError(name, receiverBasicObject.toString(), this));
        }

        return method;
    }

    public RubyContext getContext() {
        return context;
    }

    public boolean getIgnoreVisibility() { return ignoreVisibility; }

    public DispatchNode getNewDispatch() {
        return newDispatch;
    }

}
