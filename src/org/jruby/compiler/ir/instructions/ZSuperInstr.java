package org.jruby.compiler.ir.instructions;

import org.jruby.compiler.ir.IRClosure;
import org.jruby.compiler.ir.IRMethod;
import org.jruby.compiler.ir.IRScope;
import org.jruby.compiler.ir.Operation;
import org.jruby.compiler.ir.operands.Operand;
import org.jruby.compiler.ir.operands.Variable;
import org.jruby.compiler.ir.operands.MethAddr;
import org.jruby.compiler.ir.representations.InlinerInfo;

import org.jruby.parser.IRStaticScope;

import org.jruby.runtime.Block;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;

import org.jruby.runtime.DynamicScope;

public class ZSuperInstr extends SuperInstr {
    public ZSuperInstr(Variable result, Operand closure) {
        super(Operation.ZSUPER, result, closure);
    }

    @Override
    public Instr cloneForInlining(InlinerInfo ii) {
        return new ZSuperInstr(ii.getRenamedVariable(result), closure == null ? null : closure.cloneForInlining(ii));
    }

    @Override
    public Operand[] getOperands() {
        return (closure == null) ? EMPTY_OPERANDS : new Operand[] { closure };
    }

    @Override
    public Object interpret(ThreadContext context, DynamicScope currDynScope, IRubyObject self, Object[] temp, Block aBlock) {
        DynamicScope argsDynScope = currDynScope;

        // Find args that need to be passed into super
        while (!argsDynScope.getStaticScope().isArgumentScope()) argsDynScope = argsDynScope.getNextCapturedScope();
        IRScope argsIRScope = ((IRStaticScope)argsDynScope.getStaticScope()).getIRScope(); 
        Operand[] superArgs = (argsIRScope instanceof IRMethod) ? ((IRMethod)argsIRScope).getCallArgs() : ((IRClosure)argsIRScope).getBlockArgs();

        // Prepare args -- but look up in 'argsDynScope', not 'currDynScope'
        IRubyObject[] args = prepareArguments(context, self, superArgs, argsDynScope, temp);

        // Prepare block -- fetching from the frame stack, if necessary
        Block block = prepareBlock(context, self, currDynScope, temp);
        if (block == null || !block.isGiven()) block = context.getFrameBlock();

        return interpretSuper(context, self, args, block);
    }
}
