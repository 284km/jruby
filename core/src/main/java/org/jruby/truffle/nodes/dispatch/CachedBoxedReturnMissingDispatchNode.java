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
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.InvalidAssumptionException;
import org.jruby.truffle.nodes.RubyNode;
import org.jruby.truffle.runtime.NilPlaceholder;
import org.jruby.truffle.runtime.RubyContext;
import org.jruby.truffle.runtime.core.RubyBasicObject;
import org.jruby.truffle.runtime.core.RubyProc;
import org.jruby.truffle.runtime.lookup.LookupNode;

public abstract class CachedBoxedReturnMissingDispatchNode extends CachedDispatchNode {

    private final LookupNode expectedLookupNode;
    private final Assumption unmodifiedAssumption;


    public CachedBoxedReturnMissingDispatchNode(RubyContext context, Object cachedName, DispatchNode next, LookupNode expectedLookupNode) {
        super(context, cachedName, next);
        assert expectedLookupNode != null;
        this.expectedLookupNode = expectedLookupNode;
        unmodifiedAssumption = expectedLookupNode.getUnmodifiedAssumption();
        this.next = next;
    }

    public CachedBoxedReturnMissingDispatchNode(CachedBoxedReturnMissingDispatchNode prev) {
        super(prev);
        expectedLookupNode = prev.expectedLookupNode;
        unmodifiedAssumption = prev.unmodifiedAssumption;
    }

    @Specialization(guards = {"guardName"})
    public Object dispatch(VirtualFrame frame, NilPlaceholder methodReceiverObject, Object boxedCallingSelf, RubyBasicObject receiverObject, Object methodName, Object blockObject, Object argumentsObjects, DispatchHeadNode.DispatchAction dispatchAction) {
        // Check the lookup node is what we expect

        if (receiverObject.getLookupNode() != expectedLookupNode) {
            return doNext(frame, methodReceiverObject, boxedCallingSelf, receiverObject, methodName, CompilerDirectives.unsafeCast(blockObject, RubyProc.class, true, false), argumentsObjects, dispatchAction);
        }
        return doDispatch(frame, methodReceiverObject, boxedCallingSelf, receiverObject, methodName, CompilerDirectives.unsafeCast(blockObject, RubyProc.class, true, false), CompilerDirectives.unsafeCast(argumentsObjects, Object[].class, true, true), dispatchAction);

    }

    private Object doDispatch(VirtualFrame frame, Object methodReceiverObject, Object boxedCallingSelf, RubyBasicObject receiverObject, Object methodName, RubyProc blockObject, Object[] argumentsObjects, DispatchHeadNode.DispatchAction dispatchAction) {
        RubyNode.notDesignedForCompilation();
        // Check the class has not been modified

        try {
            unmodifiedAssumption.check();
        } catch (InvalidAssumptionException e) {
            return resetAndDispatch("class modified", frame, methodReceiverObject, boxedCallingSelf, receiverObject, methodName, blockObject, argumentsObjects, dispatchAction);
        }

        if (dispatchAction == DispatchHeadNode.DispatchAction.CALL) {
            return DispatchHeadNode.MISSING;
        } else if (dispatchAction == DispatchHeadNode.DispatchAction.RESPOND) {
            return false;
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Fallback
    public Object dispatch(VirtualFrame frame, Object methodReceiverObject, Object callingSelf, Object receiverObject, Object methodName, Object blockObject, Object argumentsObjects, DispatchHeadNode.DispatchAction dispatchAction) {
        return doNext(frame, methodReceiverObject, callingSelf, receiverObject, methodName, CompilerDirectives.unsafeCast(blockObject, RubyProc.class, true, false), argumentsObjects, dispatchAction);
    }

    private Object doNext(VirtualFrame frame, Object methodReceiverObject, Object callingSelf, Object receiverObject, Object methodName, RubyProc blockObject, Object argumentsObjects, DispatchHeadNode.DispatchAction dispatchAction) {
        return next.executeDispatch(frame, methodReceiverObject, callingSelf, receiverObject, methodName, blockObject, argumentsObjects, dispatchAction);
    }
}