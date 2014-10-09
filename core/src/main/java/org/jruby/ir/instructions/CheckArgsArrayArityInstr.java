package org.jruby.ir.instructions;

import org.jruby.RubyArray;
import org.jruby.ir.IRVisitor;
import org.jruby.ir.Operation;
import org.jruby.ir.operands.Fixnum;
import org.jruby.ir.operands.Operand;
import org.jruby.ir.transformations.inlining.CloneInfo;
import org.jruby.parser.StaticScope;
import org.jruby.runtime.DynamicScope;
import org.jruby.runtime.Helpers;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;

import java.util.Map;

public class CheckArgsArrayArityInstr extends Instr  implements FixedArityInstr {
    public final int required;
    public final int opt;
    public final int rest;
    private Operand argsArray;

    public CheckArgsArrayArityInstr(Operand argsArray, int required, int opt, int rest) {
        super(Operation.CHECK_ARGS_ARRAY_ARITY);

        this.required = required;
        this.opt = opt;
        this.rest = rest;
        this.argsArray = argsArray;
    }

    public Operand getArgsArray() {
        return argsArray;
    }

    @Override
    public Operand[] getOperands() {
        return new Operand[] { argsArray, new Fixnum(required), new Fixnum(opt), new Fixnum(rest) };
    }

    @Override
    public void simplifyOperands(Map<Operand, Operand> valueMap, boolean force) {
        argsArray = argsArray.getSimplifiedOperand(valueMap, force);
    }

    @Override
    public String toString() {
        return super.toString() + "(" + argsArray + ", " +  required + ", " + opt + ", " + rest + ")";
    }

    @Override
    public Instr clone(CloneInfo ii) {
        return new CheckArgsArrayArityInstr(argsArray.cloneForInlining(ii), required, opt, rest);
    }

    @Override
    public Object interpret(ThreadContext context, StaticScope currScope, DynamicScope currDynScope, IRubyObject self, Object[] temp) {
        RubyArray args = (RubyArray) argsArray.retrieve(context, self, currScope, currDynScope, temp);
        Helpers.irCheckArgsArrayArity(context, args, required, opt, rest);
        return null;
    }

    @Override
    public void visit(IRVisitor visitor) {
        visitor.CheckArgsArrayArityInstr(this);
    }
}
