package org.jruby.compiler.ir.operands;

import java.util.List;
import org.jruby.compiler.ir.IRClosure;
import org.jruby.runtime.Binding;
import org.jruby.runtime.Block;
import org.jruby.runtime.BlockBody;
import org.jruby.runtime.DynamicScope;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;

public class WrappedIRClosure extends Operand {
    private final IRClosure closure;

    public WrappedIRClosure(IRClosure scope) {
        this.closure = scope;
    }
    
    @Override
    public void addUsedVariables(List<Variable> l) {
        /* Nothing to o */
    }    

    public IRClosure getClosure() {
        return closure;
    }

    @Override
    public boolean canCopyPropagate() {
        return true;
    }

    @Override
    public String toString() {
        return closure.toString();
    }

    @Override
    public Object retrieve(ThreadContext context, IRubyObject self, DynamicScope currDynScope, Object[] temp) {
        BlockBody body = closure.getBlockBody();
        closure.getStaticScope().determineModule();
        Binding binding = context.currentBinding(self, currDynScope);

        return new Block(body, binding);
    }
}
