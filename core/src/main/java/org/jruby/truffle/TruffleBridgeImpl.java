/*
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.frame.MaterializedFrame;
import org.jruby.TruffleBridge;
import org.jruby.internal.runtime.methods.DynamicMethod;
import org.jruby.runtime.builtin.IRubyObject;
import org.jruby.truffle.nodes.RubyNode;
import org.jruby.truffle.nodes.core.CoreMethodNodeManager;
import org.jruby.truffle.nodes.methods.MethodDefinitionNode;
import org.jruby.truffle.runtime.NilPlaceholder;
import org.jruby.truffle.runtime.RubyArguments;
import org.jruby.truffle.runtime.RubyContext;
import org.jruby.truffle.runtime.RubyParserResult;
import org.jruby.truffle.runtime.backtrace.MRIBacktraceFormatter;
import org.jruby.truffle.runtime.control.RaiseException;
import org.jruby.truffle.runtime.core.RubyArray;
import org.jruby.truffle.runtime.core.RubyException;
import org.jruby.truffle.translator.TranslatorDriver;

public class TruffleBridgeImpl implements TruffleBridge {

    private final org.jruby.Ruby runtime;
    private final RubyContext truffleContext;

    public TruffleBridgeImpl(org.jruby.Ruby runtime) {
        assert runtime != null;

        this.runtime = runtime;

        // Set up a context

        truffleContext = new RubyContext(runtime);
    }

    @Override
    public void init() {
        if (RubyContext.PRINT_RUNTIME) {
            runtime.getInstanceConfig().getError().println("jruby: using " + Truffle.getRuntime().getName());
        }

        // Bring in core method nodes

        CoreMethodNodeManager.addStandardMethods(truffleContext.getCoreLibrary().getObjectClass());

        // Give the core library manager a chance to tweak some of those methods

        truffleContext.getCoreLibrary().initializeAfterMethodsAdded();

        // Set program arguments

        for (IRubyObject arg : ((org.jruby.RubyArray) runtime.getObject().getConstant("ARGV")).toJavaArray()) {
            assert arg != null;

            truffleContext.getCoreLibrary().getArgv().slowPush(truffleContext.makeString(arg.toString()));
        }

        // Set the load path

        final RubyArray loadPath = (RubyArray) truffleContext.getCoreLibrary().getGlobalVariablesObject().getInstanceVariable("$:");

        for (IRubyObject path : ((org.jruby.RubyArray) runtime.getLoadService().getLoadPath()).toJavaArray()) {
            loadPath.slowPush(truffleContext.makeString(path.toString()));
        }

        // Hook

        if (truffleContext.getHooks() != null) {
            truffleContext.getHooks().afterInit(truffleContext);
        }
    }

    @Override
    public TruffleMethod truffelize(DynamicMethod originalMethod, org.jruby.ast.ArgsNode argsNode, org.jruby.ast.Node bodyNode) {
        final MethodDefinitionNode methodDefinitionNode = truffleContext.getTranslator().parse(truffleContext, null, argsNode, bodyNode, null);
        return new TruffleMethod(originalMethod, Truffle.getRuntime().createCallTarget(methodDefinitionNode.getMethodRootNode()));
    }

    @Override
    public Object execute(TranslatorDriver.ParserContext parserContext, Object self, MaterializedFrame parentFrame, org.jruby.ast.RootNode rootNode) {
        try {
            final RubyParserResult parseResult = truffleContext.getTranslator().parse(truffleContext, truffleContext.getSourceManager().get(rootNode.getPosition().getFile()), parserContext, parentFrame, null);
            final CallTarget callTarget = Truffle.getRuntime().createCallTarget(parseResult.getRootNode());
            return callTarget.call(RubyArguments.pack(parentFrame, self, null));
        } catch (RaiseException e) {
            // TODO(CS): what's this cast about?
            final RubyException rubyException = (RubyException) e.getRubyException();

            for (String line : new MRIBacktraceFormatter().format(truffleContext, rubyException, rubyException.getBacktrace())) {
                System.err.println(line);
            }

            return NilPlaceholder.INSTANCE;
        }
    }

    @Override
    public IRubyObject toJRuby(Object object) {
        return truffleContext.toJRuby(object);
    }

    @Override
    public Object toTruffle(IRubyObject object) {
        return truffleContext.toTruffle(object);
    }

    @Override
    public void shutdown() {
        truffleContext.shutdown();
    }

}
