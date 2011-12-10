package org.jruby.compiler.ir;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import org.jruby.Ruby;
import org.jruby.RubyModule;
import org.jruby.compiler.ir.compiler_pass.CompilerPass;
import org.jruby.compiler.ir.instructions.ReceiveSelfInstruction;
import org.jruby.compiler.ir.operands.LocalVariable;
import org.jruby.parser.StaticScope;

public class IRModule extends IRScope {
    private static Map<String, IRClass> coreClasses;

    private CodeVersion version;    // Current code version for this module

    // Modules, classes, and methods that belong to this scope 
    //
    // LEXICAL scoping, but when a class, method, module definition is
    // encountered in a closure or a method in Ruby code, that definition
    // is pushed up to the nearest containing module!
    //
    // In most cases, this lexical scoping also matches actual class/module hierarchies
    // SSS FIXME: An example where they might be different?
    private List<IRModule> modules = new ArrayList<IRModule>();
    private List<IRClass> classes = new ArrayList<IRClass>();
    private List<IRMethod> methods = new ArrayList<IRMethod>();
    
    static {
        bootStrap();
    }
    

    public IRModule(IRScope lexicalParent, String name, StaticScope scope) {
        super(lexicalParent, name, scope);
        
        updateVersion();
    }    

    static private IRClass addCoreClass(String name, IRScope parent, String[] coreMethods, StaticScope staticScope) {
        IRClass c = new IRClass(parent, null, name, staticScope);
        c.addInstr(new ReceiveSelfInstruction(c.getSelf()));
        coreClasses.put(c.getName(), c);
        if (coreMethods != null) {
            for (String m : coreMethods) {
                IRMethod meth = new IRMethod(c, m, true, null);
                meth.setCodeModificationFlag(false);
                c.addMethod(meth);
            }
        }
        return c;
    }

    // SSS FIXME: These should get normally compiled or initialized some other way ... 
    // SSS FIXME: Parent/super-type info is incorrect!
    // These are just placeholders for now .. this needs to be updated with *real* class objects later!
    static public void bootStrap() {
        coreClasses = new HashMap<String, IRClass>();
        IRScript boostrapScript = new IRScript("[bootstrap]", "[bootstrap]", null);
        boostrapScript.addInstr(new ReceiveSelfInstruction(boostrapScript.getSelf()));
        addCoreClass("Object", boostrapScript, null, null);
        addCoreClass("Module", boostrapScript, null, null);
        addCoreClass("Class", boostrapScript, null, null);
        addCoreClass("Fixnum", boostrapScript, new String[]{"+", "-", "/", "*"}, null);
        addCoreClass("Float", boostrapScript, new String[]{"+", "-", "/", "*"}, null);
        addCoreClass("Array", boostrapScript, new String[]{"[]", "each", "inject"}, null);
        addCoreClass("Range", boostrapScript, new String[]{"each"}, null);
        addCoreClass("Hash", boostrapScript, new String[]{"each"}, null);
        addCoreClass("String", boostrapScript, null, null);
        addCoreClass("Proc", boostrapScript, null, null);
    }

    public static IRClass getCoreClass(String n) {
        return coreClasses.get(n);
    }

    public List<IRModule> getModules() {
        return modules;
    }

    public List<IRClass> getClasses() {
        return classes;
    }

    public List<IRMethod> getMethods() {
        return methods;
    }

    public void addModule(IRModule m) {
        modules.add(m);
    }

    public void addClass(IRClass c) {
        classes.add(c);
    }

    public void addMethod(IRMethod method) {
        assert !method.isScriptBody();

        methods.add(method);
    }

    @Override
    public void runCompilerPassOnNestedScopes(CompilerPass p) {
        for (IRScope m : modules) {
            m.runCompilerPass(p);
        }

        for (IRScope c : classes) {
            c.runCompilerPass(p);
        }

        for (IRScope meth : methods) {
            meth.runCompilerPass(p);
        }
    }

    @Override
    public IRModule getNearestModule() {
        return this;
    }

    public void updateVersion() {
        version = CodeVersion.getClassVersionToken();
    }

    public String getScopeName() {
        return "Module";
    }

    public CodeVersion getVersion() {
        return version;
    }

    public IRMethod getInstanceMethod(String name) {
        for (IRMethod m : methods) {
            if (m.isInstanceMethod && m.getName().equals(name)) return m;
        }

        return null;
    }

    public boolean isACoreClass() {
        return this == IRClass.getCoreClass(getName());
    }

    public boolean isCoreClassType(String className) {
        return this == IRClass.getCoreClass(className);
    }

    public RubyModule getCoreClassModule(Ruby runtime) {
        // SSS FIXME: Here, I dont really care if this is a core class module or not .. so, why the charade?
        String n = getName();
        if (n.equals("Object")) return runtime.getObject();
        else return runtime.getClass(n);
    }

    public LocalVariable getLocalVariable(String name, int scopeDepth) {
        LocalVariable lvar = findExistingLocalVariable(name);
        if (lvar == null) {
            lvar = new LocalVariable(name, scopeDepth, localVars.nextSlot);
            localVars.putVariable(name, lvar);
        }

        return lvar;
    }

    @Override
    public LocalVariable getImplicitBlockArg() {
        assert false: "A Script body never accepts block args";
        
        return null;
    }

    @Override
    public LocalVariable findExistingLocalVariable(String name) {
        return localVars.getVariable(name);
    }
    
    @Override
    public boolean isScriptBody() {
        return true;
    }
}
