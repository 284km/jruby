package org.jruby.ir.passes;

import org.jruby.ir.IRScope;
import org.jruby.ir.instructions.CopyInstr;
import org.jruby.ir.instructions.Instr;
import org.jruby.ir.operands.Nil;
import org.jruby.ir.operands.TemporaryVariable;
import org.jruby.ir.operands.Variable;
import org.jruby.ir.representations.BasicBlock;
import org.jruby.ir.representations.CFG;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EnsureTempsAssigned extends CompilerPass {
    public static List<Class<? extends CompilerPass>> DEPENDENCIES = Arrays.<Class<? extends CompilerPass>>asList(CFGBuilder.class);

    @Override
    public String getLabel() {
        return "Ensure Temporary Variables Assigned";
    }

    @Override
    public List<Class<? extends CompilerPass>> getDependencies() {
        return DEPENDENCIES;
    }

    @Override
    public Object execute(IRScope scope, Object... data) {
        processCFG(scope.getCFG());

        return null;
    }

    private void processCFG(CFG cfg) {
        Set<TemporaryVariable> names = new HashSet<TemporaryVariable>();
        for (BasicBlock b : cfg.getBasicBlocks()) {
            for (Instr i : b.getInstrs()) {
                for (Variable v : i.getUsedVariables()) {
                    if (v instanceof TemporaryVariable) {
                        names.add((TemporaryVariable)v);
                    }
                }
            }
        }

        BasicBlock bb = cfg.getEntryBB();
        for (TemporaryVariable name : names) {
            bb.getInstrs().add(0, new CopyInstr(name, new Nil()));
        }

        // recurse
        for (IRScope childScope : cfg.getScope().getClosures()) {
            processCFG(childScope.cfg());
        }
    }

    @Override
    public void invalidate(IRScope scope) {
    }
}
