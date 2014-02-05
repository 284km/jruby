package org.jruby.ir.targets;

import org.jruby.Ruby;
import org.jruby.RubyBoolean;
import org.jruby.RubyClass;
import org.jruby.RubyFixnum;
import org.jruby.RubyModule;
import org.jruby.RubyFloat;
import org.jruby.RubyString;
import org.jruby.compiler.impl.SkinnyMethodAdapter;
import org.jruby.internal.runtime.GlobalVariables;
import org.jruby.internal.runtime.methods.CompiledIRMethod;
import org.jruby.ir.IRClassBody;
import org.jruby.ir.IRVisitor;
import org.jruby.ir.IRMetaClassBody;
import org.jruby.ir.IRClosure;
import org.jruby.ir.IRMethod;
import org.jruby.ir.IRModuleBody;
import org.jruby.ir.IRScope;
import org.jruby.ir.IRScriptBody;
import org.jruby.ir.Tuple;
import org.jruby.ir.instructions.AliasInstr;
import org.jruby.ir.instructions.AttrAssignInstr;
import org.jruby.ir.instructions.BEQInstr;
import org.jruby.ir.instructions.BFalseInstr;
import org.jruby.ir.instructions.BNEInstr;
import org.jruby.ir.instructions.BNilInstr;
import org.jruby.ir.instructions.BTrueInstr;
import org.jruby.ir.instructions.BUndefInstr;
import org.jruby.ir.instructions.BlockGivenInstr;
import org.jruby.ir.instructions.BreakInstr;
import org.jruby.ir.instructions.BuildLambdaInstr;
import org.jruby.ir.instructions.CallInstr;
import org.jruby.ir.instructions.CheckArgsArrayArityInstr;
import org.jruby.ir.instructions.CheckArityInstr;
import org.jruby.ir.instructions.ClassSuperInstr;
import org.jruby.ir.instructions.ConstMissingInstr;
import org.jruby.ir.instructions.CopyInstr;
import org.jruby.ir.instructions.DefineClassInstr;
import org.jruby.ir.instructions.DefineClassMethodInstr;
import org.jruby.ir.instructions.DefineInstanceMethodInstr;
import org.jruby.ir.instructions.DefineMetaClassInstr;
import org.jruby.ir.instructions.DefineModuleInstr;
import org.jruby.ir.instructions.EQQInstr;
import org.jruby.ir.instructions.ExceptionRegionEndMarkerInstr;
import org.jruby.ir.instructions.ExceptionRegionStartMarkerInstr;
import org.jruby.ir.instructions.GVarAliasInstr;
import org.jruby.ir.instructions.GetClassVarContainerModuleInstr;
import org.jruby.ir.instructions.GetClassVariableInstr;
import org.jruby.ir.instructions.GetEncodingInstr;
import org.jruby.ir.instructions.GetFieldInstr;
import org.jruby.ir.instructions.GetGlobalVariableInstr;
import org.jruby.ir.instructions.InheritanceSearchConstInstr;
import org.jruby.ir.instructions.InstanceSuperInstr;
import org.jruby.ir.instructions.Instr;
import org.jruby.ir.instructions.JumpInstr;
import org.jruby.ir.instructions.LabelInstr;
import org.jruby.ir.instructions.LexicalSearchConstInstr;
import org.jruby.ir.instructions.LineNumberInstr;
import org.jruby.ir.instructions.LoadLocalVarInstr;
import org.jruby.ir.instructions.Match2Instr;
import org.jruby.ir.instructions.Match3Instr;
import org.jruby.ir.instructions.MatchInstr;
import org.jruby.ir.instructions.MethodLookupInstr;
import org.jruby.ir.instructions.ModuleVersionGuardInstr;
import org.jruby.ir.instructions.NoResultCallInstr;
import org.jruby.ir.instructions.NonlocalReturnInstr;
import org.jruby.ir.instructions.NopInstr;
import org.jruby.ir.instructions.NotInstr;
import org.jruby.ir.instructions.OptArgMultipleAsgnInstr;
import org.jruby.ir.instructions.PopBindingInstr;
import org.jruby.ir.instructions.PopFrameInstr;
import org.jruby.ir.instructions.ProcessModuleBodyInstr;
import org.jruby.ir.instructions.PushBindingInstr;
import org.jruby.ir.instructions.PushFrameInstr;
import org.jruby.ir.instructions.PutClassVariableInstr;
import org.jruby.ir.instructions.PutConstInstr;
import org.jruby.ir.instructions.PutFieldInstr;
import org.jruby.ir.instructions.PutGlobalVarInstr;
import org.jruby.ir.instructions.RaiseArgumentErrorInstr;
import org.jruby.ir.instructions.ReceiveClosureInstr;
import org.jruby.ir.instructions.ReceiveJRubyExceptionInstr;
import org.jruby.ir.instructions.ReceiveOptArgInstr;
import org.jruby.ir.instructions.ReceivePostReqdArgInstr;
import org.jruby.ir.instructions.ReceivePreReqdArgInstr;
import org.jruby.ir.instructions.ReceiveRestArgInstr;
import org.jruby.ir.instructions.ReceiveRubyExceptionInstr;
import org.jruby.ir.instructions.ReceiveSelfInstr;
import org.jruby.ir.instructions.RecordEndBlockInstr;
import org.jruby.ir.instructions.ReqdArgMultipleAsgnInstr;
import org.jruby.ir.instructions.RescueEQQInstr;
import org.jruby.ir.instructions.RestArgMultipleAsgnInstr;
import org.jruby.ir.instructions.ReturnInstr;
import org.jruby.ir.instructions.RuntimeHelperCall;
import org.jruby.ir.instructions.SearchConstInstr;
import org.jruby.ir.instructions.StoreLocalVarInstr;
import org.jruby.ir.instructions.ThreadPollInstr;
import org.jruby.ir.instructions.ThrowExceptionInstr;
import org.jruby.ir.instructions.ToAryInstr;
import org.jruby.ir.instructions.UndefMethodInstr;
import org.jruby.ir.instructions.UnresolvedSuperInstr;
import org.jruby.ir.instructions.YieldInstr;
import org.jruby.ir.instructions.ZSuperInstr;
import org.jruby.ir.instructions.boxing.AluInstr;
import org.jruby.ir.instructions.boxing.BoxBooleanInstr;
import org.jruby.ir.instructions.boxing.BoxFixnumInstr;
import org.jruby.ir.instructions.boxing.BoxFloatInstr;
import org.jruby.ir.instructions.boxing.UnboxBooleanInstr;
import org.jruby.ir.instructions.boxing.UnboxFixnumInstr;
import org.jruby.ir.instructions.boxing.UnboxFloatInstr;
import org.jruby.ir.operands.*;
import org.jruby.ir.operands.Boolean;
import org.jruby.ir.operands.Float;
import org.jruby.ir.runtime.IRRuntimeHelpers;
import org.jruby.ir.instructions.defined.BackrefIsMatchDataInstr;
import org.jruby.ir.instructions.defined.ClassVarIsDefinedInstr;
import org.jruby.ir.instructions.defined.GetBackrefInstr;
import org.jruby.ir.instructions.defined.GetDefinedConstantOrMethodInstr;
import org.jruby.ir.instructions.defined.GetErrorInfoInstr;
import org.jruby.ir.instructions.defined.GlobalIsDefinedInstr;
import org.jruby.ir.instructions.defined.HasInstanceVarInstr;
import org.jruby.ir.instructions.defined.IsMethodBoundInstr;
import org.jruby.ir.instructions.defined.MethodDefinedInstr;
import org.jruby.ir.instructions.defined.MethodIsPublicInstr;
import org.jruby.ir.instructions.defined.RestoreErrorInfoInstr;
import org.jruby.ir.instructions.defined.SuperMethodBoundInstr;
import org.jruby.runtime.Helpers;
import org.jruby.parser.StaticScope;
import org.jruby.runtime.Block;
import org.jruby.runtime.DynamicScope;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.Visibility;
import org.jruby.runtime.builtin.IRubyObject;
import org.jruby.runtime.builtin.InstanceVariables;
import org.jruby.runtime.invokedynamic.InvokeDynamicSupport;
import org.jruby.util.JRubyClassLoader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.jruby.RubyArray;
import org.jruby.RubyRange;

import static org.jruby.util.CodegenUtils.ci;
import static org.jruby.util.CodegenUtils.p;
import static org.jruby.util.CodegenUtils.sig;

import org.jruby.util.cli.Options;
import org.jruby.util.log.Logger;
import org.jruby.util.log.LoggerFactory;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;

/**
 * Implementation of IRCompiler for the JVM.
 */
public class JVMVisitor extends IRVisitor {

    private static final Logger LOG = LoggerFactory.getLogger("JVMVisitor");
    public static final String DYNAMIC_SCOPE = "$dynamicScope";

    public JVMVisitor() {
        this.jvm = new JVM();
    }

    public static Class compile(Ruby ruby, IRScope scope, JRubyClassLoader jrubyClassLoader) {
        // run compiler
        JVMVisitor target = new JVMVisitor();

        target.codegen(scope);

//        try {
//            FileOutputStream fos = new FileOutputStream("tmp.class");
//            fos.write(target.code());
//            fos.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        return jrubyClassLoader.defineClass(JVM.scriptToClass(scope.getName()), target.code());
    }

    public byte[] code() {
        return jvm.code();
    }

    public void codegen(IRScope scope) {
        if (scope instanceof IRScriptBody) {
            codegen((IRScriptBody)scope);
        }
    }

    public void codegen(IRScriptBody script) {
        this.script = script;
        emit(script);
    }

    public String emitScope(IRScope scope, String name, int arity) {
        this.currentScope = scope;
        name = name + scope.getLineNumber();

        boolean debug = false;

        Tuple<Instr[], Map<Integer,Label[]>> t = scope.prepareForCompilation();
        Instr[] instrs = t.a;

        Map<Integer, Label[]> jumpTable = t.b;
        Map<Integer, Integer> rescueTable = scope.getRescueMap();

        if (Options.IR_COMPILER_DEBUG.load()) {
            StringBuilder b = new StringBuilder();

            b.append("\n\nLinearized instructions for JIT:\n");

            int i = 0;
            for (Instr instr : instrs) {
                if (i > 0) b.append("\n");

                b.append("  ").append(i).append('\t').append(instr);

                i++;
            }

            b.append("\n\nRescues: \n" + rescueTable);

            LOG.info("Starting JVM compilation on scope " + scope);
            LOG.info(b.toString());
        }

        jvm.pushmethodVarargs(name);

        // UGLY hack for blocks, which still have their scopes pushed before invocation
        // Scope management for blocks needs to be figured out
        if (scope instanceof IRClosure) {
            jvm.method().loadContext();
            jvm.method().invokeVirtual(Type.getType(ThreadContext.class), Method.getMethod("org.jruby.runtime.DynamicScope getCurrentScope()"));
            jvmStoreLocal(DYNAMIC_SCOPE);
        }

        IRBytecodeAdapter m = jvm.method();
        org.objectweb.asm.Label[] allLabels = new org.objectweb.asm.Label[instrs.length + 1];
        for (int i = 0; i < allLabels.length; i++) {
            allLabels[i] = new org.objectweb.asm.Label();
        }

        // set up try/catch table
        int[][] tryCatchTable = new int[instrs.length][];
        List<int[]> allCatches = new ArrayList<int[]>();
        int[] rescueRange = null;
        for (int i = 0; i < instrs.length; i++) {
            Instr instr = instrs[i];

            int rescueIPC = rescueTable.get(instr.getIPC());
            if (rescueIPC != -1) {
                if (rescueRange == null) {
                    // new range
                    rescueRange = new int[]{i, i+1, rescueIPC};
                    continue;
                } else {
                    if (rescueIPC != rescueRange[2]) {
                        // new range
                        allCatches.add(rescueRange);
                        rescueRange = new int[]{i, i+1, rescueIPC};
                    } else {
                        rescueRange[1] = i + 1;
                    }
                }
            } else {
                if (rescueRange == null) continue;

                // found end of range, done
                allCatches.add(rescueRange);
                rescueRange = null;
            }
        }

        for (int[] range : allCatches) {
            if (debug) System.out.println("rescue range: " + Arrays.toString(range));
            jvm.method().adapter.trycatch(allLabels[range[0]], allLabels[range[1]], allLabels[range[2]], p(Throwable.class));
        }

        for (int i = 0; i < instrs.length; i++) {
            Instr instr = instrs[i];
            if (debug) System.out.println("ipc " + instr.getIPC() + " rescued by " + rescueTable.get(instr.getIPC()));
            if (debug) System.out.println("ipc " + instr.getIPC() + " is: " + instr + " (" + instr.getClass() + ")");

            if (jumpTable.get(i) != null) {
                for (Label label : jumpTable.get(i)) m.mark(jvm.methodData().getLabel(label));
            }

            // this is probably not efficient because it's putting a label at every instruction
            m.mark(allLabels[i]);

            visit(instr);
        }

        // mark last label
        m.mark(allLabels[allLabels.length - 1]);

        jvm.popmethod();

        return name;
    }

    public void emit(IRScriptBody script) {
        String clsName = jvm.scriptToClass(script.getName());
        jvm.pushscript(clsName, script.getFileName());

        emitScope(script, "__script__", 0);

        jvm.cls().visitEnd();
        jvm.popclass();
    }

    public String[] emit(IRMethod method) {
        String name = emitScope(method, method.getName(), method.getCallArgs().length);

        // Emit code for all nested closures
        for (IRClosure c: method.getClosures()) {
            emit(c);
        }

        // return array of class name and generated method name
        return new String[]{jvm.clsData().clsName, name};
    }

    public String[] emit(IRClosure closure) {
        /* Compile the closure like a method */
        String name = closure.getName() + "__" + closure.getLexicalParent().getName();

        name = emitScope(closure, name, 0);

        /* .. Build a CompiledIRBlockBody object here ... */
        /* .. and bind that with the "method" emitted ... */

        // Emit code for all nested closures
        for (IRClosure c: closure.getClosures()) {
            emit(c);
        }

        // return array of class name and generated method name
        return new String[]{jvm.clsData().clsName, name};
        // push a method handle for binding purposes
        //jvm.method().pushHandle(jvm.clsData().clsName, name, closure.getStaticScope().getRequiredArgs());
    }

    public void emit(IRModuleBody method) {
        String name = method.getName();
        if (name.indexOf("DUMMY_MC") != -1) {
            name = "METACLASS";
        }

        name = emitScope(method, name, 0);

        // push a method handle for binding purposes
        jvm.method().pushHandle(jvm.clsData().clsName, name, method.getStaticScope().getRequiredArgs());
    }

    public void visit(Instr instr) {
        instr.visit(this);
    }

    public void visit(Operand operand) {
        operand.visit(this);
    }

    private int getJVMLocalVarIndex(Variable variable) {
        if (variable instanceof TemporaryLocalVariable) {
            switch (((TemporaryLocalVariable)variable).getType()) {
            case FLOAT: return jvm.methodData().local(variable, JVM.DOUBLE_TYPE);
            case FIXNUM: return jvm.methodData().local(variable, JVM.LONG_TYPE);
            case BOOLEAN: return jvm.methodData().local(variable, JVM.BOOLEAN_TYPE);
            default: return jvm.methodData().local(variable);
            }
        } else {
            return jvm.methodData().local(variable);
        }
    }

    private int getJVMLocalVarIndex(String specialVar) {
        return jvm.methodData().local(specialVar);
    }

    private org.objectweb.asm.Label getJVMLabel(Label label) {
        return jvm.methodData().getLabel(label);
    }

    private void jvmStoreLocal(Variable variable) {
        if (variable instanceof TemporaryLocalVariable) {
            switch (((TemporaryLocalVariable)variable).getType()) {
            case FLOAT: jvm.method().adapter.dstore(getJVMLocalVarIndex(variable)); break;
            case FIXNUM: jvm.method().adapter.lstore(getJVMLocalVarIndex(variable)); break;
            case BOOLEAN: jvm.method().adapter.istore(getJVMLocalVarIndex(variable)); break;
            default: jvm.method().storeLocal(getJVMLocalVarIndex(variable)); break;
            }
        } else {
            jvm.method().storeLocal(getJVMLocalVarIndex(variable));
        }
    }

    private void jvmStoreLocal(String specialVar) {
        jvm.method().storeLocal(getJVMLocalVarIndex(specialVar));
    }

    private void jvmLoadLocal(Variable variable) {
        if (variable instanceof TemporaryLocalVariable) {
            switch (((TemporaryLocalVariable)variable).getType()) {
            case FLOAT: jvm.method().adapter.dload(getJVMLocalVarIndex(variable)); break;
            case FIXNUM: jvm.method().adapter.lload(getJVMLocalVarIndex(variable)); break;
            case BOOLEAN: jvm.method().adapter.iload(getJVMLocalVarIndex(variable)); break;
            default: jvm.method().loadLocal(getJVMLocalVarIndex(variable)); break;
            }
        } else {
            jvm.method().loadLocal(getJVMLocalVarIndex(variable));
        }
    }

    private void jvmLoadLocal(String specialVar) {
        jvm.method().loadLocal(getJVMLocalVarIndex(specialVar));
    }

    // JVM maintains a stack of ClassData (for nested classes being compiled)
    // Each class maintains a stack of MethodData (for methods being compiled in the class)
    // MethodData wraps a IRBytecodeAdapter which wraps a SkinnyMethodAdapter which has a ASM MethodVisitor which emits bytecode
    // A long chain of indirection: JVM -> MethodData -> IRBytecodeAdapter -> SkinnyMethodAdapter -> ASM.MethodVisitor
    // In some places, methods reference JVM -> MethodData -> IRBytecodeAdapter (via jvm.method()) and ask it to walk the last 2 links
    // In other places, methods reference JVM -> MethodData -> IRBytecodeAdapter -> SkinnyMethodAdapter (via jvm.method().adapter) and ask it to walk the last link
    // Can this be cleaned up to either (a) get rid of IRBytecodeAdapter OR (b) implement passthru' methods for SkinnyMethodAdapter methods (like the others it implements)?

    @Override
    public void AliasInstr(AliasInstr aliasInstr) {
        IRBytecodeAdapter m = jvm.method();
        m.loadLocal(0);
        m.loadLocal(getJVMLocalVarIndex(aliasInstr.getReceiver()));
        m.adapter.ldc(((StringLiteral) aliasInstr.getNewName()).string);
        m.adapter.ldc(((StringLiteral) aliasInstr.getOldName()).string);
        m.invokeHelper("defineAlias", IRubyObject.class, ThreadContext.class, IRubyObject.class, Object.class, Object.class);
        m.adapter.pop();
    }

    @Override
    public void AttrAssignInstr(AttrAssignInstr attrAssignInstr) {
        jvm.method().loadLocal(0);
        visit(attrAssignInstr.getReceiver());
        for (Operand operand : attrAssignInstr.getCallArgs()) {
            visit(operand);
        }

        jvm.method().invokeOther(attrAssignInstr.getMethodAddr().getName(), attrAssignInstr.getCallArgs().length);
        jvm.method().adapter.pop();
    }

    @Override
    public void BEQInstr(BEQInstr beqInstr) {
        jvm.method().loadLocal(0);
        visit(beqInstr.getArg1());
        visit(beqInstr.getArg2());
        jvm.method().invokeHelper("BEQ", boolean.class, ThreadContext.class, IRubyObject.class, IRubyObject.class);
        jvm.method().adapter.iftrue(getJVMLabel(beqInstr.getJumpTarget()));
    }

    @Override
    public void BFalseInstr(BFalseInstr bFalseInstr) {
        Operand arg1 = bFalseInstr.getArg1();
        visit(arg1);
        // this is a gross hack because we don't have distinction in boolean instrs between boxed and unboxed
        if (!(arg1 instanceof TemporaryBooleanVariable) && !(arg1 instanceof UnboxedBoolean)) {
            // unbox
            jvm.method().adapter.invokeinterface(p(IRubyObject.class), "isTrue", sig(boolean.class));
        }
        jvm.method().bfalse(getJVMLabel(bFalseInstr.getJumpTarget()));
    }

    @Override
    public void BlockGivenInstr(BlockGivenInstr blockGivenInstr) {
        jvm.method().loadRuntime();
        visit(blockGivenInstr.getBlockArg());
        jvm.method().invokeVirtual(Type.getType(Block.class), Method.getMethod("boolean isGiven()"));
        jvm.method().invokeVirtual(Type.getType(Ruby.class), Method.getMethod("org.jruby.RubyBoolean newBoolean(boolean)"));
        jvmStoreLocal(blockGivenInstr.getResult());
    }

    private void loadFloatArg(Operand arg) {
        if (arg instanceof Variable) {
            visit(arg);
        } else {
            double val;
            if (arg instanceof Float) {
                val = ((Float)arg).value;
            } else if (arg instanceof Fixnum) {
                val = (double)((Fixnum)arg).value;
            } else {
                // Should not happen -- so, forcing an exception.
                throw new RuntimeException("Non-float/fixnum in loadFloatArg!" + arg);
            }
            jvm.method().adapter.ldc(val);
        }
    }

    private void loadFixnumArg(Operand arg) {
        if (arg instanceof Variable) {
            visit(arg);
        } else {
            long val;
            if (arg instanceof Float) {
                val = (long)((Float)arg).value;
            } else if (arg instanceof Fixnum) {
                val = ((Fixnum)arg).value;
            } else {
                // Should not happen -- so, forcing an exception.
                throw new RuntimeException("Non-float/fixnum in loadFixnumArg!" + arg);
            }
            jvm.method().adapter.ldc(val);
        }
    }

    private void loadBooleanArg(Operand arg) {
        if (arg instanceof Variable) {
            visit(arg);
        } else {
            boolean val;
            if (arg instanceof UnboxedBoolean) {
                val = ((UnboxedBoolean)arg).isTrue();
            } else {
                // Should not happen -- so, forcing an exception.
                throw new RuntimeException("Non-float/fixnum in loadFixnumArg!" + arg);
            }
            jvm.method().adapter.ldc(val);
        }
    }

    @Override
    public void BoxFloatInstr(BoxFloatInstr instr) {
        IRBytecodeAdapter   m = jvm.method();
        SkinnyMethodAdapter a = m.adapter;

        // Load runtime
        m.loadContext();
        a.getfield(p(ThreadContext.class), "runtime", ci(Ruby.class));

        // Get unboxed float
        loadFloatArg(instr.getValue());

        // Box the float
        a.invokevirtual(p(Ruby.class), "newFloat", sig(RubyFloat.class, double.class));

        // Store it
        jvmStoreLocal(instr.getResult());
    }

    @Override
    public void BoxFixnumInstr(BoxFixnumInstr instr) {
        IRBytecodeAdapter   m = jvm.method();
        SkinnyMethodAdapter a = m.adapter;

        // Load runtime
        m.loadContext();
        a.getfield(p(ThreadContext.class), "runtime", ci(Ruby.class));

        // Get unboxed fixnum
        loadFixnumArg(instr.getValue());

        // Box the fixnum
        a.invokevirtual(p(Ruby.class), "newFixnum", sig(RubyFixnum.class, long.class));

        // Store it
        jvmStoreLocal(instr.getResult());
    }

    @Override
    public void BoxBooleanInstr(BoxBooleanInstr instr) {
        IRBytecodeAdapter   m = jvm.method();
        SkinnyMethodAdapter a = m.adapter;

        // Load runtime
        m.loadContext();
        a.getfield(p(ThreadContext.class), "runtime", ci(Ruby.class));

        // Get unboxed boolean
        loadBooleanArg(instr.getValue());

        // Box the fixnum
        a.invokevirtual(p(Ruby.class), "newBoolean", sig(RubyBoolean.class, boolean.class));

        // Store it
        jvmStoreLocal(instr.getResult());
    }

    @Override
    public void UnboxFloatInstr(UnboxFloatInstr instr) {
        // Load boxed value
        visit(instr.getValue());

        // Unbox it
        jvm.method().invokeIRHelper("unboxFloat", sig(double.class, IRubyObject.class));

        // Store it
        jvmStoreLocal(instr.getResult());
    }

    @Override
    public void UnboxFixnumInstr(UnboxFixnumInstr instr) {
        // Load boxed value
        visit(instr.getValue());

        // Unbox it
        jvm.method().invokeIRHelper("unboxFixnum", sig(long.class, IRubyObject.class));

        // Store it
        jvmStoreLocal(instr.getResult());
    }

    @Override
    public void UnboxBooleanInstr(UnboxBooleanInstr instr) {
        // Load boxed value
        visit(instr.getValue());

        // Unbox it
        jvm.method().invokeIRHelper("unboxBoolean", sig(boolean.class, IRubyObject.class));

        // Store it
        jvmStoreLocal(instr.getResult());
    }

    public void AluInstr(AluInstr instr) {
        IRBytecodeAdapter   m = jvm.method();
        SkinnyMethodAdapter a = m.adapter;

        // Load args
        visit(instr.getArg1());
        visit(instr.getArg2());

        // Compute result
        switch (instr.getOperation()) {
            case FADD: a.dadd(); break;
            case FSUB: a.dsub(); break;
            case FMUL: a.dmul(); break;
            case FDIV: a.ddiv(); break;
            case FLT: m.invokeIRHelper("flt", sig(boolean.class, double.class, double.class)); break; // annoying to have to do it in a method
            case FGT: m.invokeIRHelper("fgt", sig(boolean.class, double.class, double.class)); break; // annoying to have to do it in a method
            case IADD: a.ladd(); break;
            case ISUB: a.lsub(); break;
            case IMUL: a.lmul(); break;
            case IDIV: a.ldiv(); break;
            case ILT: m.invokeIRHelper("ilt", sig(boolean.class, long.class, long.class)); break; // annoying to have to do it in a method
            case IGT: m.invokeIRHelper("igt", sig(boolean.class, long.class, long.class)); break; // annoying to have to do it in a method
            default: throw new RuntimeException("UNHANDLED!");
        }

        // Store it
        jvmStoreLocal(instr.getResult());
    }

    @Override
    public void BNEInstr(BNEInstr bneinstr) {
        jvm.method().loadLocal(0);
        visit(bneinstr.getArg1());
        visit(bneinstr.getArg2());
        jvm.method().invokeHelper("BNE", boolean.class, ThreadContext.class, IRubyObject.class, IRubyObject.class);
        jvm.method().adapter.iftrue(getJVMLabel(bneinstr.getJumpTarget()));
    }

    @Override
    public void BNilInstr(BNilInstr bnilinstr) {
        visit(bnilinstr.getArg1());
        jvm.method().isNil();
        jvm.method().btrue(getJVMLabel(bnilinstr.getJumpTarget()));
    }

    @Override
    public void BreakInstr(BreakInstr breakInstr) {
        IRBytecodeAdapter   m = jvm.method();
        SkinnyMethodAdapter a = m.adapter;
        m.loadLocal(0); // context
        a.aload(1); // current scope
        // FIXME: This can also be done in the helper itself
        m.invokeVirtual(Type.getType(IRScope.class), Method.getMethod("org.jruby.ir.IRScope getIRScope()"));
        a.ldc(breakInstr.getScopeToReturnTo().getScopeId());
        visit(breakInstr.getReturnValue());
        // FIXME: emit block-type for the scope that is currently executing
        // For now, it is null
        m.pushNil();
        a.invokestatic(p(IRubyObject.class), "initiateBreak", sig(ThreadContext.class, IRScope.class, IRScope.class, IRubyObject.class, Block.Type.class));
    }

    @Override
    public void BTrueInstr(BTrueInstr btrueinstr) {
        Operand arg1 = btrueinstr.getArg1();
        visit(arg1);
        // this is a gross hack because we don't have distinction in boolean instrs between boxed and unboxed
        if (!(arg1 instanceof TemporaryBooleanVariable) && !(arg1 instanceof UnboxedBoolean)) {
            jvm.method().isTrue();
        }
        jvm.method().btrue(getJVMLabel(btrueinstr.getJumpTarget()));
    }

    @Override
    public void BUndefInstr(BUndefInstr bundefinstr) {
        visit(bundefinstr.getArg1());
        jvm.method().pushUndefined();
        jvm.method().adapter.if_acmpeq(getJVMLabel(bundefinstr.getJumpTarget()));
    }

    @Override
    public void CallInstr(CallInstr callInstr) {
        IRBytecodeAdapter m = jvm.method();
        String name = callInstr.getMethodAddr().getName();
        Operand[] args = callInstr.getCallArgs();
        int numArgs = args.length;

        // This is disabled to remove another special-case path. Such things should be restored through
        // manipulations of the IR and numeric specializations in the future.
        /**
        if (   (name.equals("+") || name.equals("-") || name.equals("*") || name.equals("/"))
            && numArgs == 1
            && args[0] instanceof Fixnum
            && callInstr.getCallType() == CallType.NORMAL)
        {
            m.loadLocal(0);
            m.loadLocal(2); // dummy to satisfy signature of existing target linker (MathLinker)
            visit(callInstr.getReceiver());
            m.invokeFixnumOp(name, ((Fixnum)args[0]).value);
        } else {
         **/
            m.loadLocal(0); // tc
            m.loadLocal(2); // caller
            visit(callInstr.getReceiver());
            for (Operand operand : args) {
                visit(operand);
            }
            switch (callInstr.getCallType()) {
                case FUNCTIONAL:
                case VARIABLE:
                    m.invokeSelf(name, numArgs);
                    break;
                case NORMAL:
                    m.invokeOther(name, numArgs);
                    break;
                case SUPER:
                    m.invokeSuper(name, numArgs);
                    break;
            }
        //}

        jvmStoreLocal(callInstr.getResult());
    }

    @Override
    public void CheckArgsArrayArityInstr(CheckArgsArrayArityInstr checkargsarrayarityinstr) {
        jvm.method().loadContext();
        visit(checkargsarrayarityinstr.getArgsArray());
        jvm.method().adapter.pushInt(checkargsarrayarityinstr.required);
        jvm.method().adapter.pushInt(checkargsarrayarityinstr.opt);
        jvm.method().adapter.pushInt(checkargsarrayarityinstr.rest);
        jvm.method().invokeStatic(Type.getType(Helpers.class), Method.getMethod("void irCheckArgsArrayArity(org.jruby.runtime.ThreadContext, org.jruby.RubyArray, int, int, int)"));
    }

    @Override
    public void CheckArityInstr(CheckArityInstr checkarityinstr) {
        // no-op for now
    }

    @Override
    public void ClassSuperInstr(ClassSuperInstr classsuperinstr) {
        super.ClassSuperInstr(classsuperinstr);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void ConstMissingInstr(ConstMissingInstr constmissinginstr) {
        visit(constmissinginstr.getReceiver());
        jvm.method().adapter.checkcast("org/jruby/RubyModule");
        jvm.method().loadContext();
        jvm.method().adapter.ldc("const_missing");
        jvm.method().pushSymbol(constmissinginstr.getMissingConst());
        jvm.method().invokeVirtual(Type.getType(RubyModule.class), Method.getMethod("org.jruby.runtime.builtin.IRubyObject callMethod(org.jruby.runtime.ThreadContext, java.lang.String, org.jruby.runtime.builtin.IRubyObject)"));
    }

    @Override
    public void CopyInstr(CopyInstr copyinstr) {
        Operand  src = copyinstr.getSource();
        Variable res = copyinstr.getResult();
        if (res instanceof TemporaryFloatVariable) {
            loadFloatArg(src);
        } else if (res instanceof TemporaryFixnumVariable) {
            loadFixnumArg(src);
        } else {
            visit(src);
        }
        jvmStoreLocal(res);
    }

    @Override
    public void DefineClassInstr(DefineClassInstr defineclassinstr) {
        IRClassBody newIRClassBody = defineclassinstr.getNewIRClassBody();
        StaticScope scope = newIRClassBody.getStaticScope();
        if (scope.getRequiredArgs() > 3 || scope.getRestArg() >= 0 || scope.getOptionalArgs() != 0) {
            throw new RuntimeException("can't compile variable method: " + this);
        }

        String scopeString = Helpers.encodeScope(scope);

        IRBytecodeAdapter   m = jvm.method();
        SkinnyMethodAdapter a = m.adapter;

        // new CompiledIRMethod
        a.newobj(p(CompiledIRMethod.class));
        a.dup();

        // emit method body and get handle
        emit(newIRClassBody); // handle

        // add'l args for CompiledIRMethod constructor
        a.ldc(newIRClassBody.getName());
        a.ldc(newIRClassBody.getFileName());
        a.ldc(newIRClassBody.getLineNumber());

        // construct class with Helpers.newClassForIR
        a.aload(0); // ThreadContext
        a.ldc(newIRClassBody.getName()); // class name
        m.loadLocal(2); // self

        // create class
        m.loadLocal(0);
        visit(defineclassinstr.getContainer());
        m.invokeHelper("checkIsRubyModule", RubyModule.class, ThreadContext.class, Object.class);

        // superclass
        if (defineclassinstr.getSuperClass() instanceof Nil) {
            a.aconst_null();
        } else {
            visit(defineclassinstr.getSuperClass());
        }

        // is meta?
        a.ldc(newIRClassBody instanceof IRMetaClassBody);

        m.invokeHelper("newClassForIR", RubyClass.class, ThreadContext.class, String.class, IRubyObject.class, RubyModule.class, Object.class, boolean.class);

        //// static scope
        a.aload(0);
        a.aload(1);
        a.ldc(scopeString);
        a.invokestatic(p(Helpers.class), "decodeScope", "(Lorg/jruby/runtime/ThreadContext;Lorg/jruby/parser/StaticScope;Ljava/lang/String;)Lorg/jruby/parser/StaticScope;");
        a.swap();

        // set into StaticScope
        a.dup2();
        a.invokevirtual(p(StaticScope.class), "setModule", sig(void.class, RubyModule.class));

        a.getstatic(p(Visibility.class), "PUBLIC", ci(Visibility.class));
        a.swap();

        // invoke constructor
        a.invokespecial(p(CompiledIRMethod.class), "<init>", "(Ljava/lang/invoke/MethodHandle;Ljava/lang/String;Ljava/lang/String;ILorg/jruby/parser/StaticScope;Lorg/jruby/runtime/Visibility;Lorg/jruby/RubyModule;)V");

        // store
        jvmStoreLocal(defineclassinstr.getResult());
    }

    @Override
    public void DefineClassMethodInstr(DefineClassMethodInstr defineclassmethodinstr) {
        super.DefineClassMethodInstr(defineclassmethodinstr);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void DefineInstanceMethodInstr(DefineInstanceMethodInstr defineinstancemethodinstr) {
        IRMethod method = defineinstancemethodinstr.getMethod();
        StaticScope scope = method.getStaticScope();

        String scopeString = Helpers.encodeScope(scope);

        IRBytecodeAdapter   m = jvm.method();
        SkinnyMethodAdapter a = m.adapter;
        List<String[]> parameters = method.getArgDesc();

        a.aload(0); // ThreadContext
        String[] classAndMethod = emit(method); // handle
        jvm.method().pushHandleVarargs(classAndMethod[0], classAndMethod[1]);
        a.ldc(method.getName());
        a.aload(1);
        a.ldc(scopeString);
        a.ldc(method.getFileName());
        a.ldc(method.getLineNumber());
        a.ldc(Helpers.encodeParameterList(parameters));

        // add method
        a.invokestatic(p(IRRuntimeHelpers.class), "defCompiledIRMethod",
                sig(IRubyObject.class, ThreadContext.class, java.lang.invoke.MethodHandle.class, String.class,
                        StaticScope.class, String.class, String.class, int.class, String.class));

        a.pop();
    }

    @Override
    public void DefineMetaClassInstr(DefineMetaClassInstr definemetaclassinstr) {
        IRModuleBody metaClassBody = definemetaclassinstr.getMetaClassBody();
        StaticScope scope = metaClassBody.getStaticScope();
        if (scope.getRequiredArgs() > 3 || scope.getRestArg() >= 0 || scope.getOptionalArgs() != 0) {
            throw new RuntimeException("can't compile variable method: " + this);
        }

        String scopeString = Helpers.encodeScope(scope);

        IRBytecodeAdapter   m = jvm.method();
        SkinnyMethodAdapter a = m.adapter;

        // new CompiledIRMethod
        a.newobj(p(CompiledIRMethod.class));
        a.dup();

        // emit method body and get handle
        emit(metaClassBody); // handle

        // add'l args for CompiledIRMethod constructor
        a.ldc(metaClassBody.getName());
        a.ldc(metaClassBody.getFileName());
        a.ldc(metaClassBody.getLineNumber());

        //// static scope
        a.aload(0);
        a.aload(1);
        a.ldc(scopeString);
        a.invokestatic(p(Helpers.class), "decodeScope", "(Lorg/jruby/runtime/ThreadContext;Lorg/jruby/parser/StaticScope;Ljava/lang/String;)Lorg/jruby/parser/StaticScope;");

        // get singleton class
        m.loadRuntime();
        visit(definemetaclassinstr.getObject());
        m.invokeHelper("getSingletonClass", RubyClass.class, Ruby.class, IRubyObject.class);

        // set into StaticScope
        a.dup2();
        a.invokevirtual(p(StaticScope.class), "setModule", sig(void.class, RubyModule.class));

        a.getstatic(p(Visibility.class), "PUBLIC", ci(Visibility.class));
        a.swap();

        // invoke constructor
        a.invokespecial(p(CompiledIRMethod.class), "<init>", "(Ljava/lang/invoke/MethodHandle;Ljava/lang/String;Ljava/lang/String;ILorg/jruby/parser/StaticScope;Lorg/jruby/runtime/Visibility;Lorg/jruby/RubyModule;)V");

        // store
        jvmStoreLocal(definemetaclassinstr.getResult());
    }

    @Override
    public void DefineModuleInstr(DefineModuleInstr definemoduleinstr) {
        IRModuleBody newIRModuleBody = definemoduleinstr.getNewIRModuleBody();
        StaticScope scope = newIRModuleBody.getStaticScope();
        if (scope.getRequiredArgs() > 3 || scope.getRestArg() >= 0 || scope.getOptionalArgs() != 0) {
            throw new RuntimeException("can't compile variable method: " + this);
        }

        String scopeString = Helpers.encodeScope(scope);

        IRBytecodeAdapter   m = jvm.method();
        SkinnyMethodAdapter a = m.adapter;

        // new CompiledIRMethod
        a.newobj(p(CompiledIRMethod.class));
        a.dup();

        // emit method body and get handle
        emit(newIRModuleBody); // handle

        // add'l args for CompiledIRMethod constructor
        a.ldc(newIRModuleBody.getName());
        a.ldc(newIRModuleBody.getFileName());
        a.ldc(newIRModuleBody.getLineNumber());

        a.aload(0);
        a.aload(1);
        a.ldc(scopeString);
        a.invokestatic(p(Helpers.class), "decodeScope", "(Lorg/jruby/runtime/ThreadContext;Lorg/jruby/parser/StaticScope;Ljava/lang/String;)Lorg/jruby/parser/StaticScope;");

        // create module
        m.loadLocal(0);
        visit(definemoduleinstr.getContainer());
        m.invokeHelper("checkIsRubyModule", RubyModule.class, ThreadContext.class, Object.class);
        a.ldc(newIRModuleBody.getName());
        a.invokevirtual(p(RubyModule.class), "defineOrGetModuleUnder", sig(RubyModule.class, String.class));

        // set into StaticScope
        a.dup2();
        a.invokevirtual(p(StaticScope.class), "setModule", sig(void.class, RubyModule.class));

        a.getstatic(p(Visibility.class), "PUBLIC", ci(Visibility.class));
        a.swap();

        // invoke constructor
        a.invokespecial(p(CompiledIRMethod.class), "<init>", "(Ljava/lang/invoke/MethodHandle;Ljava/lang/String;Ljava/lang/String;ILorg/jruby/parser/StaticScope;Lorg/jruby/runtime/Visibility;Lorg/jruby/RubyModule;)V");

        // store
        jvmStoreLocal(definemoduleinstr.getResult());
    }

    @Override
    public void EQQInstr(EQQInstr eqqinstr) {
        super.EQQInstr(eqqinstr);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void ExceptionRegionEndMarkerInstr(ExceptionRegionEndMarkerInstr exceptionregionendmarkerinstr) {
        throw new RuntimeException("Marker instructions shouldn't reach compiler: " + exceptionregionendmarkerinstr);
    }

    @Override
    public void ExceptionRegionStartMarkerInstr(ExceptionRegionStartMarkerInstr exceptionregionstartmarkerinstr) {
        throw new RuntimeException("Marker instructions shouldn't reach compiler: " + exceptionregionstartmarkerinstr);
    }

    @Override
    public void GetClassVarContainerModuleInstr(GetClassVarContainerModuleInstr getclassvarcontainermoduleinstr) {
        super.GetClassVarContainerModuleInstr(getclassvarcontainermoduleinstr);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void GetClassVariableInstr(GetClassVariableInstr getclassvariableinstr) {
        super.GetClassVariableInstr(getclassvariableinstr);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void GetFieldInstr(GetFieldInstr getfieldinstr) {
        visit(getfieldinstr.getSource());
        jvm.method().getField(getfieldinstr.getRef());
        jvmStoreLocal(getfieldinstr.getResult());
    }

    @Override
    public void GetGlobalVariableInstr(GetGlobalVariableInstr getglobalvariableinstr) {
        Operand source = getglobalvariableinstr.getSource();
        GlobalVariable gvar = (GlobalVariable)source;
        String name = gvar.getName();
        jvm.method().loadRuntime();
        jvm.method().invokeVirtual(Type.getType(Ruby.class), Method.getMethod("org.jruby.internal.runtime.GlobalVariables getGlobalVariables()"));
        jvm.method().adapter.ldc(name);
        jvm.method().invokeVirtual(Type.getType(GlobalVariables.class), Method.getMethod("org.jruby.runtime.builtin.IRubyObject get(String)"));
        jvmStoreLocal(getglobalvariableinstr.getResult());
    }

    @Override
    public void GVarAliasInstr(GVarAliasInstr gvaraliasinstr) {
        super.GVarAliasInstr(gvaraliasinstr);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void InheritanceSearchConstInstr(InheritanceSearchConstInstr inheritancesearchconstinstr) {
        jvm.method().loadLocal(0);
        visit(inheritancesearchconstinstr.getCurrentModule());

        // TODO: private consts
        jvm.method().inheritanceSearchConst(inheritancesearchconstinstr.getConstName());
        jvmStoreLocal(inheritancesearchconstinstr.getResult());
    }

    @Override
    public void InstanceSuperInstr(InstanceSuperInstr instancesuperinstr) {
        super.InstanceSuperInstr(instancesuperinstr);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void JumpInstr(JumpInstr jumpinstr) {
        jvm.method().goTo(getJVMLabel(jumpinstr.getJumpTarget()));
    }

    @Override
    public void LabelInstr(LabelInstr labelinstr) {
        jvm.method().mark(getJVMLabel(labelinstr.getLabel()));
    }

    @Override
    public void LexicalSearchConstInstr(LexicalSearchConstInstr lexicalsearchconstinstr) {
        super.LexicalSearchConstInstr(lexicalsearchconstinstr);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void LineNumberInstr(LineNumberInstr linenumberinstr) {
        jvm.method().adapter.line(linenumberinstr.getLineNumber());
    }

    @Override
    public void LoadLocalVarInstr(LoadLocalVarInstr loadlocalvarinstr) {
        IRBytecodeAdapter m = jvm.method();
        jvmLoadLocal(DYNAMIC_SCOPE);
        int depth = loadlocalvarinstr.getLocalVar().getScopeDepth();
        // TODO should not have to subtract 1
        int location = loadlocalvarinstr.getLocalVar().getLocation() - 1;
        // TODO if we can avoid loading nil unnecessarily, it could be a big win
        switch (depth) {
            case 0:
                switch (location) {
                    case 0:
                        m.pushNil();
                        m.adapter.invokevirtual(p(DynamicScope.class), "getValueZeroDepthZeroOrNil", sig(IRubyObject.class, IRubyObject.class));
                        return;
                    case 1:
                        m.pushNil();
                        m.adapter.invokevirtual(p(DynamicScope.class), "getValueOneDepthZeroOrNil", sig(IRubyObject.class, IRubyObject.class));
                        return;
                    case 2:
                        m.pushNil();
                        m.adapter.invokevirtual(p(DynamicScope.class), "getValueTwoDepthZeroOrNil", sig(IRubyObject.class, IRubyObject.class));
                        return;
                    case 3:
                        m.pushNil();
                        m.adapter.invokevirtual(p(DynamicScope.class), "getValueThreeDepthZeroOrNil", sig(IRubyObject.class, IRubyObject.class));
                        return;
                    default:
                        m.adapter.pushInt(location);
                        m.pushNil();
                        m.adapter.invokevirtual(p(DynamicScope.class), "getValueDepthZeroOrNil", sig(IRubyObject.class, int.class, IRubyObject.class));
                        return;
                }
            default:
                m.adapter.pushInt(location);
                m.adapter.pushInt(depth);
                m.pushNil();
                m.adapter.invokevirtual(p(DynamicScope.class), "getValueOrNil", sig(IRubyObject.class, int.class, int.class, IRubyObject.class));
        }
    }

    @Override
    public void Match2Instr(Match2Instr match2instr) {
        super.Match2Instr(match2instr);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void Match3Instr(Match3Instr match3instr) {
        super.Match3Instr(match3instr);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void MatchInstr(MatchInstr matchinstr) {
        super.MatchInstr(matchinstr);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void MethodLookupInstr(MethodLookupInstr methodlookupinstr) {
        // SSS FIXME: Unused at this time
        throw new RuntimeException("Unsupported instruction: " + methodlookupinstr);
    }

    @Override
    public void ModuleVersionGuardInstr(ModuleVersionGuardInstr moduleversionguardinstr) {
        // SSS FIXME: Unused at this time
        throw new RuntimeException("Unsupported instruction: " + moduleversionguardinstr);
    }

    @Override
    public void NopInstr(NopInstr nopinstr) {
        // do nothing
    }

    @Override
    public void NoResultCallInstr(NoResultCallInstr noResultCallInstr) {
        IRBytecodeAdapter m = jvm.method();
        m.loadLocal(0);
        m.loadSelf(); // caller
        visit(noResultCallInstr.getReceiver());
        for (Operand operand : noResultCallInstr.getCallArgs()) {
            visit(operand);
        }

        switch (noResultCallInstr.getCallType()) {
            case FUNCTIONAL:
            case VARIABLE:
                m.invokeSelf(noResultCallInstr.getMethodAddr().getName(), noResultCallInstr.getCallArgs().length);
                break;
            case NORMAL:
                m.invokeOther(noResultCallInstr.getMethodAddr().getName(), noResultCallInstr.getCallArgs().length);
                break;
            case SUPER:
                m.invokeSuper(noResultCallInstr.getMethodAddr().getName(), noResultCallInstr.getCallArgs().length);
                break;
        }

        m.adapter.pop();
    }

    @Override
    public void NotInstr(NotInstr instr) {
        visit(instr.getArg());
        // SSS FIXME: Does this really require a helper rather than being inlined?
        jvm.method().invokeHelper("irNot", IRubyObject.class, ThreadContext.class, IRubyObject.class);
    }

    @Override
    public void OptArgMultipleAsgnInstr(OptArgMultipleAsgnInstr optargmultipleasgninstr) {
        super.OptArgMultipleAsgnInstr(optargmultipleasgninstr);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void PopBindingInstr(PopBindingInstr popbindinginstr) {
        jvm.method().loadContext();
        jvm.method().invokeVirtual(Type.getType(ThreadContext.class), Method.getMethod("void popScope()"));
    }

    @Override
    public void PopFrameInstr(PopFrameInstr popframeinstr) {
        jvm.method().loadContext();
        jvm.method().invokeVirtual(Type.getType(ThreadContext.class), Method.getMethod("void postMethodFrameOnly()"));
    }

    @Override
    public void ProcessModuleBodyInstr(ProcessModuleBodyInstr processmodulebodyinstr) {
        jvm.method().loadLocal(0);
        visit(processmodulebodyinstr.getModuleBody());
        jvm.method().invokeHelper("invokeModuleBody", IRubyObject.class, ThreadContext.class, CompiledIRMethod.class);
        jvmStoreLocal(processmodulebodyinstr.getResult());
    }

    @Override
    public void PushBindingInstr(PushBindingInstr pushbindinginstr) {
        jvm.method().loadContext();
        jvm.method().loadStaticScope();
        jvm.method().adapter.invokestatic(p(DynamicScope.class), "newDynamicScope", sig(DynamicScope.class, StaticScope.class));
        jvm.method().adapter.dup();
        jvmStoreLocal(DYNAMIC_SCOPE);
        jvm.method().invokeVirtual(Type.getType(ThreadContext.class), Method.getMethod("void pushScope(org.jruby.runtime.DynamicScope)"));
    }

    @Override
    public void PushFrameInstr(PushFrameInstr pushframeinstr) {
        jvm.method().loadContext();
        jvm.method().adapter.ldc(pushframeinstr.getFrameName().getName());
        jvm.method().loadSelf();
        jvm.method().loadLocal(4);
        jvm.method().loadStaticScope();
        jvm.method().invokeVirtual(Type.getType(ThreadContext.class), Method.getMethod("void preMethodFrameAndClass(String, org.jruby.runtime.builtin.IRubyObject, org.jruby.runtime.Block, org.jruby.parser.StaticScope)"));
    }

    @Override
    public void PutClassVariableInstr(PutClassVariableInstr putclassvariableinstr) {
        super.PutClassVariableInstr(putclassvariableinstr);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void PutConstInstr(PutConstInstr putconstinstr) {
        IRBytecodeAdapter m = jvm.method();
        visit(putconstinstr.getTarget());
        m.adapter.checkcast(p(RubyModule.class));
        m.adapter.ldc(putconstinstr.getRef());
        visit(putconstinstr.getValue());
        m.adapter.invokevirtual(p(RubyModule.class), "setConstant", sig(IRubyObject.class, String.class, IRubyObject.class));
        m.adapter.pop();
    }

    @Override
    public void PutFieldInstr(PutFieldInstr putfieldinstr) {
        visit(putfieldinstr.getTarget());
        visit(putfieldinstr.getValue());
        jvm.method().putField(putfieldinstr.getRef());
    }

    @Override
    public void PutGlobalVarInstr(PutGlobalVarInstr putglobalvarinstr) {
        GlobalVariable target = (GlobalVariable)putglobalvarinstr.getTarget();
        String name = target.getName();
        jvm.method().loadRuntime();
        jvm.method().invokeVirtual(Type.getType(Ruby.class), Method.getMethod("org.jruby.internal.runtime.GlobalVariables getGlobalVariables()"));
        jvm.method().adapter.ldc(name);
        visit(putglobalvarinstr.getValue());
        jvm.method().invokeVirtual(Type.getType(GlobalVariables.class), Method.getMethod("org.jruby.runtime.builtin.IRubyObject set(String, org.jruby.runtime.builtin.IRubyObject)"));
        // leaves copy of value on stack
        jvm.method().adapter.pop();
    }

    @Override
    public void RaiseArgumentErrorInstr(RaiseArgumentErrorInstr raiseargumenterrorinstr) {
        super.RaiseArgumentErrorInstr(raiseargumenterrorinstr);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void ReceiveClosureInstr(ReceiveClosureInstr receiveclosureinstr) {
        jvm.method().loadRuntime();
        jvmLoadLocal("$block");
        jvm.method().invokeIRHelper("newProc", sig(IRubyObject.class, Ruby.class, Block.class));
        jvmStoreLocal(receiveclosureinstr.getResult());
    }

    @Override
    public void ReceiveRubyExceptionInstr(ReceiveRubyExceptionInstr receiveexceptioninstr) {
        // exception should be on stack from try/catch, so unwrap and store it
        jvmStoreLocal(receiveexceptioninstr.getResult());
    }

    @Override
    public void ReceiveJRubyExceptionInstr(ReceiveJRubyExceptionInstr receiveexceptioninstr) {
        // exception should be on stack from try/catch, so just store it
        jvmStoreLocal(receiveexceptioninstr.getResult());
    }

    @Override
    public void ReceivePreReqdArgInstr(ReceivePreReqdArgInstr instr) {
        int index = getJVMLocalVarIndex(instr.getResult());
        jvm.method().loadLocal(3); // index of arg array
        jvm.method().adapter.ldc(instr.getArgIndex());
        jvm.method().adapter.aaload();
        jvm.method().storeLocal(index);
    }

    @Override
    public void ReceiveOptArgInstr(ReceiveOptArgInstr instr) {
        // FIXME: Only works when args is in an array rather than being flattened out
        // FIXME: Missing kwargs 2.0 support (kwArgHashCount value)
        jvm.method().adapter.pushInt(instr.getArgIndex() + instr.numUsedArgs); // MIN reqd args
        jvm.method().adapter.pushInt(instr.getArgIndex() + instr.argOffset); // args array offset
        jvm.method().adapter.aload(3); // index of arg array
        jvm.method().invokeHelper("irLoadOptArg", IRubyObject.class, int.class, int.class, IRubyObject[].class);
        jvmStoreLocal(instr.getResult());
    }

    @Override
    public void ReceivePostReqdArgInstr(ReceivePostReqdArgInstr instr) {
        // FIXME: Only works when args is in an array rather than being flattened out
        // FIXME: Missing kwargs 2.0 support (kwArgHashCount value)
        jvm.method().loadContext();
        jvm.method().adapter.pushInt(instr.getArgIndex());
        jvm.method().adapter.pushInt(instr.preReqdArgsCount);
        jvm.method().adapter.pushInt(instr.postReqdArgsCount);
        jvm.method().adapter.aload(3); // index of arg array
        jvm.method().invokeHelper("irLoadPostReqdArg", IRubyObject.class, int.class, int.class, int.class, IRubyObject[].class);
        jvmStoreLocal(instr.getResult());
    }

    @Override
    public void ReceiveRestArgInstr(ReceiveRestArgInstr instr) {
        // FIXME: Only works when args is in an array rather than being flattened out
        // FIXME: Missing kwargs 2.0 support (kwArgHashCount value)
        jvm.method().loadContext();
        jvm.method().adapter.pushInt(instr.numUsedArgs); // MIN reqd args
        jvm.method().adapter.pushInt(instr.getArgIndex()); // args array offset
        jvm.method().adapter.aload(3); // index of arg array
        jvm.method().invokeHelper("irLoadRestArg", IRubyObject.class, ThreadContext.class, int.class, int.class, IRubyObject[].class);
        jvmStoreLocal(instr.getResult());
    }

    @Override
    public void ReceiveSelfInstr(ReceiveSelfInstr receiveselfinstr) {
        throw new RuntimeException("Self instr should have been stripped: " + receiveselfinstr);
    }

    @Override
    public void RecordEndBlockInstr(RecordEndBlockInstr recordendblockinstr) {
        super.RecordEndBlockInstr(recordendblockinstr);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void ReqdArgMultipleAsgnInstr(ReqdArgMultipleAsgnInstr reqdargmultipleasgninstr) {
        jvm.method().loadContext();
        visit(reqdargmultipleasgninstr.getArrayArg());
        jvm.method().adapter.checkcast("org/jruby/RubyArray");
        jvm.method().adapter.pushInt(reqdargmultipleasgninstr.getPreArgsCount());
        jvm.method().adapter.pushInt(reqdargmultipleasgninstr.getIndex());
        jvm.method().adapter.pushInt(reqdargmultipleasgninstr.getPostArgsCount());
        jvm.method().invokeHelper("irReqdArgMultipleAsgn", IRubyObject.class, ThreadContext.class, RubyArray.class, int.class, int.class, int.class);
        jvmStoreLocal(reqdargmultipleasgninstr.getResult());
    }

    @Override
    public void RescueEQQInstr(RescueEQQInstr rescueeqqinstr) {
        jvm.method().loadContext();
        visit(rescueeqqinstr.getArg1());
        visit(rescueeqqinstr.getArg2());
        jvm.method().invokeIRHelper("isExceptionHandled", sig(IRubyObject.class, ThreadContext.class, IRubyObject.class, Object.class));
        jvmStoreLocal(rescueeqqinstr.getResult());
    }

    @Override
    public void RestArgMultipleAsgnInstr(RestArgMultipleAsgnInstr restargmultipleasgninstr) {
        super.RestArgMultipleAsgnInstr(restargmultipleasgninstr);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void RuntimeHelperCall(RuntimeHelperCall runtimehelpercall) {
        // no-op for the moment
    }

    @Override
    public void NonlocalReturnInstr(NonlocalReturnInstr returninstr) {
        if (this.currentScope instanceof IRClosure) {
            /* generate run-time call to check non-local-return, errors, etc */
            SkinnyMethodAdapter a = jvm.method().adapter;
            a.aload(0); // 1. ThreadContext
            a.aload(1); // 2. current scope
            // 3. ref. to returnInstr.methodToReturnFrom
            visit(returninstr.getReturnValue()); // 4. return value
            // boolean about whether we are in a closure or not
            // call to handle non-local return
        } else if (returninstr.methodToReturnFrom != null) {
            // methodtoReturnFrom will not be null for explicit returns from class/module/sclass bodies
            /* throw IR-return-jump */
        } else {
            visit(returninstr.getReturnValue());
            jvm.method().returnValue();
        }
    }

    @Override
    public void ReturnInstr(ReturnInstr returninstr) {
        visit(returninstr.getReturnValue());
        jvm.method().returnValue();
    }

    @Override
    public void SearchConstInstr(SearchConstInstr searchconstinstr) {
        // TODO: private consts
        jvm.method().loadLocal(0);
        visit(searchconstinstr.getStartingScope());
        jvm.method().searchConst(searchconstinstr.getConstName());
        jvmStoreLocal(searchconstinstr.getResult());
    }

    @Override
    public void StoreLocalVarInstr(StoreLocalVarInstr storelocalvarinstr) {
        IRBytecodeAdapter m = jvm.method();
        jvmLoadLocal(DYNAMIC_SCOPE);
        int depth = storelocalvarinstr.getLocalVar().getScopeDepth();
        // TODO should not have to subtract 1
        int location = storelocalvarinstr.getLocalVar().getLocation() - 1;
        Operand storeValue = storelocalvarinstr.getValue();
        switch (depth) {
            case 0:
                switch (location) {
                    case 0:
                        storeValue.visit(this);
                        m.adapter.invokevirtual(p(DynamicScope.class), "setValueZeroDepthZero", sig(IRubyObject.class, IRubyObject.class));
                        m.adapter.pop();
                        return;
                    case 1:
                        storeValue.visit(this);
                        m.adapter.invokevirtual(p(DynamicScope.class), "setValueOneDepthZero", sig(IRubyObject.class, IRubyObject.class));
                        m.adapter.pop();
                        return;
                    case 2:
                        storeValue.visit(this);
                        m.adapter.invokevirtual(p(DynamicScope.class), "setValueTwoDepthZero", sig(IRubyObject.class, IRubyObject.class));
                        m.adapter.pop();
                        return;
                    case 3:
                        storeValue.visit(this);
                        m.adapter.invokevirtual(p(DynamicScope.class), "setValueThreeDepthZero", sig(IRubyObject.class, IRubyObject.class));
                        m.adapter.pop();
                        return;
                    default:
                        storeValue.visit(this);
                        m.adapter.pushInt(location);
                        m.adapter.invokevirtual(p(DynamicScope.class), "setValueDepthZero", sig(IRubyObject.class, IRubyObject.class, int.class));
                        m.adapter.pop();
                        return;
                }
            default:
                m.adapter.pushInt(depth);
                storeValue.visit(this);
                m.adapter.pushInt(location);
                m.adapter.invokevirtual(p(DynamicScope.class), "setValue", sig(IRubyObject.class, int.class, IRubyObject.class, int.class));
                m.adapter.pop();
        }
    }

    @Override
    public void ThreadPollInstr(ThreadPollInstr threadpollinstr) {
        jvm.method().loadContext();
        jvm.method().adapter.invokedynamic(
                "checkpoint",
                sig(void.class, ThreadContext.class),
                InvokeDynamicSupport.checkpointHandle());
    }

    @Override
    public void ThrowExceptionInstr(ThrowExceptionInstr throwexceptioninstr) {
        visit(throwexceptioninstr.getExceptionArg());
        jvm.method().adapter.athrow();
    }

    @Override
    public void ToAryInstr(ToAryInstr toaryinstr) {
        jvm.method().loadContext();
        visit(toaryinstr.getArrayArg());
        jvm.method().invokeHelper("irToAry", IRubyObject.class, ThreadContext.class, IRubyObject.class);
        jvmStoreLocal(toaryinstr.getResult());
    }

    @Override
    public void UndefMethodInstr(UndefMethodInstr undefmethodinstr) {
        super.UndefMethodInstr(undefmethodinstr);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void UnresolvedSuperInstr(UnresolvedSuperInstr unresolvedsuperinstr) {
        super.UnresolvedSuperInstr(unresolvedsuperinstr);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void YieldInstr(YieldInstr yieldinstr) {
        jvm.method().loadLocal(0);
        visit(yieldinstr.getBlockArg());

        if (yieldinstr.getYieldArg() == UndefinedValue.UNDEFINED) {
            jvm.method().invokeIRHelper("yieldSpecific", sig(IRubyObject.class, ThreadContext.class, Object.class));
        } else {
            visit(yieldinstr.getYieldArg());
            jvm.method().adapter.ldc(yieldinstr.isUnwrapArray());
            jvm.method().invokeIRHelper("yield", sig(IRubyObject.class, ThreadContext.class, Object.class, Object.class, boolean.class));
        }

        jvmStoreLocal(yieldinstr.getResult());
    }

    @Override
    public void ZSuperInstr(ZSuperInstr zsuperinstr) {
        super.ZSuperInstr(zsuperinstr);    //To change body of overridden methods use File | Settings | File Templates.
    }

    // "defined" instructions


    @Override
    public void BackrefIsMatchDataInstr(BackrefIsMatchDataInstr backrefismatchdatainstr) {
        super.BackrefIsMatchDataInstr(backrefismatchdatainstr);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void ClassVarIsDefinedInstr(ClassVarIsDefinedInstr classvarisdefinedinstr) {
        super.ClassVarIsDefinedInstr(classvarisdefinedinstr);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void GetBackrefInstr(GetBackrefInstr getbackrefinstr) {
        super.GetBackrefInstr(getbackrefinstr);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void GetDefinedConstantOrMethodInstr(GetDefinedConstantOrMethodInstr getdefinedconstantormethodinstr) {
        super.GetDefinedConstantOrMethodInstr(getdefinedconstantormethodinstr);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void GetErrorInfoInstr(GetErrorInfoInstr geterrorinfoinstr) {
        super.GetErrorInfoInstr(geterrorinfoinstr);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void GlobalIsDefinedInstr(GlobalIsDefinedInstr globalisdefinedinstr) {
        super.GlobalIsDefinedInstr(globalisdefinedinstr);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void HasInstanceVarInstr(HasInstanceVarInstr hasinstancevarinstr) {
        IRBytecodeAdapter m = jvm.method();
        // TODO: This is suboptimal, not caching ivar offset at all
        m.loadRuntime();
        visit(hasinstancevarinstr.getObject());
        m.adapter.invokeinterface(p(IRubyObject.class), "getInstanceVariables", sig(InstanceVariables.class));
        m.adapter.ldc(hasinstancevarinstr.getName().string);
        m.adapter.invokeinterface(p(InstanceVariables.class), "hasInstanceVariable", sig(boolean.class, String.class));
        m.adapter.invokevirtual(p(Ruby.class), "newBoolean", sig(RubyBoolean.class, boolean.class));
        jvmStoreLocal(hasinstancevarinstr.getResult());
    }

    @Override
    public void IsMethodBoundInstr(IsMethodBoundInstr ismethodboundinstr) {
        super.IsMethodBoundInstr(ismethodboundinstr);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void MethodDefinedInstr(MethodDefinedInstr methoddefinedinstr) {
        super.MethodDefinedInstr(methoddefinedinstr);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void MethodIsPublicInstr(MethodIsPublicInstr methodispublicinstr) {
        super.MethodIsPublicInstr(methodispublicinstr);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void RestoreErrorInfoInstr(RestoreErrorInfoInstr restoreerrorinfoinstr) {
        super.RestoreErrorInfoInstr(restoreerrorinfoinstr);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void SuperMethodBoundInstr(SuperMethodBoundInstr supermethodboundinstr) {
        super.SuperMethodBoundInstr(supermethodboundinstr);    //To change body of overridden methods use File | Settings | File Templates.
    }

    // ruby 1.9 specific
    @Override
    public void BuildLambdaInstr(BuildLambdaInstr buildlambdainstr) {
        super.BuildLambdaInstr(buildlambdainstr);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void GetEncodingInstr(GetEncodingInstr getencodinginstr) {
        super.GetEncodingInstr(getencodinginstr);    //To change body of overridden methods use File | Settings | File Templates.
    }

    // operands
    @Override
    public void Array(Array array) {
        jvm.method().loadLocal(0);

        for (Operand operand : array.getElts()) {
            visit(operand);
        }

        jvm.method().array(array.getElts().length);
    }

    @Override
    public void AsString(AsString asstring) {
        visit(asstring.getSource());
        jvm.method().adapter.invokevirtual(p(IRubyObject.class), "asString", sig(RubyString.class));
    }

    @Override
    public void Backref(Backref backref) {
        super.Backref(backref);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void BacktickString(BacktickString backtickstring) {
        super.BacktickString(backtickstring);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void Bignum(Bignum bignum) {
        super.Bignum(bignum);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void Boolean(org.jruby.ir.operands.Boolean booleanliteral) {
        jvm.method().pushBoolean(booleanliteral.isTrue());
    }

    @Override
    public void UnboxedBoolean(org.jruby.ir.operands.UnboxedBoolean bool) {
        jvm.method().adapter.ldc(bool.isTrue());
    }

    @Override
    public void ClosureLocalVariable(ClosureLocalVariable closurelocalvariable) {
        super.ClosureLocalVariable(closurelocalvariable);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void CompoundArray(CompoundArray compoundarray) {
        visit(compoundarray.getAppendingArg());
        if (compoundarray.isArgsPush()) jvm.method().adapter.checkcast("org/jruby/RubyArray");
        visit(compoundarray.getAppendedArg());
        if (compoundarray.isArgsPush()) {
            jvm.method().invokeHelper("argsPush", RubyArray.class, RubyArray.class, IRubyObject.class);
        } else {
            jvm.method().invokeHelper("argsCat", RubyArray.class, IRubyObject.class, IRubyObject.class);
        }
    }

    @Override
    public void CompoundString(CompoundString compoundstring) {
        super.CompoundString(compoundstring);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void CurrentScope(CurrentScope currentscope) {
        jvm.method().adapter.aload(1);
    }

    @Override
    public void DynamicSymbol(DynamicSymbol dynamicsymbol) {
        super.DynamicSymbol(dynamicsymbol);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void Fixnum(Fixnum fixnum) {
        jvm.method().pushFixnum(fixnum.getValue());
    }

    @Override
    public void UnboxedFixnum(UnboxedFixnum fixnum) {
        jvm.method().adapter.ldc(fixnum.getValue());
    }

    @Override
    public void Float(org.jruby.ir.operands.Float flote) {
        jvm.method().pushFloat(flote.getValue());
    }

    @Override
    public void UnboxedFloat(org.jruby.ir.operands.UnboxedFloat flote) {
        jvm.method().adapter.ldc(flote.getValue());
    }

    @Override
    public void GlobalVariable(GlobalVariable globalvariable) {
        super.GlobalVariable(globalvariable);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void Hash(Hash hash) {
        super.Hash(hash);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void IRException(IRException irexception) {
        super.IRException(irexception);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void MethAddr(MethAddr methaddr) {
        jvm.method().adapter.ldc(methaddr.getName());
    }

    @Override
    public void MethodHandle(MethodHandle methodhandle) {
        // SSS FIXME: Unused at this time
        throw new RuntimeException("Unsupported operand: " + methodhandle);
    }

    @Override
    public void Nil(Nil nil) {
        jvm.method().pushNil();
    }

    @Override
    public void NthRef(NthRef nthref) {
        super.NthRef(nthref);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void ObjectClass(ObjectClass objectclass) {
        jvm.method().pushObjectClass();
    }

    @Override
    public void Range(Range range) {
        jvm.method().loadRuntime();
        jvm.method().loadContext();
        visit(range.getBegin());
        visit(range.getEnd());
        jvm.method().adapter.ldc(range.isExclusive());
        jvm.method().adapter.invokestatic(p(RubyRange.class), "newRange", sig(RubyRange.class, Ruby.class, ThreadContext.class, IRubyObject.class, IRubyObject.class, boolean.class));
    }

    @Override
    public void Regexp(Regexp regexp) {
        super.Regexp(regexp);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void ScopeModule(ScopeModule scopemodule) {
        jvm.method().adapter.aload(1);
        jvm.method().adapter.invokevirtual(p(StaticScope.class), "getModule", sig(RubyModule.class));
    }

    @Override
    public void Self(Self self) {
        // %self is in JVM-local-2 always
        jvm.method().loadLocal(2);
    }

    @Override
    public void Splat(Splat splat) {
        jvm.method().loadContext();
        visit(splat.getArray());
        jvm.method().invokeHelper("irSplat", RubyArray.class, ThreadContext.class, IRubyObject.class);
    }

    @Override
    public void StandardError(StandardError standarderror) {
        jvm.method().loadRuntime();
        jvm.method().adapter.invokevirtual(p(Ruby.class), "getStandardError", sig(RubyClass.class));
    }

    @Override
    public void StringLiteral(StringLiteral stringliteral) {
        jvm.method().pushString(stringliteral.getByteList());
    }

    @Override
    public void SValue(SValue svalue) {
        super.SValue(svalue);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void Symbol(Symbol symbol) {
        jvm.method().pushSymbol(symbol.getName());
    }

    @Override
    public void TemporaryVariable(TemporaryVariable temporaryvariable) {
        jvmLoadLocal(temporaryvariable);
    }

    @Override
    public void TemporaryLocalVariable(TemporaryLocalVariable temporarylocalvariable) {
        jvmLoadLocal(temporarylocalvariable);
    }

    @Override
    public void TemporaryFloatVariable(TemporaryFloatVariable temporaryfloatvariable) {
        jvmLoadLocal(temporaryfloatvariable);
    }

    @Override
    public void TemporaryFixnumVariable(TemporaryFixnumVariable temporaryfixnumvariable) {
        jvmLoadLocal(temporaryfixnumvariable);
    }

    @Override
    public void TemporaryBooleanVariable(TemporaryBooleanVariable temporarybooleanvariable) {
        jvmLoadLocal(temporarybooleanvariable);
    }

    @Override
    public void UndefinedValue(UndefinedValue undefinedvalue) {
        jvm.method().pushUndefined();
    }

    @Override
    public void UnexecutableNil(UnexecutableNil unexecutablenil) {
        throw new RuntimeException(this.getClass().getSimpleName() + " should never be directly executed!");
    }

    @Override
    public void WrappedIRClosure(WrappedIRClosure wrappedirclosure) {
        super.WrappedIRClosure(wrappedirclosure);    //To change body of overridden methods use File | Settings | File Templates.
    }

    private final JVM jvm;
    private IRScriptBody script;
    private IRScope currentScope;
}
