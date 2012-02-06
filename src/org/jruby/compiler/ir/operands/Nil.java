package org.jruby.compiler.ir.operands;

// Records the nil object

import java.util.List;
import org.jruby.compiler.ir.targets.JVM;
import org.jruby.runtime.DynamicScope;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;

public class Nil extends Operand {
    public static final Nil NIL = new Nil();

    protected Nil() {
    }

    @Override
    public boolean hasKnownValue() {
        return true;
    }

    @Override
    public void addUsedVariables(List<Variable> l) {
        /* Do nothing */
    }
    
    @Override
    public String toString() { 
        return "nil";
    }

    @Override
    public Object retrieve(ThreadContext context, IRubyObject self, DynamicScope currDynScope, Object[] temp) {
        return context.nil;
    }

    public void compile(JVM jvm) {
        jvm.method().pushNil();
    }
}
