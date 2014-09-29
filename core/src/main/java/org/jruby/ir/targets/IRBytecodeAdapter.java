/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jruby.ir.targets;

import com.headius.invokebinder.Signature;
import org.jcodings.Encoding;
import org.jruby.*;
import org.jruby.compiler.impl.SkinnyMethodAdapter;
import org.jruby.ir.operands.UndefinedValue;
import org.jruby.ir.runtime.IRRuntimeHelpers;
import org.jruby.parser.StaticScope;
import org.jruby.runtime.Block;
import org.jruby.runtime.Helpers;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;
import org.jruby.util.ByteList;
import org.jruby.util.JavaNameMangler;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import static org.jruby.util.CodegenUtils.*;

/**
 *
 * @author headius
 */
public abstract class IRBytecodeAdapter {
    public IRBytecodeAdapter(SkinnyMethodAdapter adapter, Signature signature, ClassData classData) {
        this.adapter = adapter;
        this.signature = signature;
        this.classData = classData;
    }

    public ClassData getClassData() {
        return classData;
    }

    public void startMethod() {
        adapter.start();
    }

    public void endMethod() {
        adapter.end(new Runnable() {
            public void run() {
                for (Map.Entry<Integer, Type> entry : variableTypes.entrySet()) {
                    int i = entry.getKey();
                    String name = variableNames.get(i);
                    adapter.local(i, name, entry.getValue());
                }
            }
        });
    }

    public void loadLocal(int i) {
        adapter.aload(i);
    }

    public void loadContext() {
        adapter.aload(0);
    }

    public void loadStaticScope() {
        adapter.aload(1);
    }

    public void loadSelf() {
        adapter.aload(2);
    }

    public void loadArgs() {
        adapter.aload(3);
    }

    public void loadBlock() {
        adapter.aload(4);
    }

    public void loadFrameClass() {
        adapter.aload(5);
    }

    public void loadSuperName() {
        adapter.aload(5);
    }

    public void loadBlockType() {
        if (signature.argOffset("type") == -1) {
            adapter.aconst_null();
        } else {
            adapter.aload(6);
        }
    }

    public void storeLocal(int i) {
        adapter.astore(i);
    }

    public void invokeVirtual(Type type, Method method) {
        adapter.invokevirtual(type.getInternalName(), method.getName(), method.getDescriptor());
    }

    public void invokeStatic(Type type, Method method) {
        adapter.invokestatic(type.getInternalName(), method.getName(), method.getDescriptor());
    }

    public void invokeHelper(String name, String sig) {
        adapter.invokestatic(p(Helpers.class), name, sig);
    }

    public void invokeHelper(String name, Class... x) {
        adapter.invokestatic(p(Helpers.class), name, sig(x));
    }

    public void invokeIRHelper(String name, String sig) {
        adapter.invokestatic(p(IRRuntimeHelpers.class), name, sig);
    }

    public void goTo(org.objectweb.asm.Label label) {
        adapter.go_to(label);
    }

    public void isTrue() {
        adapter.invokeinterface(p(IRubyObject.class), "isTrue", sig(boolean.class));
    }

    public void isNil() {
        adapter.invokeinterface(p(IRubyObject.class), "isNil", sig(boolean.class));
    }

    public void bfalse(org.objectweb.asm.Label label) {
        adapter.iffalse(label);
    }

    public void btrue(org.objectweb.asm.Label label) {
        adapter.iftrue(label);
    }

    public void poll() {
        loadContext();
        adapter.invokevirtual(p(ThreadContext.class), "pollThreadEvents", sig(void.class));
    }

    public void pushObjectClass() {
        loadRuntime();
        adapter.invokevirtual(p(Ruby.class), "getObject", sig(RubyClass.class));
    }

    public void pushUndefined() {
        adapter.getstatic(p(UndefinedValue.class), "UNDEFINED", ci(UndefinedValue.class));
    }

    public void pushHandle(Handle handle) {
        adapter.getMethodVisitor().visitLdcInsn(handle);
    }

    public void mark(org.objectweb.asm.Label label) {
        adapter.label(label);
    }

    public void returnValue() {
        adapter.areturn();
    }

    public int newLocal(String name, Type type) {
        int index = variableCount++;
        if (type == Type.DOUBLE_TYPE || type == Type.LONG_TYPE) {
            variableCount++;
        }
        variableTypes.put(index, type);
        variableNames.put(index, name);
        return index;
    }

    public org.objectweb.asm.Label newLabel() {
        return new org.objectweb.asm.Label();
    }

    /**
     * Stack required: none
     *
     * @param l long value to push as a Fixnum
     */
    public abstract void pushFixnum(Long l);

    /**
     * Stack required: none
     *
     * @param d double value to push as a Float
     */
    public abstract void pushFloat(Double d);

    /**
     * Stack required: none
     *
     * @param bl ByteList for the String to push
     */
    public abstract void pushString(ByteList bl);

    /**
     * Stack required: none
     *
     * @param bl ByteList to push
     */
    public abstract void pushByteList(ByteList bl);

    /**
     * Stack required: ThreadContext, RubyString.
     *
     * @param options options for the regexp
     */
    public abstract void pushRegexp(int options);

    /**
     * Push a symbol on the stack.
     *
     * Stack required: none
     *
     * @param sym the symbol's string identifier
     */
    public abstract void pushSymbol(String sym);

    /**
     * Push the JRuby runtime on the stack.
     *
     * Stack required: none
     */
    public abstract void loadRuntime();

    /**
     * Push an encoding on the stack.
     *
     * Stack required: none
     *
     * @param encoding the encoding to push
     */
    public abstract void pushEncoding(Encoding encoding);

    /**
     * Invoke a method on an object other than self.
     *
     * Stack required: context, self, all arguments, optional block
     *
     * @param name name of the method to invoke
     * @param arity arity of the call
     * @param hasClosure whether a closure will be on the stack for passing
     */
    public abstract void invokeOther(String name, int arity, boolean hasClosure);


    /**
     * Invoke a method on self.
     *
     * Stack required: context, caller, self, all arguments, optional block
     *
     * @param name name of the method to invoke
     * @param arity arity of the call
     * @param hasClosure whether a closure will be on the stack for passing
     */
    public abstract void invokeSelf(String name, int arity, boolean hasClosure);

    public abstract void invokeInstanceSuper(String name, int arity, boolean hasClosure, boolean[] splatmap);

    public abstract void invokeClassSuper(String name, int arity, boolean hasClosure, boolean[] splatmap);

    public abstract void invokeUnresolvedSuper(String name, int arity, boolean hasClosure, boolean[] splatmap);

    public abstract void invokeZSuper(String name, int arity, boolean hasClosure, boolean[] splatmap);

    /**
     * Lookup a constant from current context.
     *
     * Stack required: context, static scope
     *
     * @param name name of the constant
     * @param noPrivateConsts whether to ignore private constants
     */
    public abstract void searchConst(String name, boolean noPrivateConsts);

    /**
     * Lookup a constant from a given class or module.
     *
     * Stack required: context, module
     *
     * @param name name of the constant
     * @param noPrivateConsts whether to ignore private constants
     */
    public abstract void inheritanceSearchConst(String name, boolean noPrivateConsts);

    /**
     * Lookup a constant from a lexical scope.
     *
     * Stack required: context, static scope
     *
     * @param name name of the constant
     */
    public abstract void lexicalSearchConst(String name);

    /**
     * Load nil onto the stack.
     *
     * Stack required: none
     */
    public abstract void pushNil();

    /**
     * Load a boolean onto the stack.
     *
     * Stack required: none
     *
     * @param b the boolean to push
     */
    public abstract void pushBoolean(boolean b);

    /**
     * Load a Bignum onto the stack.
     *
     * Stack required: none
     *
     * @param bigint the value of the Bignum to push
     */
    public abstract void pushBignum(BigInteger bigint);

    /**
     * Store instance variable into self.
     *
     * Stack required: self, value
     *
     * @param name name of variable to store
     */
    public abstract void putField(String name);

    /**
     * Load instance variable from self.
     *
     * Stack required: self
     *
     * @param name name of variable to load
     */
    public abstract void getField(String name);

    /**
     * Construct an Array from elements on stack.
     *
     * Stack required: all elements of array
     *
     * @param length number of elements
     */
    public abstract void array(int length);

    /**
     * Construct a Hash from elements on stack.
     *
     * Stack required: all elements of hash
     *
     * @param length number of element pairs
     */
    public abstract void hash(int length);

    /**
     * Perform a thread event checkpoint.
     *
     * Stack required: none
     */
    public abstract void checkpoint();

    public SkinnyMethodAdapter adapter;
    private int variableCount = 0;
    private Map<Integer, Type> variableTypes = new HashMap<Integer, Type>();
    private Map<Integer, String> variableNames = new HashMap<Integer, String>();
    private final Signature signature;
    private final ClassData classData;
}
