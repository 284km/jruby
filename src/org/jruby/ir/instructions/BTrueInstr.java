package org.jruby.ir.instructions;

import org.jruby.ir.Operation;
import org.jruby.ir.operands.Label;
import org.jruby.ir.operands.Operand;
import org.jruby.ir.representations.InlinerInfo;
import org.jruby.ir.targets.JVM;

public class BTrueInstr extends BranchInstr {
    protected BTrueInstr(Operand v, Label jmpTarget) {
        super(Operation.B_TRUE, v, null, jmpTarget);
    }

    @Override
    public Instr cloneForInlinedScope(InlinerInfo ii) {
        return new BTrueInstr(getArg1().cloneForInlining(ii), ii.getRenamedLabel(getJumpTarget()));
    }

    @Override
    public Instr cloneForBlockCloning(InlinerInfo ii) {
        return new BTrueInstr(getArg1().cloneForInlining(ii), getJumpTarget());
    }

    public void compile(JVM jvm) {
        jvm.emit(getArg1());
        jvm.method().isTrue();
        System.out.println("btrue jump: " + getJumpTarget());
        jvm.method().btrue(jvm.methodData().getLabel(getJumpTarget()));
    }
}
