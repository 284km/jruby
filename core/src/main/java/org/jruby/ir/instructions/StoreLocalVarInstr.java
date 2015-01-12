package org.jruby.ir.instructions;

import org.jruby.ir.IRScope;
import org.jruby.ir.IRVisitor;
import org.jruby.ir.Operation;
import org.jruby.ir.operands.LocalVariable;
import org.jruby.ir.operands.Operand;
import org.jruby.ir.transformations.inlining.CloneInfo;
import org.jruby.parser.StaticScope;
import org.jruby.runtime.DynamicScope;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;

public class StoreLocalVarInstr extends Instr implements FixedArityInstr {
    private final IRScope scope;

    public StoreLocalVarInstr(Operand value, IRScope scope, LocalVariable lvar) {
        super(Operation.BINDING_STORE, new Operand[] { value, lvar });

        this.scope = scope;
    }


    public Operand getValue() {
        return operands[0];
    }

    /** This is the variable that is being stored into in this scope.  This variable
     * doesn't participate in the computation itself.  We just use it as a proxy for
     * its (a) name (b) offset (c) scope-depth. */
    public LocalVariable getLocalVar() {
        return (LocalVariable) operands[1];
    }

    public IRScope getScope() {
        return scope;
    }

    @Override
    public String toString() {
        return "store_lvar(" + getValue() + ", " + scope.getName() + ", " + getLocalVar() + ")";
    }

    // SSS FIXME: This feels dirty
    public void decrementLVarScopeDepth() {
        operands[1] = getLocalVar().cloneForDepth(getLocalVar().getScopeDepth()-1);
    }

    @Override
    public Instr clone(CloneInfo ii) {
        // SSS FIXME: Do we need to rename lvar really?  It is just a name-proxy!
        return new StoreLocalVarInstr(getValue().cloneForInlining(ii), scope,
                (LocalVariable)getLocalVar().cloneForInlining(ii));
    }

    @Override
    public Object interpret(ThreadContext context, StaticScope currScope, DynamicScope currDynScope, IRubyObject self, Object[] temp) {
        Object varValue = getValue().retrieve(context, self, currScope, currDynScope, temp);
        currDynScope.setValue((IRubyObject)varValue, getLocalVar().getLocation(), getLocalVar().getScopeDepth());
        return null;
    }

    @Override
    public void visit(IRVisitor visitor) {
        visitor.StoreLocalVarInstr(this);
    }
}
