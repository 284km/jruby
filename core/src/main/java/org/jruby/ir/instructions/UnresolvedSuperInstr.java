package org.jruby.ir.instructions;

import org.jruby.ir.IRVisitor;
import org.jruby.ir.Operation;
import org.jruby.ir.operands.MethAddr;
import org.jruby.ir.operands.Operand;
import org.jruby.ir.operands.Variable;
import org.jruby.ir.runtime.IRRuntimeHelpers;
import org.jruby.ir.transformations.inlining.InlinerInfo;
import org.jruby.parser.StaticScope;
import org.jruby.runtime.Block;
import org.jruby.runtime.CallType;
import org.jruby.runtime.DynamicScope;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;

public class UnresolvedSuperInstr extends CallInstr {
    // SSS FIXME: receiver is never used -- being passed in only to meet requirements of CallInstr
    public UnresolvedSuperInstr(Operation op, Variable result, Operand receiver, Operand[] args, Operand closure) {
        super(op, CallType.SUPER, result, MethAddr.UNKNOWN_SUPER_TARGET, receiver, args, closure);
    }

    public UnresolvedSuperInstr(Variable result, Operand receiver, Operand[] args, Operand closure) {
        this(Operation.UNRESOLVED_SUPER, result, receiver, args, closure);
    }

    @Override
    public Instr cloneForInlining(InlinerInfo ii) {
        return new UnresolvedSuperInstr(ii.getRenamedVariable(getResult()), getReceiver().cloneForInlining(ii), cloneCallArgs(ii), closure == null ? null : closure.cloneForInlining(ii));
    }

    // We cannot convert this into a NoCallResultInstr
    @Override
    public Instr discardResult() {
        return this;
    }

    @Override
    public CallBase specializeForInterpretation() {
        return this;
    }

    @Override
    public Object interpret(ThreadContext context, StaticScope currScope, DynamicScope currDynScope, IRubyObject self, Object[] temp) {
        IRubyObject[] args = prepareArguments(context, self, getCallArgs(), currScope, currDynScope, temp);
        Block block = prepareBlock(context, self, currScope, currDynScope, temp);
        return IRRuntimeHelpers.unresolvedSuper(context, self, args, block);
    }

    @Override
    public void visit(IRVisitor visitor) {
        visitor.UnresolvedSuperInstr(this);
    }
}
