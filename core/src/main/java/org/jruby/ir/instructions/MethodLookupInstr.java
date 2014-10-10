package org.jruby.ir.instructions;

import org.jruby.ir.IRVisitor;
import org.jruby.ir.Operation;
import org.jruby.ir.operands.MethodHandle;
import org.jruby.ir.operands.Operand;
import org.jruby.ir.operands.Variable;
import org.jruby.ir.transformations.inlining.CloneInfo;
import org.jruby.parser.StaticScope;
import org.jruby.runtime.DynamicScope;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;

import java.util.Map;

public class MethodLookupInstr extends Instr implements ResultInstr, FixedArityInstr {
    private Operand methodHandle;
    private Variable result;

    public MethodLookupInstr(Variable result, MethodHandle mh) {
        super(Operation.METHOD_LOOKUP);

        assert result != null: "MethodLookupInstr result is null";

        this.methodHandle = mh;
        this.result = result;
    }

    public MethodLookupInstr(Variable dest, Operand methodName, Operand receiver) {
        this(dest, new MethodHandle(methodName, receiver));
    }

    public MethodHandle getMethodHandle() {
        return (MethodHandle)methodHandle;
    }

    @Override
    public Operand[] getOperands() {
        return new Operand[]{methodHandle};
    }

    @Override
    public Variable getResult() {
        return result;
    }

    @Override
    public void updateResult(Variable v) {
        this.result = v;
    }

    @Override
    public void simplifyOperands(Map<Operand, Operand> valueMap, boolean force) {
        methodHandle = methodHandle.getSimplifiedOperand(valueMap, force);
    }

    @Override
    public String toString() {
        return super.toString() + "(" + methodHandle + ")";
    }

    @Override
    public Instr clone(CloneInfo ii) {
        return new MethodLookupInstr(ii.getRenamedVariable(result), (MethodHandle)methodHandle.cloneForInlining(ii));
    }

    @Override
    public Object interpret(ThreadContext context, StaticScope currScope, DynamicScope currDynScope, IRubyObject self, Object[] temp) {
        return methodHandle.retrieve(context, self, currScope, currDynScope, temp);
    }

    @Override
    public void visit(IRVisitor visitor) {
        visitor.MethodLookupInstr(this);
    }
}
