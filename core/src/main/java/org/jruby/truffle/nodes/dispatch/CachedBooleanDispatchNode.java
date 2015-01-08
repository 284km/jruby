/*
 * Copyright (c) 2014, 2015 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.nodes.dispatch;

import com.oracle.truffle.api.Assumption;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.DirectCallNode;
import com.oracle.truffle.api.nodes.IndirectCallNode;
import com.oracle.truffle.api.nodes.InvalidAssumptionException;
import com.oracle.truffle.api.utilities.BranchProfile;
import org.jruby.truffle.runtime.RubyArguments;
import org.jruby.truffle.runtime.RubyContext;
import org.jruby.truffle.runtime.core.RubyProc;
import org.jruby.truffle.runtime.methods.RubyMethod;

public abstract class CachedBooleanDispatchNode extends CachedDispatchNode {

    private final Assumption falseUnmodifiedAssumption;
    private final RubyMethod falseMethod;
    private final BranchProfile falseProfile = BranchProfile.create();

    private final Object falseValue;
    @Child private DirectCallNode falseCallDirect;

    private final Assumption trueUnmodifiedAssumption;
    private final RubyMethod trueMethod;
    private final BranchProfile trueProfile = BranchProfile.create();

    private final Object trueValue;
    @Child private DirectCallNode trueCallDirect;

    @Child private IndirectCallNode indirectCallNode;

    public CachedBooleanDispatchNode(
            RubyContext context,
            Object cachedName,
            DispatchNode next,
            Assumption falseUnmodifiedAssumption,
            Object falseValue,
            RubyMethod falseMethod,
            Assumption trueUnmodifiedAssumption,
            Object trueValue,
            RubyMethod trueMethod,
            boolean indirect,
            DispatchAction dispatchAction) {
        super(context, cachedName, next, indirect, dispatchAction);

        this.falseUnmodifiedAssumption = falseUnmodifiedAssumption;
        this.falseMethod = falseMethod;
        this.falseValue = falseValue;

        if (falseMethod != null) {
            if (!indirect) {
                falseCallDirect = Truffle.getRuntime().createDirectCallNode(falseMethod.getCallTarget());

                if (falseCallDirect.isCallTargetCloningAllowed() && falseMethod.getSharedMethodInfo().shouldAlwaysSplit()) {
                    insert(falseCallDirect);
                    falseCallDirect.cloneCallTarget();
                }
            }
        }

        this.trueUnmodifiedAssumption = trueUnmodifiedAssumption;
        this.trueMethod = trueMethod;
        this.trueValue = trueValue;

        if (trueMethod != null) {
            if (!indirect) {
                trueCallDirect = Truffle.getRuntime().createDirectCallNode(trueMethod.getCallTarget());

                if (trueCallDirect.isCallTargetCloningAllowed() && trueMethod.getSharedMethodInfo().shouldAlwaysSplit()) {
                    insert(trueCallDirect);
                    trueCallDirect.cloneCallTarget();
                }
            }
        }

        if (indirect) {
            indirectCallNode = Truffle.getRuntime().createIndirectCallNode();
        }
    }

    public CachedBooleanDispatchNode(CachedBooleanDispatchNode prev) {
        super(prev);
        falseUnmodifiedAssumption = prev.falseUnmodifiedAssumption;
        falseMethod = prev.falseMethod;
        falseValue = prev.falseValue;
        falseCallDirect = prev.falseCallDirect;
        trueUnmodifiedAssumption = prev.trueUnmodifiedAssumption;
        trueValue = prev.trueValue;
        trueMethod = prev.trueMethod;
        trueCallDirect = prev.trueCallDirect;
        indirectCallNode = prev.indirectCallNode;
    }

    @Specialization(guards = "guardName")
    public Object dispatch(
            VirtualFrame frame,
            boolean receiverObject,
            Object methodName,
            Object blockObject,
            Object argumentsObjects) {
        final DispatchAction dispatchAction = getDispatchAction();

        if (receiverObject) {
            trueProfile.enter();

            try {
                trueUnmodifiedAssumption.check();
            } catch (InvalidAssumptionException e) {
                return resetAndDispatch(
                        frame,
                        receiverObject,
                        methodName,
                        CompilerDirectives.unsafeCast(blockObject, RubyProc.class, true, false),
                        argumentsObjects,
                        "class modified");
            }

            if (dispatchAction == DispatchAction.CALL_METHOD) {
                if (isIndirect()) {
                    return indirectCallNode.call(
                            frame,
                            trueMethod.getCallTarget(),
                            RubyArguments.pack(
                                    trueMethod,
                                    trueMethod.getDeclarationFrame(),
                                    receiverObject,
                                    CompilerDirectives.unsafeCast(blockObject, RubyProc.class, true, false),
                                    CompilerDirectives.unsafeCast(argumentsObjects, Object[].class, true)));
                } else {
                    return trueCallDirect.call(
                            frame,
                            RubyArguments.pack(
                                    trueMethod,
                                    trueMethod.getDeclarationFrame(),
                                    receiverObject,
                                    CompilerDirectives.unsafeCast(blockObject, RubyProc.class, true, false),
                                    CompilerDirectives.unsafeCast(argumentsObjects, Object[].class, true)));
                }
            } else if (dispatchAction == DispatchAction.RESPOND_TO_METHOD) {
                return true;
            } else if (dispatchAction == DispatchAction.READ_CONSTANT) {
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
                        receiverObject,
                        methodName,
                        CompilerDirectives.unsafeCast(blockObject, RubyProc.class, true, false),
                        argumentsObjects,
                        "class modified");
            }

            if (dispatchAction == DispatchAction.CALL_METHOD) {
                if (isIndirect()) {
                    return indirectCallNode.call(
                            frame,
                            falseMethod.getCallTarget(),
                            RubyArguments.pack(
                                    falseMethod,
                                    falseMethod.getDeclarationFrame(),
                                    receiverObject,
                                    CompilerDirectives.unsafeCast(blockObject, RubyProc.class, true, false),
                                    CompilerDirectives.unsafeCast(argumentsObjects, Object[].class, true)));
                } else {
                    return falseCallDirect.call(
                            frame,
                            RubyArguments.pack(
                                    falseMethod,
                                    falseMethod.getDeclarationFrame(),
                                    receiverObject,
                                    CompilerDirectives.unsafeCast(blockObject, RubyProc.class, true, false),
                                    CompilerDirectives.unsafeCast(argumentsObjects, Object[].class, true)));
                }
            } else if (dispatchAction == DispatchAction.RESPOND_TO_METHOD) {
                return true;
            } else if (dispatchAction == DispatchAction.READ_CONSTANT) {
                return falseValue;
            } else {
                throw new UnsupportedOperationException();
            }
        }
    }

    @Fallback
    public Object dispatch(
            VirtualFrame frame,
            Object receiverObject,
            Object methodName,
            Object blockObject,
            Object argumentsObjects) {
        return next.executeDispatch(
                frame,
                receiverObject,
                methodName,
                blockObject,
                argumentsObjects);
    }

}
