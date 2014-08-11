package org.jruby.truffle.nodes.call;

import com.oracle.truffle.api.Assumption;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.dsl.Generic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.DirectCallNode;
import com.oracle.truffle.api.nodes.InvalidAssumptionException;
import com.oracle.truffle.api.utilities.BranchProfile;
import org.jruby.truffle.runtime.RubyArguments;
import org.jruby.truffle.runtime.RubyContext;
import org.jruby.truffle.runtime.core.RubyBasicObject;
import org.jruby.truffle.runtime.core.RubyProc;
import org.jruby.truffle.runtime.methods.RubyMethod;

/**
 * Created by mg on 8/7/14.
 */
public abstract class NewCachedBooleanDispatchNode extends NewCachedDispatchNode {

    private final Assumption falseUnmodifiedAssumption;
    private final RubyMethod falseMethod;
    private final BranchProfile falseProfile = new BranchProfile();
    @Child protected DirectCallNode falseCall;

    private final Assumption trueUnmodifiedAssumption;
    private final RubyMethod trueMethod;
    private final BranchProfile trueProfile = new BranchProfile();
    @Child protected DirectCallNode trueCall;



    public NewCachedBooleanDispatchNode(RubyContext context, NewDispatchNode next, Assumption falseUnmodifiedAssumption, RubyMethod falseMethod, Assumption trueUnmodifiedAssumption,
                                        RubyMethod trueMethod) {
        super(context, next);
        assert falseUnmodifiedAssumption != null;
        assert falseMethod != null;
        assert trueUnmodifiedAssumption != null;
        assert trueMethod != null;

        this.falseUnmodifiedAssumption = falseUnmodifiedAssumption;
        this.falseMethod = falseMethod;
        falseCall = Truffle.getRuntime().createDirectCallNode(falseMethod.getCallTarget());

        this.trueUnmodifiedAssumption = trueUnmodifiedAssumption;
        this.trueMethod = trueMethod;
        trueCall = Truffle.getRuntime().createDirectCallNode(trueMethod.getCallTarget());
    }

    public NewCachedBooleanDispatchNode(NewCachedBooleanDispatchNode prev) {
        this(prev.getContext(), prev.next, prev.falseUnmodifiedAssumption, prev.falseMethod, prev.trueUnmodifiedAssumption, prev.trueMethod);
    }


    @Specialization
    public Object dispatch(VirtualFrame frame, Object boxedCallingSelf, boolean receiverObject, Object blockObject, Object argumentsObjects) {
        return doDispatch(frame, receiverObject, CompilerDirectives.unsafeCast(blockObject, RubyProc.class, true, false), CompilerDirectives.unsafeCast(argumentsObjects, Object[].class, true, true));
    }


    private Object doDispatch(VirtualFrame frame, boolean receiverObject, RubyProc blockObject, Object[] argumentsObjects) {
        if ((boolean) receiverObject) {
            trueProfile.enter();

            try {
                trueUnmodifiedAssumption.check();
            } catch (InvalidAssumptionException e) {
                return respecialize("class modified", frame, receiverObject, blockObject, argumentsObjects);
            }

            return trueCall.call(frame, RubyArguments.pack(trueMethod.getDeclarationFrame(), receiverObject, blockObject, argumentsObjects));
        } else {
            falseProfile.enter();

            try {
                falseUnmodifiedAssumption.check();
            } catch (InvalidAssumptionException e) {
                return respecialize("class modified", frame, receiverObject, blockObject, argumentsObjects);
            }

            return falseCall.call(frame, RubyArguments.pack(falseMethod.getDeclarationFrame(), receiverObject, blockObject, argumentsObjects));
        }
    }

    @Generic
    public Object dispatch(VirtualFrame frame, Object callingSelf, Object receiverObject, Object blockObject, Object argumentsObjects) {
        return next.executeDispatch(frame, callingSelf, receiverObject, blockObject, argumentsObjects);
    }


}