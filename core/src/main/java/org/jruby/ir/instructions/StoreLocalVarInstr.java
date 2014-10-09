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

import java.util.Map;

public class StoreLocalVarInstr extends Instr implements FixedArityInstr {
    private final IRScope scope;
    private Operand value;

    /** This is the variable that is being stored into in this scope.  This variable
     * doesn't participate in the computation itself.  We just use it as a proxy for
     * its (a) name (b) offset (c) scope-depth. */
    private LocalVariable lvar;

    public StoreLocalVarInstr(Operand value, IRScope scope, LocalVariable lvar) {
        super(Operation.BINDING_STORE);

        this.lvar = lvar;
        this.value = value;
        this.scope = scope;
    }

    public IRScope getScope() {
        return scope;
    }

    @Override
    public Operand[] getOperands() {
        return new Operand[]{value, lvar};
    }

    @Override
    public void simplifyOperands(Map<Operand, Operand> valueMap, boolean force) {
        value = value.getSimplifiedOperand(valueMap, force);
    }

    @Override
    public String toString() {
        return "store_lvar(" + value + ", " + scope.getName() + ", " + lvar + ")";
    }

    public LocalVariable getLocalVar() {
        return lvar;
    }

    // SSS FIXME: This feels dirty
    public void decrementLVarScopeDepth() {
        this.lvar = lvar.cloneForDepth(lvar.getScopeDepth()-1);
    }

    @Override
    public Instr clone(CloneInfo ii) {
        // SSS FIXME: Do we need to rename lvar really?  It is just a name-proxy!
        return new StoreLocalVarInstr(value.cloneForInlining(ii), scope, (LocalVariable)lvar.cloneForInlining(ii));
    }

    @Override
    public Object interpret(ThreadContext context, StaticScope currScope, DynamicScope currDynScope, IRubyObject self, Object[] temp) {
        Object varValue = value.retrieve(context, self, currScope, currDynScope, temp);
        currDynScope.setValue((IRubyObject)varValue, lvar.getLocation(), lvar.getScopeDepth());
        return null;
    }

    @Override
    public void visit(IRVisitor visitor) {
        visitor.StoreLocalVarInstr(this);
    }

    public Operand getValue() {
        return value;
    }
}
