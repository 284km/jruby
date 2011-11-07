package org.jruby.compiler.ir.instructions;

import java.util.Map;

import org.jruby.compiler.ir.Operation;
import org.jruby.compiler.ir.operands.BooleanLiteral;
import org.jruby.compiler.ir.operands.Label;
import org.jruby.compiler.ir.operands.Operand;
import org.jruby.compiler.ir.operands.Nil;
import org.jruby.compiler.ir.operands.Variable;
import org.jruby.compiler.ir.representations.InlinerInfo;
import org.jruby.interpreter.InterpreterContext;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;

//    is_true(a) = (!a.nil? && a != false) 
//
// Only nil and false compute to false
//
public class IsTrueInstr extends Instr {
    private Operand value;

    public IsTrueInstr(Variable result, Operand value) {
        super(Operation.IS_TRUE, result);
        this.value = value; 
    }

    public Operand[] getOperands() {
        return new Operand[]{value};
    }

    @Override
    public void simplifyOperands(Map<Operand, Operand> valueMap) {
        value = value.getSimplifiedOperand(valueMap);
    }

    @Override
    public String toString() {
        return super.toString() + "(" + value + ")";
    }

    @Override
    public Operand simplifyAndGetResult(Map<Operand, Operand> valueMap) {
        simplifyOperands(valueMap);
        if (!value.isConstant()) return null;

        return value == Nil.NIL || value == BooleanLiteral.FALSE ?
                    BooleanLiteral.FALSE : BooleanLiteral.TRUE;
    }

    @Override
    public Instr cloneForInlining(InlinerInfo ii) {
        return new IsTrueInstr(ii.getRenamedVariable(getResult()), value.cloneForInlining(ii));
    }

    @Override
    public Label interpret(InterpreterContext interp, ThreadContext context, IRubyObject self) {
        // ENEBO: This seems like a lot of extra work...
        getResult().store(interp, context, self, 
                context.getRuntime().newBoolean(((IRubyObject) value.retrieve(interp, context, self)).isTrue()));
        return null;
    }
}
