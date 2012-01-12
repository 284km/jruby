package org.jruby.internal.runtime.methods;

import org.jruby.RubyModule;
import org.jruby.compiler.ir.IRScope;
import org.jruby.compiler.ir.representations.CFG;
import org.jruby.interpreter.Interpreter;
import org.jruby.runtime.Arity;
import org.jruby.runtime.Block;
import org.jruby.runtime.DynamicScope;
import org.jruby.parser.StaticScope;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.Visibility;
import org.jruby.runtime.builtin.IRubyObject;
import org.jruby.util.log.Logger;
import org.jruby.util.log.LoggerFactory;

public class InterpretedIRMethod extends DynamicMethod {
    private static final Logger LOG = LoggerFactory.getLogger("InterpretedIRMethod");

    private final boolean  isTopLevel;
    private final IRScope method;
    private Arity arity;
    boolean displayedCFG = false; // FIXME: Remove when we find nicer way of logging CFG
    
    private InterpretedIRMethod(IRScope method, Visibility visibility, RubyModule implementationClass, boolean isTopLevel) {
        super(implementationClass, visibility, CallConfiguration.FrameNoneScopeNone);
        this.method = method;
        this.method.getStaticScope().determineModule();
        this.isTopLevel = isTopLevel;
        this.arity = calculateArity();
    }

    // We can probably use IRMethod callArgs for something (at least arity)
    public InterpretedIRMethod(IRScope method, RubyModule implementationClass) {
        this(method, Visibility.PRIVATE, implementationClass, false);
    }

    // We can probably use IRMethod callArgs for something (at least arity)
    public InterpretedIRMethod(IRScope method, RubyModule implementationClass, boolean isTopLevel) {
        this(method, Visibility.PRIVATE, implementationClass, isTopLevel);
    }

    // We can probably use IRMethod callArgs for something (at least arity)
    public InterpretedIRMethod(IRScope method, Visibility visibility, RubyModule implementationClass) {
        this(method, visibility, implementationClass, false);
    }
    
    public IRScope getIRMethod() {
        return method;
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
        if (Interpreter.isDebug()) {
            // FIXME: name should probably not be "" ever.
            String realName = name == null || "".equals(name) ? method.getName() : name;
            LOG.info("Executing '" + realName + "'");
        }

        // The base IR may not have been processed yet
        if (method.getInstrsForInterpretation() == null) method.prepareForInterpretation();

        if (Interpreter.isDebug() && displayedCFG == false) {
            CFG cfg = method.getCFG();
            LOG.info("Graph:\n" + cfg.toStringGraph());
            LOG.info("CFG:\n" + cfg.toStringInstrs());
            displayedCFG = true;
        }

        // SSS FIXME: Is this correct?
        if (isTopLevel) context.getRuntime().getObject().setConstantQuiet("TOPLEVEL_BINDING", context.getRuntime().newBinding(context.currentBinding()));

        try {
            // update call stacks (push: frame, class, scope, etc.)
            RubyModule implementationClass = getImplementationClass();
            context.preMethodFrameAndScope(implementationClass, name, self, block, method.getStaticScope());
            context.setCurrentVisibility(getVisibility());
            return Interpreter.INTERPRET_METHOD(context, method, self, name, implementationClass, args, block, null, false);
        } finally {
            // update call stacks (pop: ..)
            context.popFrame();
            context.postMethodScopeOnly();
        }
    }

    @Override
    public DynamicMethod dup() {
        return new InterpretedIRMethod(method, visibility, implementationClass);
    }
}
