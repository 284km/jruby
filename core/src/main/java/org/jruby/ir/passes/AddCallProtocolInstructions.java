package org.jruby.ir.passes;

import org.jruby.ir.*;
import org.jruby.ir.dataflow.analyses.StoreLocalVarPlacementProblem;
import org.jruby.ir.instructions.*;
import org.jruby.ir.operands.Label;
import org.jruby.ir.operands.MethAddr;
import org.jruby.ir.operands.Variable;
import org.jruby.ir.representations.BasicBlock;
import org.jruby.ir.representations.CFG;

import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

public class AddCallProtocolInstructions extends CompilerPass {
    @Override
    public String getLabel() {
        return "Add Call Protocol Instructions (push/pop of dyn-scope, frame, impl-class values)";
    }

    public static List<Class<? extends CompilerPass>> DEPENDENCIES = Arrays.<Class<? extends CompilerPass>>asList(CFGBuilder.class);

    @Override
    public List<Class<? extends CompilerPass>> getDependencies() {
        return DEPENDENCIES;
    }

    private boolean explicitCallProtocolSupported(IRScope scope) {
        return scope instanceof IRMethod || (scope instanceof IRModuleBody && !(scope instanceof IRMetaClassBody));
    }

    @Override
    public Object execute(IRScope scope, Object... data) {
        // IRScriptBody do not get explicit call protocol instructions right now.
        // They dont push/pop a frame and do other special things like run begin/end blocks.
        // So, for now, they go through the runtime stub in IRScriptBody.
        //
        // SSS FIXME: Right now, we always add push/pop frame instrs -- in the future, we may skip them
        // for certain scopes.
        //
        // Add explicit frame and binding push/pop instrs ONLY for methods -- we cannot handle this in closures and evals yet
        // If the scope uses $_ or $~ family of vars, has local load/stores, or if its binding has escaped, we have
        // to allocate a dynamic scope for it and add binding push/pop instructions.
        if (explicitCallProtocolSupported(scope)) {
            StoreLocalVarPlacementProblem slvpp = (StoreLocalVarPlacementProblem)scope.getDataFlowSolution(StoreLocalVarPlacementProblem.NAME);

            boolean scopeHasLocalVarStores      = false;
            boolean scopeHasUnrescuedExceptions = false;
            boolean bindingHasEscaped           = scope.bindingHasEscaped();

            CFG        cfg = scope.cfg();

            if (slvpp != null && bindingHasEscaped) {
                scopeHasLocalVarStores      = slvpp.scopeHasLocalVarStores();
                scopeHasUnrescuedExceptions = slvpp.scopeHasUnrescuedExceptions();
            } else {
                // We dont require local-var load/stores to have been run.
                // If it is not run, we go conservative and add push/pop binding instrs. everywhere
                scopeHasLocalVarStores      = bindingHasEscaped;
                scopeHasUnrescuedExceptions = false;
                for (BasicBlock bb: cfg.getBasicBlocks()) {
                    // SSS FIXME: This is highly conservative.  If the bb has an exception raising instr.
                    // and if we dont have a rescuer, only then do we have unrescued exceptions.
                    if (cfg.getRescuerBBFor(bb) == null) {
                        scopeHasUnrescuedExceptions = true;
                        break;
                    }
                }
            }

/* ----------------------------------------------------------------------
 * Turning this off for now since this code is buggy and fails a few tests
 * See example below which fails:
 *
       def y; yield; end

       y {
         def revivify
           Proc::new
         end

         y {
           x = Proc.new {}
           y = revivify(&x)
           p x.object_id, y.object_id
         }
       }
 *
            boolean requireFrame = bindingHasEscaped || scope.usesEval();

            for (IRFlags flag : scope.getFlags()) {
                switch (flag) {
                    case BINDING_HAS_ESCAPED:
                    case CAN_CAPTURE_CALLERS_BINDING:
                    case CAN_RECEIVE_BREAKS:
                    case CAN_RECEIVE_NONLOCAL_RETURNS:
                    case HAS_NONLOCAL_RETURNS:
                    case REQUIRES_FRAME:
                    case REQUIRES_VISIBILITY:
                    case USES_BACKREF_OR_LASTLINE:
                    case USES_EVAL:
                    case USES_ZSUPER:
                        requireFrame = true;
                }
            }
 * ---------------------------------------------------------------------- */

            boolean requireFrame = true;
            boolean requireBinding = bindingHasEscaped || scopeHasLocalVarStores || !scope.getFlags().contains(IRFlags.DYNSCOPE_ELIMINATED);

            // FIXME: Why do we need a push/pop for frame & binding for scopes with unrescued exceptions??
            // 1. I think we need a different check for frames -- it is NOT scopeHasUnrescuedExceptions
            //    We need scope.requiresFrame() to push/pop frames
            // 2. Plus bindingHasEscaped check in IRScope is missing some other check since we should
            //    just be able to check (bindingHasEscaped || scopeHasVarStores) to push/pop bindings.
            // We need scopeHasUnrescuedExceptions to add GEB for popping frame/binding on exit from unrescued exceptions
            BasicBlock entryBB = cfg.getEntryBB();
            if (requireBinding || requireFrame) {
                // Push
                if (requireFrame) entryBB.addInstr(new PushFrameInstr(new MethAddr(scope.getName())));
                if (requireBinding) entryBB.addInstr(new PushBindingInstr());

                // Allocate GEB if necessary for popping
                BasicBlock geb = cfg.getGlobalEnsureBB();
                if (geb == null) {
                    Variable exc = scope.createTemporaryVariable();
                    geb = new BasicBlock(cfg, Label.getGlobalEnsureBlockLabel());
                    geb.addInstr(new ReceiveJRubyExceptionInstr(exc)); // JRuby Implementation exception handling
                    geb.addInstr(new ThrowExceptionInstr(exc));
                    cfg.addGlobalEnsureBB(geb);
                }

                // Pop on all scope-exit paths
                for (BasicBlock bb: cfg.getBasicBlocks()) {
                    ListIterator<Instr> instrs = bb.getInstrs().listIterator();
                    while (instrs.hasNext()) {
                        Instr i = instrs.next();
                        // Right now, we only support explicit call protocol on methods.
                        // So, non-local returns and breaks don't get here.
                        // Non-local-returns and breaks are tricky since they almost always
                        // throw an exception and we don't multiple pops (once before the
                        // return/break, and once when the exception is caught).
                        if (!bb.isExitBB() && i instanceof ReturnBase) {
                            // Add before the break/return
                            instrs.previous();
                            if (requireBinding) instrs.add(new PopBindingInstr());
                            if (requireFrame) instrs.add(new PopFrameInstr());
                            break;
                        }
                    }

                    if (bb.isExitBB() && !bb.isEmpty()) {
                        // Last instr could be a return -- so, move iterator one position back
                        if (instrs.hasPrevious()) instrs.previous();
                        if (requireBinding) instrs.add(new PopBindingInstr());
                        if (requireFrame) instrs.add(new PopFrameInstr());
                    }

                    if (bb == geb) {
                        // Add before throw-exception-instr which would be the last instr
                        instrs.previous();
                        if (requireBinding) instrs.add(new PopBindingInstr());
                        if (requireFrame) instrs.add(new PopFrameInstr());
                    }
                }
            }

            // This scope has an explicit call protocol flag now
            scope.setExplicitCallProtocolFlag();
        }

        // FIXME: Useless for now
        // Run on all nested closures.
        for (IRClosure c: scope.getClosures()) run(c, false, true);

        // LVA information is no longer valid after the pass
        // FIXME: Grrr ... this seems broken to have to create a new object to invalidate
        (new LiveVariableAnalysis()).invalidate(scope);

        return null;
    }

    @Override
    public boolean invalidate(IRScope scope) {
        // Cannot add call protocol instructions after we've added them once.
        return false;
    }
}
