package org.jruby.runtime;

import org.jruby.EvalType;
import org.jruby.RubyArray;
import org.jruby.ir.IRClosure;
import org.jruby.ir.IRScope;
import org.jruby.ir.runtime.IRRuntimeHelpers;
import org.jruby.runtime.Block.Type;
import org.jruby.runtime.builtin.IRubyObject;

public abstract class IRBlockBody extends ContextAwareBlockBody {
    protected final String fileName;
    protected final int lineNumber;
    protected final IRClosure closure;
    protected ThreadLocal<EvalType> evalType;

    public IRBlockBody(IRScope closure, Signature signature) {
        super(closure.getStaticScope(), signature);
        this.closure = (IRClosure) closure;
        this.fileName = closure.getFileName();
        this.lineNumber = closure.getLineNumber();
        this.evalType = new ThreadLocal();
        this.evalType.set(EvalType.NONE);
    }

    public void setEvalType(EvalType evalType) {
        this.evalType.set(evalType);
    }

    @Override
    public IRubyObject call(ThreadContext context, Block b, Type type) {
        return call(context, IRubyObject.NULL_ARRAY, b, type, Block.NULL_BLOCK);
    }

    @Override
    public IRubyObject call(ThreadContext context, IRubyObject arg0, Block b, Type type) {
        return call(context, new IRubyObject[] {arg0}, b, type, Block.NULL_BLOCK);
    }

    @Override
    public IRubyObject call(ThreadContext context, IRubyObject arg0, IRubyObject arg1, Block b, Type type) {
        return call(context, new IRubyObject[] {arg0, arg1}, b, type, Block.NULL_BLOCK);
    }

    @Override
    public IRubyObject call(ThreadContext context, IRubyObject arg0, IRubyObject arg1, IRubyObject arg2, Block b, Type type) {
        return call(context, new IRubyObject[]{arg0, arg1, arg2}, b, type, Block.NULL_BLOCK);
    }

    @Override
    public IRubyObject call(ThreadContext context, IRubyObject[] args, Block b, Type type) {
        return call(context, args, b, type, Block.NULL_BLOCK);
    }

    @Override
    public IRubyObject call(ThreadContext context, IRubyObject[] args, Block b, Type type, Block block) {
        if (type == Type.LAMBDA) signature.checkArity(context.runtime, args);

        return commonYieldPath(context, prepareArgumentsForCall(context, args, type), null, b, type, block);
    }

    @Override
    public IRubyObject yieldSpecific(ThreadContext context, Block b, Type type) {
        IRubyObject[] args = IRubyObject.NULL_ARRAY;
        if (type == Type.LAMBDA) signature.checkArity(context.runtime, args);

        return commonYieldPath(context, args, null, b, type, Block.NULL_BLOCK);
    }

    @Override
    public IRubyObject yieldSpecific(ThreadContext context, IRubyObject arg0, Block b, Type type) {
        if (arg0 instanceof RubyArray) {
		    // Unwrap the array arg
            IRubyObject[] args = IRRuntimeHelpers.convertValueIntoArgArray(context, arg0, signature.arityValue(), true);

            // FIXME: arity error is aginst new args but actual error shows arity of original args.
            if (type == Type.LAMBDA) signature.checkArity(context.runtime, args);

            return commonYieldPath(context, args, null, b, type, Block.NULL_BLOCK);
        } else {
            return yield(context, arg0, b, type);
        }
    }

    IRubyObject yieldSpecificMultiArgsCommon(ThreadContext context, IRubyObject[] args, Block b, Type type) {
        int blockArity = getSignature().arityValue();
        if (blockArity == 0) {
            args = IRubyObject.NULL_ARRAY; // discard args
        } else if (blockArity == 1) {
            args = new IRubyObject[] { RubyArray.newArrayNoCopy(context.runtime, args) };
        }

        if (type == Type.LAMBDA) signature.checkArity(context.runtime, args);

        return commonYieldPath(context, args, null, b, type, Block.NULL_BLOCK);
    }

    @Override
    public IRubyObject yieldSpecific(ThreadContext context, IRubyObject arg0, IRubyObject arg1, Block b, Type type) {
        return yieldSpecificMultiArgsCommon(context, new IRubyObject[]{arg0, arg1}, b, type);
    }

    @Override
    public IRubyObject yieldSpecific(ThreadContext context, IRubyObject arg0, IRubyObject arg1, IRubyObject arg2, Block b, Type type) {
        return yieldSpecificMultiArgsCommon(context, new IRubyObject[]{arg0, arg1, arg2}, b, type);
    }

    private IRubyObject[] toAry(ThreadContext context, IRubyObject value) {
        IRubyObject val0 = Helpers.aryToAry(value);

        if (val0.isNil()) return new IRubyObject[] { value };

        if (!(val0 instanceof RubyArray)) {
            throw context.runtime.newTypeError(value.getType().getName() + "#to_ary should return Array");
        }

        return ((RubyArray)val0).toJavaArray();
    }

    protected IRubyObject doYieldLambda(ThreadContext context, IRubyObject value, Block b, Type type) {
        // Lambda does not splat arrays even if a rest arg is present when it wants a single parameter filled.
        IRubyObject[] args;

        if (value == null) { // no args case from BlockBody.yieldSpecific
            args = IRubyObject.NULL_ARRAY;
        } else if (signature.required() == 1 || signature.arityValue() == -1) {
            args = new IRubyObject[] { value };
        } else {
            args = toAry(context, value);
        }

        signature.checkArity(context.runtime, args);

        return commonYieldPath(context, args, null, b, type, Block.NULL_BLOCK);
    }

    @Override
    public IRubyObject doYield(ThreadContext context, IRubyObject value, Block b, Type type) {
        if (type == Type.LAMBDA) return doYieldLambda(context, value, b, type);

        int blockArity = getSignature().arityValue();

        IRubyObject[] args;
        if (value == null) { // no args case from BlockBody.yieldSpecific
            args = IRubyObject.NULL_ARRAY;
        } else if (blockArity >= -1 && blockArity <= 1) {
            args = new IRubyObject[] { value };
        } else {
            args = toAry(context, value);
        }

        return commonYieldPath(context, args, null, b, type, Block.NULL_BLOCK);
    }

    @Override
    public IRubyObject doYield(ThreadContext context, IRubyObject[] args, IRubyObject self, Block b, Type type) {
        if (type == Type.LAMBDA) signature.checkArity(context.runtime, args);

        return commonYieldPath(context, args, self, b, type, Block.NULL_BLOCK);
    }

    protected IRubyObject useBindingSelf(Binding binding) {
        IRubyObject self = binding.getSelf();
        binding.getFrame().setSelf(self);

        return self;
    }

    protected abstract IRubyObject commonYieldPath(ThreadContext context, IRubyObject[] args, IRubyObject self, Block b, Type type, Block block);

    public IRClosure getScope() {
        return closure;
    }

    @Override
    public String getFile() {
        return fileName;
    }

    @Override
    public int getLine() {
        return lineNumber;
    }
}
