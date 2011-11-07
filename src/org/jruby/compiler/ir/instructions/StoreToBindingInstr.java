package org.jruby.compiler.ir.instructions;

import java.util.Map;
import org.jruby.compiler.ir.Operation;
import org.jruby.compiler.ir.operands.Label;
import org.jruby.compiler.ir.operands.Operand;
import org.jruby.compiler.ir.IRExecutionScope;
import org.jruby.compiler.ir.IRMethod;
import org.jruby.compiler.ir.operands.LocalVariable;
import org.jruby.compiler.ir.operands.UndefinedValue;
import org.jruby.compiler.ir.representations.InlinerInfo;
import org.jruby.interpreter.InterpreterContext;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;

public class StoreToBindingInstr extends Instr {
    private IRMethod targetMethod;
    private String slotName;
    private Operand value;
    private int bindingSlot;

    public StoreToBindingInstr(IRExecutionScope scope, String slotName, Operand value) {
        super(Operation.BINDING_STORE, null);

        this.slotName = slotName;
        this.targetMethod = (IRMethod)scope.getClosestMethodAncestor();
        this.value = value;
        bindingSlot = targetMethod.assignBindingSlot(slotName);
    }

    public String getSlotName() {
        return slotName;
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
        return "BINDING(" + targetMethod + ")." + slotName + " = " + value;
    }

    public Instr cloneForInlining(InlinerInfo ii) {
        return new StoreToBindingInstr(targetMethod, slotName, value.cloneForInlining(ii));
    }

    @Override
    public Label interpret(InterpreterContext interp, ThreadContext context, IRubyObject self) {
        LocalVariable v = (LocalVariable) value;
        
        if (bindingSlot == -1) bindingSlot = targetMethod.getBindingSlot(v.getName());
        
        // FIXME: This is a pseudo-hack.  bindings set up for blocks in opt arg default values
        // can trip over this since we cannot store somethign which is not a real IRubyObject.
        Object value = interp.getLocalVariable(context, v.getScopeDepth(), v.getLocation());
        if (!(value instanceof UndefinedValue)) {
            interp.setSharedBindingVariable(bindingSlot, value);
        }
        return null;
    }
}
