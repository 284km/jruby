package org.jruby.compiler.ir.operands;

import org.jruby.runtime.ThreadContext;

public class BooleanLiteral extends ImmutableLiteral {
    private final boolean truthy;
    
    public BooleanLiteral(boolean truthy) {
        this.truthy = truthy;
    }

    @Override
    public Object createCacheObject(ThreadContext context) {
        return context.getRuntime().newBoolean(isTrue());
    }
    
    public boolean isTrue()  {
        return truthy;
    }

    public boolean isFalse() {
        return !truthy;
    }
    
    @Override
    public String toString() {
        return isTrue() ? "true" : "false";
    }
}
