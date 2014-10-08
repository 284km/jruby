/*
 * Copyright (c) 2014 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.nodes.dispatch;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.NodeUtil;
import org.jruby.truffle.nodes.RubyNode;
import org.jruby.truffle.runtime.ModuleOperations;
import org.jruby.truffle.runtime.RubyConstant;
import org.jruby.truffle.runtime.RubyContext;
import org.jruby.truffle.runtime.control.RaiseException;
import org.jruby.truffle.runtime.core.*;
import org.jruby.truffle.runtime.methods.RubyMethod;

@NodeChildren({
        @NodeChild(value="methodReceiverObject", type=Node.class),
        @NodeChild(value="callingSelf", type=Node.class),
        @NodeChild(value="receiver", type=Node.class),
        @NodeChild(value="methodName", type=Node.class),
        @NodeChild(value="blockObject", type=Node.class),
        @NodeChild(value="arguments", type=Node.class),
        @NodeChild(value="action", type=Node.class)})
public abstract class DispatchNode extends RubyNode {

    public DispatchNode(RubyContext context) {
        super(context, null);
    }

    public DispatchNode(DispatchNode prev) {
        this(prev.getContext());
    }

    public abstract Object executeDispatch(
            VirtualFrame frame,
            Object methodReceiverObject,
            Object callingSelf,
            Object receiverObject,
            Object methodName,
            Object blockObject,
            Object argumentsObjects,
            Dispatch.DispatchAction dispatchAction);

    protected RubyConstant lookupConstant(
            RubyBasicObject callingSelf,
            RubyBasicObject receiver,
            String name,
            boolean ignoreVisibility,
            Dispatch.DispatchAction dispatchAction) {
        CompilerAsserts.neverPartOfCompilation();

        RubyConstant constant;

        constant = ModuleOperations.lookupConstant(receiver.getMetaClass(), name);

        if (constant == null && receiver instanceof RubyModule) {
            /*
             * FIXME(CS): I'm obviously doing something wrong with constant lookup in nested modules
             * here, but explicitly looking in the Module itself, not its lookup node, seems to fix
             * it for now.
             */

            constant =  ModuleOperations.lookupConstant((RubyModule) receiver, name);
        }

        return constant;
    }

    protected RubyMethod lookup(
            RubyBasicObject callingSelf,
            RubyBasicObject receiver,
            String name,
            boolean ignoreVisibility,
            Dispatch.DispatchAction dispatchAction) {
        CompilerAsserts.neverPartOfCompilation();

        RubyMethod method = ModuleOperations.lookupMethod(receiver.getMetaClass(), name);

        // If no method was found, use #method_missing

        if (method == null) {
            return null;
        }

        // Check for methods that are explicitly undefined

        if (method.isUndefined()) {
            throw new RaiseException(getContext().getCoreLibrary().noMethodError(name, receiver.toString(), this));
        }

        // Check visibility

        if (callingSelf == receiver.getLogicalClass()){
            return method;
        }

        if (!ignoreVisibility && !method.isVisibleTo(this, callingSelf)) {
            if (dispatchAction == Dispatch.DispatchAction.CALL_METHOD) {
                throw new RaiseException(getContext().getCoreLibrary().privateMethodError(name, receiver.toString(), this));
            } else if (dispatchAction == Dispatch.DispatchAction.RESPOND_TO_METHOD) {
                return null;
            } else {
                throw new UnsupportedOperationException();
            }
        }

        return method;
    }

    protected Object resetAndDispatch(
            VirtualFrame frame,
            Object methodReceiverObject,
            Object callingSelf,
            Object receiverObject,
            Object methodName,
            RubyProc blockObject,
            Object argumentsObjects,
            Dispatch.DispatchAction dispatchAction,
            String reason) {
        final DispatchHeadNode head = getHeadNode();
        head.reset(reason);
        return head.dispatch(
                frame,
                methodReceiverObject,
                callingSelf,
                receiverObject,
                methodName,
                blockObject,
                argumentsObjects,
                dispatchAction);
    }

    protected DispatchHeadNode getHeadNode() {
        return NodeUtil.findParent(this, DispatchHeadNode.class);
    }

    public final Object execute(VirtualFrame frame) {
        throw new IllegalStateException("do not call execute on dispatch nodes");
    }

}
