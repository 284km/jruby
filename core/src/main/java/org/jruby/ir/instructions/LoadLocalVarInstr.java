package org.jruby.ir.instructions;

import org.jruby.ir.IRScope;
import org.jruby.ir.IRVisitor;
import org.jruby.ir.Interp;
import org.jruby.ir.Operation;
import org.jruby.ir.operands.*;
import org.jruby.ir.transformations.inlining.CloneInfo;
import org.jruby.parser.StaticScope;
import org.jruby.runtime.DynamicScope;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;

public class LoadLocalVarInstr extends Instr implements ResultInstr, FixedArityInstr {
    private final IRScope scope;

    public LoadLocalVarInstr(IRScope scope, TemporaryLocalVariable result, LocalVariable lvar) {
        super(Operation.BINDING_LOAD, result, new Operand[] { lvar });

        assert result != null: "LoadLocalVarInstr result is null";

        this.scope = scope;
    }

    /** This is the variable that is being loaded from the scope.  This variable doesn't participate in the
     * computation itself.  We just use it as a proxy for its (a) name (b) offset (c) scope-depth.
     */
    public LocalVariable getLocalVar() {
        return (LocalVariable) operands[0];
    }

    public IRScope getScope() {
        return scope;
    }

    // SSS FIXME: This feels dirty
    public void decrementLVarScopeDepth() {
        operands[0] = getLocalVar().cloneForDepth(getLocalVar().getScopeDepth()-1);
    }

    @Override
    public String toString() {
        return result + " = load_lvar(" + scope.getName() + ", " + getLocalVar() + ")";
    }

    @Override
    public Instr clone(CloneInfo ii) {
        // SSS FIXME: Do we need to rename lvar really?  It is just a name-proxy!
        return new LoadLocalVarInstr(scope, (TemporaryLocalVariable)ii.getRenamedVariable(result),
                (LocalVariable)ii.getRenamedVariable(getLocalVar()));
    }

    @Interp
    @Override
    public Object interpret(ThreadContext context, StaticScope currScope, DynamicScope currDynScope, IRubyObject self, Object[] temp) {
        return getLocalVar().retrieve(context, self, currScope, currDynScope, temp);
    }

    @Override
    public void visit(IRVisitor visitor) {
        visitor.LoadLocalVarInstr(this);
    }
}
