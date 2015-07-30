package org.jruby.ir.instructions;

import org.jruby.ir.Operation;
import org.jruby.ir.operands.Label;
import org.jruby.ir.operands.Operand;

public abstract class BranchInstr extends Instr {
    public BranchInstr(Operation op) {
        super(op);
    }

    public abstract Label getJumpTarget();
}
