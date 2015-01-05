package org.jruby.ir.instructions;

import org.jruby.RubyArray;
import org.jruby.ir.IRScope;
import org.jruby.ir.Operation;
import org.jruby.ir.operands.*;
import org.jruby.ir.operands.Float;
import org.jruby.ir.runtime.IRRuntimeHelpers;
import org.jruby.ir.transformations.inlining.CloneInfo;
import org.jruby.parser.StaticScope;
import org.jruby.runtime.*;
import org.jruby.runtime.builtin.IRubyObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.jruby.ir.IRFlags.*;

public abstract class CallBase extends Instr implements Specializeable, ClosureAcceptingInstr {
    private static long callSiteCounter = 1;

    public final long callSiteId;
    private final CallType callType;
    protected Operand   receiver;
    protected Operand[] arguments;
    protected Operand   closure;
    protected String name;
    protected CallSite callSite;

    private boolean flagsComputed;
    private boolean canBeEval;
    private boolean targetRequiresCallersBinding;    // Does this call make use of the caller's binding?
    private boolean targetRequiresCallersFrame;    // Does this call make use of the caller's frame?
    private boolean dontInline;
    private boolean containsArgSplat;
    private boolean procNew;

    protected CallBase(Operation op, CallType callType, String name, Operand receiver, Operand[] args, Operand closure) {
        super(op);

        this.callSiteId = callSiteCounter++;
        this.receiver = receiver;
        this.arguments = args;
        this.closure = closure;
        this.name = name;
        this.callType = callType;
        this.callSite = getCallSiteFor(callType, name);
        containsArgSplat = containsArgSplat(args);
        flagsComputed = false;
        canBeEval = true;
        targetRequiresCallersBinding = true;
        targetRequiresCallersFrame = true;
        dontInline = false;
        procNew = false;
    }

    @Override
    public Operand[] getOperands() {
        // -0 is not possible so we add 1 to arguments with closure so we get a valid negative value.
        Fixnum arity = new Fixnum(closure != null ? -1*(arguments.length + 1) : arguments.length);
        return buildAllArgs(new Fixnum(callType.ordinal()), receiver, arity, arguments, closure);
    }

    public String getName() {
        return name;
    }

    /** From interface ClosureAcceptingInstr */
    public Operand getClosureArg() {
        return closure;
    }

    public Operand getClosureArg(Operand ifUnspecified) {
        return closure == null ? ifUnspecified : closure;
    }

    public Operand getReceiver() {
        return receiver;
    }

    public Operand[] getCallArgs() {
        return arguments;
    }

    public CallSite getCallSite() {
        return callSite;
    }

    public CallType getCallType() {
        return callType;
    }

    public boolean containsArgSplat() {
        return containsArgSplat;
    }

    public boolean isProcNew() {
        return procNew;
    }

    public void setProcNew(boolean procNew) {
        this.procNew = procNew;
    }

    public void blockInlining() {
        dontInline = true;
    }

    public boolean inliningBlocked() {
        return dontInline;
    }

    private static CallSite getCallSiteFor(CallType callType, String name) {
        assert callType != null: "Calltype should never be null";

        switch (callType) {
            case NORMAL: return MethodIndex.getCallSite(name);
            case FUNCTIONAL: return MethodIndex.getFunctionalCallSite(name);
            case VARIABLE: return MethodIndex.getVariableCallSite(name);
            case SUPER: return MethodIndex.getSuperCallSite();
            case UNKNOWN:
        }

        return null; // fallthrough for unknown
    }

    public boolean hasClosure() {
        return closure != null;
    }

    public boolean hasLiteralClosure() {
        return closure instanceof WrappedIRClosure;
    }

    public boolean isAllConstants() {
        for (Operand argument : arguments) {
            if (!(argument instanceof ImmutableLiteral)) return false;
        }

        return true;
    }

    public static boolean isAllFixnums(Operand[] args) {
        for (Operand argument : args) {
            if (!(argument instanceof Fixnum)) return false;
        }

        return true;
    }

    public boolean isAllFixnums() {
        return isAllFixnums(arguments);
    }

    public static boolean isAllFloats(Operand[] args) {
        for (Operand argument : args) {
            if (!(argument instanceof Float)) return false;
        }

        return true;
    }

    public boolean isAllFloats() {
        return isAllFloats(arguments);
    }

    @Override
    public boolean computeScopeFlags(IRScope scope) {
        boolean modifiedScope = false;

        if (targetRequiresCallersBinding()) {
            modifiedScope = true;
            scope.getFlags().add(BINDING_HAS_ESCAPED);
        }

        if (targetRequiresCallersFrame()) {
            modifiedScope = true;
            scope.getFlags().add(REQUIRES_FRAME);
        }

        if (canBeEval()) {
            modifiedScope = true;
            scope.getFlags().add(USES_EVAL);

            // If this method receives a closure arg, and this call is an eval that has more than 1 argument,
            // it could be using the closure as a binding -- which means it could be using pretty much any
            // variable from the caller's binding!
            if (scope.getFlags().contains(RECEIVES_CLOSURE_ARG) && (getCallArgs().length > 1)) {
                scope.getFlags().add(CAN_CAPTURE_CALLERS_BINDING);
            }
        }

        // Kernel.local_variables inspects variables.
        // and JRuby implementation uses dyn-scope to access the static-scope
        // to output the local variables => we cannot strip dynscope in those cases.
        // FIXME: We need to decouple static-scope and dyn-scope.
        String mname = getName();
        if (mname.equals("local_variables")) {
            scope.getFlags().add(REQUIRES_DYNSCOPE);
        } else if (mname.equals("send") || mname.equals("__send__")) {
            Operand[] args = getCallArgs();
            if (args.length >= 1) {
                Operand meth = args[0];
                if (meth instanceof StringLiteral && "local_variables".equals(((StringLiteral)meth).string)) {
                    scope.getFlags().add(REQUIRES_DYNSCOPE);
                }
            }
        }

        return modifiedScope;
    }
    /**
     * Interpreter can ask the instruction if it knows how to make a more
     * efficient instruction for direct interpretation.
     *
     * @return itself or more efficient but semantically equivalent instr
     */
    public CallBase specializeForInterpretation() {
        return this;
    }

    @Override
    public void simplifyOperands(Map<Operand, Operand> valueMap, boolean force) {
        // FIXME: receiver should never be null (checkArity seems to be one culprit)
        if (receiver != null) receiver = receiver.getSimplifiedOperand(valueMap, force);

        for (int i = 0; i < arguments.length; i++) {
            arguments[i] = arguments[i].getSimplifiedOperand(valueMap, force);
        }

        // Recompute containsArgSplat flag
        containsArgSplat = containsArgSplat(arguments);

        if (closure != null) closure = closure.getSimplifiedOperand(valueMap, force);
        flagsComputed = false; // Forces recomputation of flags

        // recompute whenever instr operands change! (can this really change though?)
        callSite = getCallSiteFor(callType, name);
    }

    public Operand[] cloneCallArgs(CloneInfo ii) {
        int i = 0;
        Operand[] clonedArgs = new Operand[arguments.length];

        for (Operand a: arguments) {
            clonedArgs[i++] = a.cloneForInlining(ii);
        }

        return clonedArgs;
    }

    public boolean isRubyInternalsCall() {
        return false;
    }

    public boolean isStaticCallTarget() {
        return false;
    }

    // SSS FIXME: Are all bases covered?
    // How about aliasing of 'call', 'eval', 'send', 'module_eval', 'class_eval', 'instance_eval'?
    private boolean computeEvalFlag() {
        // ENEBO: This could be made into a recursive two-method thing so then: send(:send, :send, :send, :send, :eval, "Hosed") works
        String mname = getName();
        // checking for "call" is conservative.  It can be eval only if the receiver is a Method
        // CON: Removed "call" check because we didn't do it in 1.7 and it deopts all callers of Method or Proc objects.
        if (/*mname.equals("call") ||*/ mname.equals("eval") || mname.equals("module_eval") || mname.equals("class_eval") || mname.equals("instance_eval")) return true;

        // Calls to 'send' where the first arg is either unknown or is eval or send (any others?)
        if (mname.equals("send") || mname.equals("__send__")) {
            Operand[] args = getCallArgs();
            if (args.length >= 2) {
                Operand meth = args[0];
                if (!(meth instanceof StringLiteral)) return true; // We don't know

                String name = ((StringLiteral) meth).string;
                if (   name.equals("call")
                    || name.equals("eval")
                    || mname.equals("module_eval")
                    || mname.equals("class_eval")
                    || mname.equals("instance_eval")
                    || name.equals("send")
                    || name.equals("__send__")) return true;
            }
        }

        return false; // All checks passed
    }

    private boolean computeRequiresCallersBindingFlag() {
        if (canBeEval()) return true;

        // literal closures can be used to capture surrounding binding
        if (hasLiteralClosure()) return true;

        String mname = getName();
        if (MethodIndex.SCOPE_AWARE_METHODS.contains(mname)) {
            return true;
        } else if (mname.equals("send") || mname.equals("__send__")) {
            Operand[] args = getCallArgs();
            if (args.length >= 1) {
                Operand meth = args[0];
                if (!(meth instanceof StringLiteral)) return true; // We don't know -- could be anything

                String name = ((StringLiteral) meth).string;
                if (MethodIndex.SCOPE_AWARE_METHODS.contains(name)) {
                    return true;
                }
            }
        }

        /* -------------------------------------------------------------
         * SSS FIXME: What about aliased accesses to these same methods?
         * See problem snippet below. To be clear, the problem with this
         * Module.nesting below is because that method uses DynamicScope
         * to access the static-scope. However, even if we moved the static-scope
         * to Frame, the problem just shifts over to optimizations that eliminate
         * push/pop of Frame objects from certain scopes.
         *
         * [subbu@earth ~/jruby] cat /tmp/pgm.rb
         * class Module
         *   class << self
         *     alias_method :foobar, :nesting
         *   end
         * end
         *
         * module X
         *   puts "X. Nesting is: #{Module.foobar}"
         * end
         *
         * module Y
         *   puts "Y. Nesting is: #{Module.nesting}"
         * end
         *
         * [subbu@earth ~/jruby] jruby -X-CIR -Xir.passes=OptimizeTempVarsPass,LocalOptimizationPass,AddLocalVarLoadStoreInstructions,AddCallProtocolInstructions,LinearizeCFG /tmp/pgm.rb
         * X. Nesting is: []
         * Y. Nesting is: [Y]
         * [subbu@earth ~/jruby] jruby -X-CIR -Xir.passes=LinearizeCFG /tmp/pgm.rb
         * X. Nesting is: [X]
         * Y. Nesting is: [Y]
         * ------------------------------------------------------------- */

        // SSS FIXME: Are all bases covered?
        return false;  // All checks done -- dont need one
    }

    private boolean computeRequiresCallersFrameFlag() {
        if (canBeEval()) return true;

        // literal closures can be used to capture surrounding binding
        if (hasLiteralClosure()) return true;

        if (procNew) return true;

        String mname = getName();
        if (MethodIndex.FRAME_AWARE_METHODS.contains(mname)) {
            // Known frame-aware methods.
            return true;

        } else if (mname.equals("send") || mname.equals("__send__")) {
            // Allow send to access full binding, since someone might send :eval and friends.
            Operand[] args = getCallArgs();
            if (args.length >= 1) {
                Operand meth = args[0];
                if (!(meth instanceof StringLiteral)) return true; // We don't know -- could be anything

                String name = ((StringLiteral) meth).string;
                if (MethodIndex.FRAME_AWARE_METHODS.contains(name)) {
                    return true;
                }
            }
        }

        return false;
    }

    private void computeFlags() {
        // Order important!
        flagsComputed = true;
        canBeEval = computeEvalFlag();
        targetRequiresCallersBinding = canBeEval || computeRequiresCallersBindingFlag();
        targetRequiresCallersFrame = canBeEval || computeRequiresCallersFrameFlag();
    }

    public boolean canBeEval() {
        if (!flagsComputed) computeFlags();

        return canBeEval;
    }

    public boolean targetRequiresCallersBinding() {
        if (!flagsComputed) computeFlags();

        return targetRequiresCallersBinding;
    }

    public boolean targetRequiresCallersFrame() {
        if (!flagsComputed) computeFlags();

        return targetRequiresCallersFrame;
    }

    @Override
    public String toString() {
        return "" + getOperation()  + "(" + callType + ", " + getName() + ", " + receiver +
                ", " + Arrays.toString(getCallArgs()) +
                (closure == null ? "" : ", &" + closure) + ")";
    }

    public static boolean containsArgSplat(Operand[] arguments) {
        for (Operand argument : arguments) {
            if (argument instanceof Splat && ((Splat)argument).unsplatArgs) return true;
        }

        return false;
    }

    private final static int REQUIRED_OPERANDS = 3;
    private static Operand[] buildAllArgs(Operand callType, Operand receiver,
            Fixnum argsCount, Operand[] callArgs, Operand closure) {
        Operand[] allArgs = new Operand[callArgs.length + REQUIRED_OPERANDS + (closure != null ? 1 : 0)];

        assert receiver != null : "RECEIVER is null";


        allArgs[0] = callType;
        allArgs[1] = receiver;
        // -0 not possible so if closure exists we are negative and we subtract one to get real arg count.
        allArgs[2] = argsCount;
        for (int i = 0; i < callArgs.length; i++) {
            assert callArgs[i] != null : "ARG " + i + " is null";

            allArgs[i + REQUIRED_OPERANDS] = callArgs[i];
        }

        if (closure != null) allArgs[callArgs.length + REQUIRED_OPERANDS] = closure;

        return allArgs;
    }

    @Override
    public Object interpret(ThreadContext context, StaticScope currScope, DynamicScope dynamicScope, IRubyObject self, Object[] temp) {
        IRubyObject object = (IRubyObject) receiver.retrieve(context, self, currScope, dynamicScope, temp);
        IRubyObject[] values = prepareArguments(context, self, arguments, currScope, dynamicScope, temp);
        Block preparedBlock = prepareBlock(context, self, currScope, dynamicScope, temp);

        return callSite.call(context, self, object, values, preparedBlock);
    }

    protected IRubyObject[] prepareArguments(ThreadContext context, IRubyObject self, Operand[] arguments, StaticScope currScope, DynamicScope dynamicScope, Object[] temp) {
        return containsArgSplat ?
                prepareArgumentsComplex(context, self, arguments, currScope, dynamicScope, temp) :
                prepareArgumentsSimple(context, self, arguments, currScope, dynamicScope, temp);
    }

    protected IRubyObject[] prepareArgumentsSimple(ThreadContext context, IRubyObject self, Operand[] args, StaticScope currScope, DynamicScope currDynScope, Object[] temp) {
        IRubyObject[] newArgs = new IRubyObject[args.length];

        for (int i = 0; i < args.length; i++) {
            newArgs[i] = (IRubyObject) args[i].retrieve(context, self, currScope, currDynScope, temp);
        }

        return newArgs;
    }

    protected IRubyObject[] prepareArgumentsComplex(ThreadContext context, IRubyObject self, Operand[] args, StaticScope currScope, DynamicScope currDynScope, Object[] temp) {
        // SSS: For regular calls, IR builder never introduces splats except as the first argument
        // But when zsuper is converted to SuperInstr with known args, splats can appear anywhere
        // in the list.  So, this looping handles both these scenarios, although if we wanted to
        // optimize for CallInstr which has splats only in the first position, we could do that.
        List<IRubyObject> argList = new ArrayList<IRubyObject>(args.length * 2);
        for (Operand arg : args) {
            IRubyObject rArg = (IRubyObject) arg.retrieve(context, self, currScope, currDynScope, temp);
            if (arg instanceof Splat) {
                RubyArray array = (RubyArray) rArg;
                for (int i = 0; i < array.size(); i++) {
                    argList.add(array.eltOk(i));
                }
            } else {
                argList.add(rArg);
            }
        }

        return argList.toArray(new IRubyObject[argList.size()]);
    }

    public Block prepareBlock(ThreadContext context, IRubyObject self, StaticScope currScope, DynamicScope currDynScope, Object[] temp) {
        if (closure == null) return Block.NULL_BLOCK;

        return IRRuntimeHelpers.getBlockFromObject(context, closure.retrieve(context, self, currScope, currDynScope, temp));
    }
}
