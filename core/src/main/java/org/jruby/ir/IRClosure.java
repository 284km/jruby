package org.jruby.ir;

import org.jruby.ir.instructions.*;
import org.jruby.ir.operands.*;
import org.jruby.ir.representations.BasicBlock;
import org.jruby.ir.representations.CFG;
import org.jruby.ir.transformations.inlining.CloneInfo;
import org.jruby.ir.transformations.inlining.SimpleCloneInfo;
import org.jruby.parser.StaticScope;
import org.jruby.runtime.Arity;
import org.jruby.runtime.BlockBody;
import org.jruby.runtime.InterpretedIRBlockBody;
import org.objectweb.asm.Handle;

import java.util.ArrayList;
import java.util.List;

// Closures are contexts/scopes for the purpose of IR building.  They are self-contained and accumulate instructions
// that don't merge into the flow of the containing scope.  They are manipulated as an unit.
// Their parents are always execution scopes.

public class IRClosure extends IRScope {
    public final Label startLabel; // Label for the start of the closure (used to implement redo)
    public final Label endLabel;   // Label for the end of the closure (used to implement retry)
    public final int closureId;    // Unique id for this closure within the nearest ancestor method.

    private int nestingDepth;      // How many nesting levels within a method is this closure nested in?

    private boolean isBeginEndBlock;

    // Block parameters
    private List<Operand> blockArgs;

    /** The parameter names, for Proc#parameters */
    private String[] parameterList;

    private Arity arity;
    private int argumentType;

    /** Added for interp/JIT purposes */
    private BlockBody body;

    /** Added for JIT purposes */
    private Handle handle;

    // Used by other constructions and by IREvalScript as well
    protected IRClosure(IRManager manager, IRScope lexicalParent, String fileName, int lineNumber, StaticScope staticScope, String prefix) {
        super(manager, lexicalParent, null, fileName, lineNumber, staticScope);

        this.startLabel = getNewLabel(prefix + "START");
        this.endLabel = getNewLabel(prefix + "END");
        this.closureId = lexicalParent.getNextClosureId();
        setName(prefix + closureId);
        this.body = null;
        this.parameterList = new String[] {};

        // set nesting depth
        int n = 0;
        IRScope s = this.getLexicalParent();
        while (s instanceof IRClosure) {
            n++;
            s = s.getLexicalParent();
        }
        this.nestingDepth = n;
    }

    /** Used by cloning code */
    /* Inlining generates a new name and id and basic cloning will reuse the originals name */
    protected IRClosure(IRClosure c, IRScope lexicalParent, int closureId, String fullName) {
        super(c, lexicalParent);
        this.closureId = closureId;
        super.setName(fullName);
        this.startLabel = getNewLabel(getName() + "_START");
        this.endLabel = getNewLabel(getName() + "_END");
        if (getManager().isDryRun()) {
            this.body = null;
        } else {
            this.body = new InterpretedIRBlockBody(this, c.body.arity(), c.body.getArgumentType());
        }
        this.blockArgs = new ArrayList<Operand>();
        this.arity = c.arity;
    }

    public IRClosure(IRManager manager, IRScope lexicalParent, int lineNumber, StaticScope staticScope, Arity arity, int argumentType) {
        this(manager, lexicalParent, lineNumber, staticScope, arity, argumentType, "_CLOSURE_");
    }

    public IRClosure(IRManager manager, IRScope lexicalParent, int lineNumber, StaticScope staticScope, Arity arity, int argumentType, String prefix) {
        this(manager, lexicalParent, lineNumber, staticScope, arity, argumentType, prefix, false);
    }

    public IRClosure(IRManager manager, IRScope lexicalParent, int lineNumber, StaticScope staticScope, Arity arity, int argumentType, String prefix, boolean isBeginEndBlock) {
        this(manager, lexicalParent, lexicalParent.getFileName(), lineNumber, staticScope, prefix);
        this.blockArgs = new ArrayList<Operand>();
        this.argumentType = argumentType;
        this.arity = arity;
        lexicalParent.addClosure(this);

        if (getManager().isDryRun()) {
            this.body = null;
        } else {
            this.body = new InterpretedIRBlockBody(this, arity, argumentType);
            if (staticScope != null && !isBeginEndBlock) {
                staticScope.setIRScope(this);
                staticScope.setScopeType(this.getScopeType());
            }
        }

        this.nestingDepth++;
    }

    public InterpreterContext prepareInterpreterContext(Operand self) {
        if (interpreterContext != null) return interpreterContext; // Already prepared

        initScope(false);

        Instr[] linearizedInstrArray = prepareInstructions();

        interpreterContext = new ClosureInterpreterContext(getTemporaryVariablesCount(), getBooleanVariablesCount(),
                getFixnumVariablesCount(), getFloatVariablesCount(),getFlags().clone(), linearizedInstrArray,
                self, getStaticScope(), getBlockBody());

        return interpreterContext;
    }

    @Override
    public synchronized InterpreterContext prepareForInterpretation() {
        // This should have already been prepared during preparation of parent scopes.
        // If this is null, it would be a bug and let users throw a NPE.
        return interpreterContext;
    }

    public void setBeginEndBlock() {
        this.isBeginEndBlock = true;
    }

    public boolean isBeginEndBlock() {
        return isBeginEndBlock;
    }

    public void setParameterList(String[] parameterList) {
        this.parameterList = parameterList;
        if (!getManager().isDryRun()) {
            ((InterpretedIRBlockBody)this.body).setParameterList(parameterList);
        }
    }

    public String[] getParameterList() {
        return this.parameterList;
    }

    @Override
    public int getNextClosureId() {
        return getLexicalParent().getNextClosureId();
    }

    @Override
    public LocalVariable getNewFlipStateVariable() {
        throw new RuntimeException("Cannot get flip variables from closures.");
    }

    @Override
    public TemporaryLocalVariable createTemporaryVariable() {
        return getNewTemporaryVariable(TemporaryVariableType.CLOSURE);
    }

    @Override
    public TemporaryLocalVariable getNewTemporaryVariable(TemporaryVariableType type) {
        if (type == TemporaryVariableType.CLOSURE) {
            temporaryVariableIndex++;
            return new TemporaryClosureVariable(closureId, temporaryVariableIndex);
        }

        return super.getNewTemporaryVariable(type);
    }

    @Override
    public Label getNewLabel() {
        return getNewLabel("CL" + closureId + "_LBL");
    }

    @Override
    public IRScopeType getScopeType() {
        return IRScopeType.CLOSURE;
    }

    @Override
    public boolean isTopLocalVariableScope() {
        return false;
    }

    @Override
    public boolean isFlipScope() {
        return false;
    }

    @Override
    public void addInstr(Instr i) {
        // Accumulate block arguments
        if (i instanceof ReceiveRestArgInstr) blockArgs.add(new Splat(((ReceiveRestArgInstr)i).getResult()));
        else if (i instanceof ReceiveArgBase) blockArgs.add(((ReceiveArgBase) i).getResult());

        super.addInstr(i);
    }

    public Operand[] getBlockArgs() {
        return blockArgs.toArray(new Operand[blockArgs.size()]);
    }

    public String toStringBody() {
        StringBuilder buf = new StringBuilder();
        buf.append(getName()).append(" = { \n");

        CFG c = getCFG();
        if (c != null) {
            buf.append("\nCFG:\n").append(c.toStringGraph()).append("\nInstructions:\n").append(c.toStringInstrs());
        } else {
            buf.append(toStringInstrs());
        }
        buf.append("\n}\n\n");
        return buf.toString();
    }

    public BlockBody getBlockBody() {
        return body;
    }

    @Override
    protected LocalVariable findExistingLocalVariable(String name, int scopeDepth) {
        LocalVariable lvar = lookupExistingLVar(name);
        if (lvar != null) return lvar;

        int newDepth = scopeDepth - 1;

        return newDepth >= 0 ? getLexicalParent().findExistingLocalVariable(name, newDepth) : null;
    }

    public LocalVariable getNewLocalVariable(String name, int depth) {
        if (depth == 0 && !(this instanceof IRFor)) {
            LocalVariable lvar = new ClosureLocalVariable(this, name, 0, getStaticScope().addVariableThisScope(name));
            localVars.put(name, lvar);
            return lvar;
        } else {
            IRScope s = this;
            int     d = depth;
            do {
                // account for for-loops
                while (s instanceof IRFor) {
                    depth++;
                    s = s.getLexicalParent();
                }

                // walk up
                d--;
                if (d >= 0) s = s.getLexicalParent();
            } while (d >= 0);

            return s.getNewLocalVariable(name, 0).cloneForDepth(depth);
        }
    }

    @Override
    public LocalVariable getLocalVariable(String name, int depth) {
        // AST doesn't seem to be implementing shadowing properly and sometimes
        // has the wrong depths which screws up variable access. So, we implement
        // shadowing here by searching for an existing local var from depth 0 and upwards.
        //
        // Check scope depths for 'a' in the closure in the following snippet:
        //
        //   "a = 1; foo(1) { |(a)| a }"
        //
        // In "(a)", it is 0 (correct), but in the body, it is 1 (incorrect)

        LocalVariable lvar = null;
        IRScope s = this;
        int d = depth;
        do {
            // account for for-loops
            while (s instanceof IRFor) {
                depth++;
                s = s.getLexicalParent();
            }

            // lookup
            lvar = s.lookupExistingLVar(name);

            // walk up
            d--;
            if (d >= 0) s = s.getLexicalParent();
        } while (lvar == null && d >= 0);

        if (lvar == null) {
            // Create a new var at requested/adjusted depth
            lvar = s.getNewLocalVariable(name, 0).cloneForDepth(depth);
        } else {
            // Find # of lexical scopes we walked up to find 'lvar'.
            // We need a copy of 'lvar' usable at that depth
            int lvarDepth = depth - (d + 1);
            if (lvar.getScopeDepth() != lvarDepth) lvar = lvar.cloneForDepth(lvarDepth);
        }

        return lvar;
    }

    public int getNestingDepth() {
        return nestingDepth;
    }

    protected IRClosure cloneForInlining(CloneInfo ii, IRClosure clone) {
        clone.nestingDepth  = this.nestingDepth;
        // FIXME: This is fragile. Untangle this state.
        // Why is this being copied over to InterpretedIRBlockBody?
        clone.setParameterList(this.parameterList);
        clone.isBeginEndBlock = this.isBeginEndBlock;

        SimpleCloneInfo clonedII = ii.cloneForCloningClosure(clone);

        if (getCFG() != null) {
            clone.setCFG(getCFG().clone(clonedII, clone));
        } else {
            for (Instr i: getInstrs()) {
                clone.addInstr(i.clone(clonedII));
            }
        }

        return clone;
    }

    public IRClosure cloneForInlining(CloneInfo ii) {
        IRClosure clonedClosure;
        IRScope lexicalParent = ii.getScope();

        if (ii instanceof SimpleCloneInfo && !((SimpleCloneInfo)ii).isEnsureBlockCloneMode()) {
            clonedClosure = new IRClosure(this, lexicalParent, closureId, getName());
        } else {
            int id = lexicalParent.getNextClosureId();
            String fullName = lexicalParent.getName() + "_CLOSURE_CLONE_" + id;
            clonedClosure = new IRClosure(this, lexicalParent, id, fullName);
        }

        // WrappedIRClosure should always have a single unique IRClosure in them so we should
        // not end up adding n copies of the same closure as distinct clones...
        lexicalParent.addClosure(clonedClosure);

        return cloneForInlining(ii, clonedClosure);
    }

    @Override
    public void setName(String name) {
        // We can distinguish closures only with parent scope name
        super.setName(getLexicalParent().getName() + name);
    }

    public Arity getArity() {
        return arity;
    }

    public int getArgumentType() {
        return argumentType;
    }

    public void setHandle(Handle handle) {
        this.handle = handle;
    }

    public Handle getHandle() {
        return handle;
    }
}
