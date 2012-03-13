package org.jruby.ir.instructions;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import org.jruby.ir.Operation;
import org.jruby.ir.operands.Operand;
import org.jruby.ir.operands.Label;
import org.jruby.ir.transformations.inlining.InlinerInfo;

public class ExceptionRegionStartMarkerInstr extends Instr {
    final public Label begin;
    final public Label end;
    final public Label firstRescueBlockLabel;
    final public Label ensureBlockLabel;

    public ExceptionRegionStartMarkerInstr(Label begin, Label end, 
            Label ensureBlockLabel, Label firstRescueBlockLabel) {
        super(Operation.EXC_REGION_START);
        
        this.begin = begin;
        this.end = end;
        this.firstRescueBlockLabel = firstRescueBlockLabel;
        this.ensureBlockLabel = ensureBlockLabel;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder(super.toString());
        
        buf.append("(").append(begin).append(", ").append(end).append(", rescue[").append(firstRescueBlockLabel).append("]");
        if (ensureBlockLabel != null) buf.append(", ensure[").append(ensureBlockLabel).append("]");
        buf.append(")");
        
        return buf.toString();
    }

    public Operand[] getOperands() {
        return EMPTY_OPERANDS;
    }
}
