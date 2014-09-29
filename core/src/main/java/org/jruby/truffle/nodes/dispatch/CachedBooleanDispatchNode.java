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

import com.oracle.truffle.api.Assumption;
import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.DirectCallNode;
import com.oracle.truffle.api.nodes.InvalidAssumptionException;
import com.oracle.truffle.api.utilities.BranchProfile;
import org.jruby.truffle.runtime.NilPlaceholder;
import org.jruby.truffle.runtime.RubyArguments;
import org.jruby.truffle.runtime.RubyContext;
import org.jruby.truffle.runtime.core.RubyProc;
import org.jruby.truffle.runtime.methods.RubyMethod;

public abstract class CachedBooleanDispatchNode extends CachedDispatchNode {

    private final Assumption falseUnmodifiedAssumption;
    private final RubyMethod falseMethod;
    private final BranchProfile falseProfile = new BranchProfile();

    private final Object falseValue;
    @Child protected DirectCallNode falseCall;

    private final Assumption trueUnmodifiedAssumption;
    private final RubyMethod trueMethod;
    private final BranchProfile trueProfile = new BranchProfile();

    private final Object trueValue;
    @Child protected DirectCallNode trueCall;

    public CachedBooleanDispatchNode(
            RubyContext context, Object cachedName, DispatchNode next,
            Assumption falseUnmodifiedAssumption, Object falseValue, RubyMethod falseMethod,
            Assumption trueUnmodifiedAssumption, Object trueValue, RubyMethod trueMethod) {
        super(context, cachedName, next);

        this.falseUnmodifiedAssumption = falseUnmodifiedAssumption;
        this.falseMethod = falseMethod;
        this.falseValue = falseValue;

        if (falseMethod != null) {
            falseCall = Truffle.getRuntime().createDirectCallNode(falseMethod.getCallTarget());
        }

        this.trueUnmodifiedAssumption = trueUnmodifiedAssumption;
        this.trueMethod = trueMethod;
        this.trueValue = trueValue;

        if (trueMethod != null) {
            trueCall = Truffle.getRuntime().createDirectCallNode(trueMethod.getCallTarget());
        }
    }

    public CachedBooleanDispatchNode(CachedBooleanDispatchNode prev) {
        super(prev);
        falseUnmodifiedAssumption = prev.falseUnmodifiedAssumption;
        falseMethod = prev.falseMethod;
        falseValue = prev.falseValue;
        falseCall = prev.falseCall;
        trueUnmodifiedAssumption = prev.trueUnmodifiedAssumption;
        trueValue = prev.trueValue;
        trueMethod = prev.trueMethod;
        trueCall = prev.trueCall;
    }

    @Specialization(guards = "guardName")
    public Object dispatch(
            VirtualFrame frame,
            NilPlaceholder methodReceiverObject,
            Object callingSelf,
            boolean receiverObject,
            Object methodName,
            Object blockObject,
            Object argumentsObjects,
            Dispatch.DispatchAction dispatchAction) {
        CompilerAsserts.compilationConstant(dispatchAction);

        if (receiverObject) {
            trueProfile.enter();

            try {
                trueUnmodifiedAssumption.check();
            } catch (InvalidAssumptionException e) {
                return resetAndDispatch(
                        frame,
                        methodReceiverObject,
                        callingSelf,
                        receiverObject,
                        methodName,
                        CompilerDirectives.unsafeCast(blockObject, RubyProc.class, true, false),
                        argumentsObjects,
                        dispatchAction,
                        "class modified");
            }

            if (dispatchAction == Dispatch.DispatchAction.CALL) {
                return trueCall.call(
                        frame,
                        RubyArguments.pack(
                                trueMethod,
                                trueMethod.getDeclarationFrame(),
                                receiverObject,
                                CompilerDirectives.unsafeCast(blockObject, RubyProc.class, true, false),
                                CompilerDirectives.unsafeCast(argumentsObjects, Object[].class, true)));
            } else if (dispatchAction == Dispatch.DispatchAction.RESPOND) {
                return true;
            } else if (dispatchAction == Dispatch.DispatchAction.READ_CONSTANT) {
                return trueValue;
            } else {
                throw new UnsupportedOperationException();
            }
        } else {
            falseProfile.enter();

            try {
                falseUnmodifiedAssumption.check();
            } catch (InvalidAssumptionException e) {
                return resetAndDispatch(
                        frame,
                        methodReceiverObject,
                        callingSelf,
                        receiverObject,
                        methodName,
                        CompilerDirectives.unsafeCast(blockObject, RubyProc.class, true, false),
                        argumentsObjects,
                        dispatchAction,
                        "class modified");
            }

            if (dispatchAction == Dispatch.DispatchAction.CALL) {
                return falseCall.call(
                        frame,
                        RubyArguments.pack(
                                falseMethod,
                                falseMethod.getDeclarationFrame(),
                                receiverObject,
                                CompilerDirectives.unsafeCast(blockObject, RubyProc.class, true, false),
                                CompilerDirectives.unsafeCast(argumentsObjects, Object[].class, true)));
            } else if (dispatchAction == Dispatch.DispatchAction.RESPOND) {
                return true;
            } else if (dispatchAction == Dispatch.DispatchAction.READ_CONSTANT) {
                return falseValue;
            } else {
                throw new UnsupportedOperationException();
            }
        }
    }

    @Fallback
    public Object dispatch(
            VirtualFrame frame,
            Object methodReceiverObject,
            Object callingSelf,
            Object receiverObject,
            Object methodName,
            Object blockObject,
            Object argumentsObjects,
            Dispatch.DispatchAction dispatchAction) {
        return next.executeDispatch(
                frame,
                methodReceiverObject,
                callingSelf,
                receiverObject,
                methodName,
                blockObject,
                argumentsObjects,
                dispatchAction);
    }

}