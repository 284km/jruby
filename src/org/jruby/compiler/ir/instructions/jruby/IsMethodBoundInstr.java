/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jruby.compiler.ir.instructions.jruby;

import java.util.Map;
import org.jruby.compiler.ir.Operation;
import org.jruby.compiler.ir.instructions.Instr;
import org.jruby.compiler.ir.instructions.ResultInstr;
import org.jruby.compiler.ir.operands.Operand;
import org.jruby.compiler.ir.operands.StringLiteral;
import org.jruby.compiler.ir.operands.Variable;
import org.jruby.compiler.ir.representations.InlinerInfo;
import org.jruby.compiler.ir.targets.JVM;
import org.jruby.runtime.Block;
import org.jruby.runtime.DynamicScope;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;

/**
 *
 * @author enebo
 */
public class IsMethodBoundInstr extends DefinedInstr {
    private final Operand[] operands;
   
    public IsMethodBoundInstr(Variable result, Operand object, StringLiteral name) {
        super(Operation.IS_METHOD_BOUND, result);
        
        this.operands = new Operand[] { object, name };
    }

    @Override
    public Operand[] getOperands() {
        return operands;
    }
    
    public StringLiteral getName() {
        return (StringLiteral) operands[1];
    }
    
    public Operand getObject() {
        return operands[0];
    }

    @Override
    public void simplifyOperands(Map<Operand, Operand> valueMap, boolean force) {
         operands[0] = operands[0].getSimplifiedOperand(valueMap, force);
         operands[1] = operands[1].getSimplifiedOperand(valueMap, force);
    }

    public void updateResult(Variable v) {
        result = v;
    }

    @Override
    public Instr cloneForInlining(InlinerInfo inlinerInfo) {
        return new IsMethodBoundInstr((Variable) getResult().cloneForInlining(inlinerInfo), 
                getObject().cloneForInlining(inlinerInfo),
                (StringLiteral) getName().cloneForInlining(inlinerInfo));
    }

    @Override
    public String toString() {
        return super.toString() + "(" + getObject() + ", " + getName() + ")";
    }

    @Override
    public Object interpret(ThreadContext context, DynamicScope currDynScope, IRubyObject self, Object[] temp, Block block) {
        IRubyObject receiver = (IRubyObject) getObject().retrieve(context, self, currDynScope, temp);
        
        return context.runtime.newBoolean(receiver.getMetaClass().isMethodBound(getName().string, false)); 
    }

    @Override
    public void compile(JVM jvm) {
        // no-op right now
    }    
}
