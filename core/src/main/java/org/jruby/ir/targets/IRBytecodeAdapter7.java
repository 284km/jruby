/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jruby.ir.targets;

import com.headius.invokebinder.Signature;
import org.jcodings.Encoding;
import org.jruby.Ruby;
import org.jruby.RubyBignum;
import org.jruby.RubyClass;
import org.jruby.RubyEncoding;
import org.jruby.RubyRegexp;
import org.jruby.RubyString;
import org.jruby.compiler.impl.SkinnyMethodAdapter;
import org.jruby.ir.operands.UndefinedValue;
import org.jruby.ir.runtime.IRRuntimeHelpers;
import org.jruby.parser.StaticScope;
import org.jruby.runtime.Block;
import org.jruby.runtime.Helpers;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;
import org.jruby.runtime.invokedynamic.InvokeDynamicSupport;
import org.jruby.util.ByteList;
import org.jruby.util.JavaNameMangler;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import static org.jruby.util.CodegenUtils.ci;
import static org.jruby.util.CodegenUtils.p;
import static org.jruby.util.CodegenUtils.params;
import static org.jruby.util.CodegenUtils.sig;

/**
 *
 * @author headius
 */
public class IRBytecodeAdapter7 extends IRBytecodeAdapter {
    public IRBytecodeAdapter7(SkinnyMethodAdapter adapter, Signature signature, ClassData classData) {
        super(adapter, signature, classData);
    }

    public void pushFixnum(Long l) {
        loadContext();
        adapter.invokedynamic("fixnum", sig(JVM.OBJECT, ThreadContext.class), Bootstrap.fixnum(), l);
    }

    public void pushFloat(Double d) {
        loadContext();
        adapter.invokedynamic("flote", sig(JVM.OBJECT, ThreadContext.class), Bootstrap.flote(), d);
    }

    public void pushString(ByteList bl) {
        loadContext();
        adapter.invokedynamic("string", sig(RubyString.class, ThreadContext.class), Bootstrap.string(), new String(bl.bytes(), RubyEncoding.ISO), bl.getEncoding().toString());
    }

    public void pushByteList(ByteList bl) {
        adapter.invokedynamic("bytelist", sig(ByteList.class), Bootstrap.bytelist(), new String(bl.bytes(), RubyEncoding.ISO), bl.getEncoding().toString());
    }

    public void pushRegexp(int options) {
        adapter.invokedynamic("regexp", sig(RubyRegexp.class, ThreadContext.class, RubyString.class), Bootstrap.regexp(), options);
    }

    /**
     * Push a symbol on the stack
     * @param sym the symbol's string identifier
     */
    public void pushSymbol(String sym) {
        loadContext();
        adapter.invokedynamic("symbol", sig(JVM.OBJECT, ThreadContext.class), Bootstrap.symbol(), sym);
    }

    public void loadRuntime() {
        loadContext();
        adapter.invokedynamic("runtime", sig(Ruby.class, ThreadContext.class), Bootstrap.contextValue());
    }

    public void pushEncoding(Encoding encoding) {
        loadContext();
        adapter.invokedynamic("encoding", sig(RubyEncoding.class, ThreadContext.class), Bootstrap.contextValueString(), new String(encoding.getName()));
    }

    public void invokeOther(String name, int arity, boolean hasClosure) {
        if (hasClosure) {
            if (arity == -1) {
                adapter.invokedynamic("invoke:" + JavaNameMangler.mangleMethodName(name), sig(JVM.OBJECT, params(ThreadContext.class, JVM.OBJECT, JVM.OBJECT, JVM.OBJECT_ARRAY, Block.class)), Bootstrap.invoke());
            } else {
                adapter.invokedynamic("invoke:" + JavaNameMangler.mangleMethodName(name), sig(JVM.OBJECT, params(ThreadContext.class, JVM.OBJECT, arity + 2, Block.class)), Bootstrap.invoke());
            }
        } else {
            if (arity == -1) {
                adapter.invokedynamic("invoke:" + JavaNameMangler.mangleMethodName(name), sig(JVM.OBJECT, params(ThreadContext.class, JVM.OBJECT, JVM.OBJECT, JVM.OBJECT_ARRAY)), Bootstrap.invoke());
            } else {
                adapter.invokedynamic("invoke:" + JavaNameMangler.mangleMethodName(name), sig(JVM.OBJECT, params(ThreadContext.class, JVM.OBJECT, JVM.OBJECT, JVM.OBJECT, arity)), Bootstrap.invoke());
            }
        }
    }

    public void invokeSelf(String name, int arity, boolean hasClosure) {
        if (hasClosure) {
            if (arity == -1) {
                adapter.invokedynamic("invokeSelf:" + JavaNameMangler.mangleMethodName(name), sig(JVM.OBJECT, params(ThreadContext.class, JVM.OBJECT, JVM.OBJECT, JVM.OBJECT_ARRAY, Block.class)), Bootstrap.invokeSelf());
            } else {
                adapter.invokedynamic("invokeSelf:" + JavaNameMangler.mangleMethodName(name), sig(JVM.OBJECT, params(ThreadContext.class, JVM.OBJECT, arity + 2, Block.class)), Bootstrap.invokeSelf());
            }
        } else {
            if (arity == -1) {
                adapter.invokedynamic("invokeSelf:" + JavaNameMangler.mangleMethodName(name), sig(JVM.OBJECT, params(ThreadContext.class, JVM.OBJECT, JVM.OBJECT, JVM.OBJECT_ARRAY)), Bootstrap.invokeSelf());
            } else {
                adapter.invokedynamic("invokeSelf:" + JavaNameMangler.mangleMethodName(name), sig(JVM.OBJECT, params(ThreadContext.class, JVM.OBJECT, JVM.OBJECT, JVM.OBJECT, arity)), Bootstrap.invokeSelf());
            }
        }
    }

    public void invokeInstanceSuper(String name, int arity, boolean hasClosure, boolean[] splatmap) {
        String splatmapString = IRRuntimeHelpers.encodeSplatmap(splatmap);
        if (hasClosure) {
            adapter.invokedynamic("invokeInstanceSuper:" + JavaNameMangler.mangleMethodName(name), sig(JVM.OBJECT, params(ThreadContext.class, JVM.OBJECT, JVM.OBJECT, RubyClass.class, JVM.OBJECT, arity, Block.class)), Bootstrap.invokeSuper(), splatmapString);
        } else {
            adapter.invokedynamic("invokeInstanceSuper:" + JavaNameMangler.mangleMethodName(name), sig(JVM.OBJECT, params(ThreadContext.class, JVM.OBJECT, JVM.OBJECT, RubyClass.class, JVM.OBJECT, arity)), Bootstrap.invokeSuper(), splatmapString);
        }
    }

    public void invokeClassSuper(String name, int arity, boolean hasClosure, boolean[] splatmap) {
        String splatmapString = IRRuntimeHelpers.encodeSplatmap(splatmap);
        if (hasClosure) {
            adapter.invokedynamic("invokeClassSuper:" + JavaNameMangler.mangleMethodName(name), sig(JVM.OBJECT, params(ThreadContext.class, JVM.OBJECT, JVM.OBJECT, RubyClass.class, JVM.OBJECT, arity, Block.class)), Bootstrap.invokeSuper(), splatmapString);
        } else {
            adapter.invokedynamic("invokeClassSuper:" + JavaNameMangler.mangleMethodName(name), sig(JVM.OBJECT, params(ThreadContext.class, JVM.OBJECT, JVM.OBJECT, RubyClass.class, JVM.OBJECT, arity)), Bootstrap.invokeSuper(), splatmapString);
        }
    }

    public void invokeUnresolvedSuper(String name, int arity, boolean hasClosure, boolean[] splatmap) {
        String splatmapString = IRRuntimeHelpers.encodeSplatmap(splatmap);
        if (hasClosure) {
            adapter.invokedynamic("invokeUnresolvedSuper:" + JavaNameMangler.mangleMethodName(name), sig(JVM.OBJECT, params(ThreadContext.class, JVM.OBJECT, JVM.OBJECT, RubyClass.class, JVM.OBJECT, arity, Block.class)), Bootstrap.invokeSuper(), splatmapString);
        } else {
            adapter.invokedynamic("invokeUnresolvedSuper:" + JavaNameMangler.mangleMethodName(name), sig(JVM.OBJECT, params(ThreadContext.class, JVM.OBJECT, JVM.OBJECT, RubyClass.class, JVM.OBJECT, arity)), Bootstrap.invokeSuper(), splatmapString);
        }
    }

    public void invokeZSuper(String name, int arity, boolean hasClosure, boolean[] splatmap) {
        String splatmapString = IRRuntimeHelpers.encodeSplatmap(splatmap);
        if (hasClosure) {
            adapter.invokedynamic("invokeUnresolvedSuper:" + JavaNameMangler.mangleMethodName(name), sig(JVM.OBJECT, params(ThreadContext.class, JVM.OBJECT, JVM.OBJECT, RubyClass.class, JVM.OBJECT, arity, Block.class)), Bootstrap.invokeSuper(), splatmapString);
        } else {
            adapter.invokedynamic("invokeUnresolvedSuper:" + JavaNameMangler.mangleMethodName(name), sig(JVM.OBJECT, params(ThreadContext.class, JVM.OBJECT, JVM.OBJECT, RubyClass.class, JVM.OBJECT, arity)), Bootstrap.invokeSuper(), splatmapString);
        }
    }

    public void searchConst(String name, boolean noPrivateConsts) {
        adapter.invokedynamic("searchConst:" + name, sig(JVM.OBJECT, params(ThreadContext.class, StaticScope.class)), Bootstrap.searchConst(), noPrivateConsts?1:0);
    }

    public void inheritanceSearchConst(String name, boolean noPrivateConsts) {
        adapter.invokedynamic("inheritanceSearchConst:" + name, sig(JVM.OBJECT, params(ThreadContext.class, IRubyObject.class)), Bootstrap.searchConst(), noPrivateConsts?1:0);
    }

    public void lexicalSearchConst(String name) {
        adapter.invokedynamic("lexicalSearchConst:" + name, sig(JVM.OBJECT, params(ThreadContext.class, StaticScope.class)), Bootstrap.searchConst(), 0);
    }

    public void pushNil() {
        loadContext();
        adapter.invokedynamic("nil", sig(IRubyObject.class, ThreadContext.class), Bootstrap.contextValue());
    }

    public void pushBoolean(boolean b) {
        loadContext();
        adapter.invokedynamic(b ? "True" : "False", sig(IRubyObject.class, ThreadContext.class), Bootstrap.contextValue());
    }

    public void pushBignum(BigInteger bigint) {
        String bigintStr = bigint.toString();

        loadContext();

        adapter.invokedynamic("bignum", sig(RubyBignum.class, ThreadContext.class), Bootstrap.bignum(), bigintStr);
    }

    public void putField(String name) {
        adapter.invokedynamic("ivarSet:" + JavaNameMangler.mangleMethodName(name), sig(void.class, IRubyObject.class, IRubyObject.class), Bootstrap.ivar());
    }

    public void getField(String name) {
        adapter.invokedynamic("ivarGet:" + JavaNameMangler.mangleMethodName(name), sig(JVM.OBJECT, IRubyObject.class), Bootstrap.ivar());
    }

    public void array(int length) {
        adapter.invokedynamic("array", sig(JVM.OBJECT, params(ThreadContext.class, JVM.OBJECT, length)), Bootstrap.array());
    }

    public void hash(int length) {
        adapter.invokedynamic("hash", sig(JVM.OBJECT, params(ThreadContext.class, JVM.OBJECT, length * 2)), Bootstrap.hash());
    }

    public void checkpoint() {
        loadContext();
        adapter.invokedynamic(
                "checkpoint",
                sig(void.class, ThreadContext.class),
                InvokeDynamicSupport.checkpointHandle());
    }
}
