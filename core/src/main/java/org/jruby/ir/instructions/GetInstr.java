package org.jruby.ir.instructions;

import org.jruby.ir.Operation;
import org.jruby.ir.operands.Operand;
import org.jruby.ir.operands.StringLiteral;
import org.jruby.ir.operands.Variable;

import java.util.Map;

// Represents result = source.ref or result = source where source is not a stack variable
public abstract class GetInstr extends Instr implements ResultInstr, FixedArityInstr {
    private final String  ref;

    public GetInstr(Operation op, Variable result, Operand source, String ref) {
        super(op, result, new Operand[] { source });

        assert result != null: "" + getClass().getSimpleName() + " result is null";

        this.ref = ref;
    }

    public String getRef() {
        return ref;
    }

    public Operand getSource() {
        return operands[0];
    }

    @Override
    public String toString() {
        return super.toString() + "(" + getSource() + (ref == null ? "" : ", " + ref) + ")";
    }
}
