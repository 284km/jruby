package org.jruby.ir.passes;

import org.jruby.ir.IRScope;

public class CallSplitter extends CompilerPass {
    public static String[] NAMES = new String[] {"split_calls"};
    
    public String getLabel() {
        return "Call Splitting";
    }
    
    public boolean isPreOrder() {
        return true;
    }

    public Object execute(IRScope scope, Object... data) {
        scope.splitCalls();
        
        return null;
    }
    
    public void reset(IRScope scope) {
        // FIXME: ...
    }
}
