package org.jruby.internal.runtime.methods;

import java.util.ArrayList;
import java.util.List;

import org.jruby.MetaClass;
import org.jruby.Ruby;
import org.jruby.RubyClass;
import org.jruby.RubyModule;
import org.jruby.ir.*;
import org.jruby.ir.operands.InterpreterContext;
import org.jruby.ir.representations.CFG;
import org.jruby.ir.interpreter.Interpreter;
import org.jruby.ir.runtime.IRRuntimeHelpers;
import org.jruby.parser.StaticScope;
import org.jruby.runtime.Arity;
import org.jruby.runtime.Block;
import org.jruby.runtime.DynamicScope;
import org.jruby.runtime.PositionAware;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.Visibility;
import org.jruby.runtime.builtin.IRubyObject;
import org.jruby.util.cli.Options;
import org.jruby.util.log.Logger;
import org.jruby.util.log.LoggerFactory;

public class InterpretedIRMethod extends DynamicMethod implements IRMethodArgs, PositionAware {
    private static final Logger LOG = LoggerFactory.getLogger("InterpretedIRMethod");

    private Arity arity;
    private boolean displayedCFG = false; // FIXME: Remove when we find nicer way of logging CFG
    private boolean pushScope;

    protected final IRScope method;

    private static class DynamicMethodBox {
        public DynamicMethod actualMethod;
        public int callCount = 0;
    }

    private DynamicMethodBox box = new DynamicMethodBox();

    public InterpretedIRMethod(IRScope method, Visibility visibility, RubyModule implementationClass) {
        super(implementationClass, visibility, CallConfiguration.FrameNoneScopeNone, method.getName());
        this.method = method;
        this.method.getStaticScope().determineModule();
        this.arity = calculateArity();
        this.pushScope = true;
        if (!implementationClass.getRuntime().getInstanceConfig().getCompileMode().shouldJIT()) {
            this.box.callCount = -1;
        }
    }

    public IRScope getIRMethod() {
        return method;
    }

    public DynamicMethod getActualMethod() {
        return box.actualMethod;
    }

    public void setCallCount(int callCount) {
        box.callCount = callCount;
    }

    public StaticScope getStaticScope() {
        return method.getStaticScope();
    }

    public List<String[]> getParameterList() {
        return (method instanceof IRMethod) ? ((IRMethod)method).getArgDesc() : new ArrayList<String[]>();
    }

    private Arity calculateArity() {
        StaticScope s = method.getStaticScope();
        if (s.getOptionalArgs() > 0 || s.getRestArg() >= 0) return Arity.required(s.getRequiredArgs());

        return Arity.createArity(s.getRequiredArgs());
    }

    @Override
    public Arity getArity() {
        return this.arity;
    }

    @Override
    public IRubyObject call(ThreadContext context, IRubyObject self, RubyModule clazz, String name, IRubyObject[] args, Block block) {
        DynamicMethodBox box = this.box;
        if (box.callCount >= 0) tryJit(context, box);
        DynamicMethod actualMethod = box.actualMethod;
        if (actualMethod != null) return actualMethod.call(context, self, clazz, name, args, block);

        ensureInstrsReady();

        if (IRRuntimeHelpers.isDebug()) doDebug();

        if (method.hasExplicitCallProtocol()) return Interpreter.INTERPRET_METHOD(context, this, self, name, args, block);

        pre(context, self, name, block);

        try {
            return Interpreter.INTERPRET_METHOD(context, this, self, name, args, block);
        } finally {
            post(context);
        }
    }

    protected void doDebug() {
        // FIXME: name should probably not be "" ever.
        String realName = name == null || "".equals(name) ? method.getName() : name;
        LOG.info("Executing '" + realName + "'");
        if (displayedCFG == false) {
            // The base IR may not have been processed yet
            CFG cfg = method.getCFG();
            LOG.info("Graph:\n" + cfg.toStringGraph());
            LOG.info("CFG:\n" + cfg.toStringInstrs());
            displayedCFG = true;
        }
    }

    protected void post(ThreadContext context) {
        // update call stacks (pop: ..)
        context.popFrame();
        if (this.pushScope) {
            context.popScope();
        }
    }

    protected void pre(ThreadContext context, IRubyObject self, String name, Block block) {
        // update call stacks (push: frame, class, scope, etc.)
        StaticScope ss = method.getStaticScope();
        context.preMethodFrameAndClass(getImplementationClass(), name, self, block, ss);
        if (this.pushScope) {
            context.pushScope(DynamicScope.newDynamicScope(ss));
        }
        context.setCurrentVisibility(getVisibility());
    }

    public void ensureInstrsReady() {
        // SSS FIXME: Move this out of here to some other place?
        // Prepare method if not yet done so we know if the method has an explicit/implicit call protocol
        if (method.getInstrsForInterpretation() == null) {
            InterpreterContext context = method.prepareForInterpretation(false);
            this.pushScope = !context.getFlags().contains(IRFlags.DYNSCOPE_ELIMINATED);
        }
    }

    public DynamicMethod getMethodForCaching() {
        DynamicMethod method = box.actualMethod;
        if (method instanceof CompiledIRMethod) {
            return method;
        }
        return this;
    }

    public void switchToJitted(CompiledIRMethod newMethod) {
        this.box.actualMethod = newMethod;
        this.box.actualMethod.serialNumber = this.serialNumber;
        this.box.callCount = -1;
        getImplementationClass().invalidateCacheDescendants();
    }


    private void tryJit(ThreadContext context, DynamicMethodBox box) {
        Ruby runtime = context.runtime;

        // don't JIT during runtime boot
        if (runtime.isBooting()) return;

        String className;
        if (implementationClass.isSingleton()) {
            MetaClass metaClass = (MetaClass)implementationClass;
            RubyClass realClass = metaClass.getRealClass();
            // if real class is Class
            if (realClass == context.runtime.getClassClass()) {
                // use the attached class's name
                className = ((RubyClass)metaClass.getAttached()).getName();
            } else {
                // use the real class name
                className = realClass.getName();
            }
        } else {
            // use the class name
            className = implementationClass.getName();
        }


        if (box.callCount++ >= Options.JIT_THRESHOLD.load()) {
            context.runtime.getJITCompiler().jitThresholdReached(this, context.runtime.getInstanceConfig(), context, className, name);
        }
    }

    public void setActualMethod(CompiledIRMethod method) {
        this.box.actualMethod = method;
    }

    protected void dupBox(InterpretedIRMethod orig) {
        this.box = orig.box;
    }

    @Override
    public DynamicMethod dup() {
        InterpretedIRMethod x = new InterpretedIRMethod(method, visibility, implementationClass);
        x.box = box;

        return x;
    }

    public String getFile() {
        return method.getFileName();
    }

    public int getLine() {
        return method.getLineNumber();
   }
}
