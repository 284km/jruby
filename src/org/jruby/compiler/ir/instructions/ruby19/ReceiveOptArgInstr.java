package org.jruby.compiler.ir.instructions.ruby19;

import org.jruby.compiler.ir.Operation;
import org.jruby.compiler.ir.instructions.CopyInstr;
import org.jruby.compiler.ir.instructions.Instr;
import org.jruby.compiler.ir.instructions.ReceiveOptArgBase;
import org.jruby.compiler.ir.operands.UndefinedValue;
import org.jruby.compiler.ir.operands.Variable;
import org.jruby.compiler.ir.representations.InlinerInfo;
import org.jruby.runtime.builtin.IRubyObject;

public class ReceiveOptArgInstr extends ReceiveOptArgBase {
    /** This instruction gets to pick an argument off the incoming list only if
     *  there are at least this many incoming arguments */
    public final int minArgsLength;

    public ReceiveOptArgInstr(Variable result, int index, int minArgsLength) {
        super(result, index);
        this.minArgsLength = minArgsLength;
    }

    @Override
    public String toString() {
        return (isDead() ? "[DEAD]" : "") + (hasUnusedResult() ? "[DEAD-RESULT]" : "") + getResult() + " = " + getOperation() + "(" + argIndex + ", " + minArgsLength + ")";
    }

    public Instr cloneForInlining(InlinerInfo ii) {
        int n = ii.getArgsCount();
        return new CopyInstr(ii.getRenamedVariable(result), minArgsLength <= n ? ii.getCallArg(argIndex) : UndefinedValue.UNDEFINED);
    }

    public Object receiveOptArg(IRubyObject[] args) {
        return (minArgsLength <= args.length ? args[argIndex] : UndefinedValue.UNDEFINED);
    }
}
