package org.jruby.compiler.ir;

import org.jruby.RubyModule;
import org.jruby.compiler.ir.operands.Label;
import org.jruby.compiler.ir.operands.LocalVariable;
import org.jruby.compiler.ir.operands.ClosureLocalVariable;
import org.jruby.compiler.ir.operands.Operand;
import org.jruby.compiler.ir.operands.TemporaryClosureVariable;
import org.jruby.compiler.ir.operands.Variable;
import org.jruby.compiler.ir.representations.CFG;
import org.jruby.interpreter.Interpreter;
import org.jruby.interpreter.NaiveInterpreterContext;
import org.jruby.parser.IRStaticScope;
import org.jruby.parser.IRStaticScopeFactory;
import org.jruby.parser.StaticScope;
import org.jruby.runtime.Block;
import org.jruby.runtime.DynamicScope;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;
import org.jruby.util.log.Logger;
import org.jruby.util.log.LoggerFactory;

public class IREvalScript extends IRClosure {
    private IRExecutionScope nearestNonEvalScope;
    private static final Logger LOG = LoggerFactory.getLogger("IREvalScript");

    public IREvalScript(IRScope lexicalParent, StaticScope staticScope) {
        super(lexicalParent, staticScope, "EVAL_");
        IRScope s = lexicalParent;
        while (s instanceof IREvalScript) s = s.getLexicalParent();
        this.nearestNonEvalScope = (IRExecutionScope)s;
        this.nearestNonEvalScope.initEvalScopeVariableAllocator(false);
    }

    @Override
    public Variable getNewTemporaryVariable() {
        return new TemporaryClosureVariable(closureId, allocateNextPrefixedName("%ev_" + closureId));
    }

    @Override
    public int getTemporaryVariableSize() {
        return getPrefixCountSize("%ev_" + closureId);
    }

    @Override
    public Label getNewLabel() {
        return getNewLabel("EV" + closureId + "_LBL");
    }

    @Override
    public String getScopeName() {
        return "EvalScript";
    }

    @Override
    public Operand[] getBlockArgs() { 
        return new Operand[0];
    }

    @Override
    protected StaticScope constructStaticScope(StaticScope parent) {
        return IRStaticScopeFactory.newIREvalScope(parent);
    }

    public IRubyObject call(ThreadContext context, IRubyObject self, RubyModule clazz, DynamicScope evalScope, Block block) {
        CFG cfg = getCFG();
        if (Interpreter.isDebug()) {
            LOG.info("CFG:\n" + cfg.getGraph());
            LOG.info("\nInstructions:\n" + cfg.toStringInstrs());
        }
        try {
            context.pushScope(evalScope);
            NaiveInterpreterContext interp = new NaiveInterpreterContext(context, this, clazz, self, null, new IRubyObject[]{}, block, null);
            return Interpreter.interpret(context, self, this, interp);
        }
        finally {
            context.popScope();
        }
    }

    @Override
    public LocalVariable findExistingLocalVariable(String name) {
        // Look in the nearest non-eval scope's shared eval scope vars first.
        // If you dont find anything there, look in the nearest non-eval scope's regular vars.
        LocalVariable lvar = nearestNonEvalScope.evalScopeVars.getVariable(name);
        if (lvar != null) return lvar;
        else return nearestNonEvalScope.findExistingLocalVariable(name);
    }

    @Override
    public LocalVariable getNewLocalVariable(String name, int scopeDepth) {
        LocalVariable lvar = new ClosureLocalVariable(this, name, 0, nearestNonEvalScope.evalScopeVars.nextSlot);
        nearestNonEvalScope.evalScopeVars.putVariable(name, lvar);
        return lvar;
    }

    @Override
    public int getUsedVariablesCount() {
        return 1 + nearestNonEvalScope.evalScopeVars.nextSlot + getPrefixCountSize("%flip");
    }
}
