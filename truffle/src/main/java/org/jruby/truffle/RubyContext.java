/*
 * Copyright (c) 2013, 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerOptions;
import com.oracle.truffle.api.ExecutionContext;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.tools.CoverageTracker;
import org.jcodings.specific.UTF8Encoding;
import org.jruby.Ruby;
import org.jruby.runtime.builtin.IRubyObject;
import org.jruby.truffle.core.CoreLibrary;
import org.jruby.truffle.core.array.ArrayOperations;
import org.jruby.truffle.core.kernel.AtExitManager;
import org.jruby.truffle.core.kernel.TraceManager;
import org.jruby.truffle.core.objectspace.ObjectSpaceManager;
import org.jruby.truffle.core.rope.RopeTable;
import org.jruby.truffle.core.rubinius.RubiniusPrimitiveManager;
import org.jruby.truffle.core.string.StringOperations;
import org.jruby.truffle.core.symbol.SymbolTable;
import org.jruby.truffle.core.thread.ThreadManager;
import org.jruby.truffle.extra.AttachmentsManager;
import org.jruby.truffle.instrument.RubyDefaultASTProber;
import org.jruby.truffle.interop.InteropManager;
import org.jruby.truffle.interop.JRubyInterop;
import org.jruby.truffle.language.LexicalScope;
import org.jruby.truffle.language.ModuleOperations;
import org.jruby.truffle.language.Options;
import org.jruby.truffle.language.RubyGuards;
import org.jruby.truffle.language.SafepointManager;
import org.jruby.truffle.language.Warnings;
import org.jruby.truffle.language.arguments.RubyArguments;
import org.jruby.truffle.language.loader.CodeLoader;
import org.jruby.truffle.language.loader.FeatureLoader;
import org.jruby.truffle.language.loader.SourceCache;
import org.jruby.truffle.language.loader.SourceLoader;
import org.jruby.truffle.language.methods.DeclarationContext;
import org.jruby.truffle.language.methods.InternalMethod;
import org.jruby.truffle.platform.Graal;
import org.jruby.truffle.platform.NativePlatform;
import org.jruby.truffle.platform.NativePlatformFactory;
import org.jruby.truffle.tools.InstrumentationServerManager;
import org.jruby.truffle.tools.callgraph.CallGraph;
import org.jruby.truffle.tools.callgraph.SimpleWriter;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * The global state of a running Ruby system.
 */
public class RubyContext extends ExecutionContext {

    private static volatile RubyContext latestInstance;

    private final TruffleLanguage.Env env;

    private final Ruby jrubyRuntime;
    private final Options options;
    private final NativePlatform nativePlatform;
    private final CoreLibrary coreLibrary;
    private final FeatureLoader featureLoader;
    private final TraceManager traceManager;
    private final ObjectSpaceManager objectSpaceManager;
    private final ThreadManager threadManager;
    private final AtExitManager atExitManager;
    private final RopeTable ropeTable = new RopeTable();
    private final SymbolTable symbolTable = new SymbolTable(this);
    private final Warnings warnings;
    private final SafepointManager safepointManager;
    private final LexicalScope rootLexicalScope;
    private final CompilerOptions compilerOptions;
    private final RubiniusPrimitiveManager rubiniusPrimitiveManager;
    private final CoverageTracker coverageTracker;
    private final InstrumentationServerManager instrumentationServerManager;
    private final AttachmentsManager attachmentsManager;
    private final CodeLoader codeLoader;
    private final SourceCache sourceCache;
    private final CallGraph callGraph;
    private final PrintStream debugStandardOut;
    private final InteropManager interopManager;
    private final JRubyInterop jrubyInterop;

    private org.jruby.ast.RootNode initialJRubyRootNode;

    public RubyContext(Ruby jrubyRuntime, TruffleLanguage.Env env) {
        options = new Options();

        if (options.CALL_GRAPH) {
            callGraph = new CallGraph();
        } else {
            callGraph = null;
        }
        
        jrubyInterop = new JRubyInterop(this);

        latestInstance = this;

        assert jrubyRuntime != null;
        this.env = env;

        compilerOptions = Truffle.getRuntime().createCompilerOptions();

        if (!onGraal() && options.GRAAL_WARNING_UNLESS) {
            System.err.println("WARNING: This JVM does not have the Graal compiler. JRuby+Truffle's performance without it will be limited. " +
                    "See https://github.com/jruby/jruby/wiki/Truffle-FAQ#how-do-i-get-jrubytruffle");
        }

        if (compilerOptions.supportsOption("MinTimeThreshold")) {
            compilerOptions.setOption("MinTimeThreshold", 100000000);
        }

        if (compilerOptions.supportsOption("MinInliningMaxCallerSize")) {
            compilerOptions.setOption("MinInliningMaxCallerSize", 5000);
        }

        env.instrumenter().registerASTProber(new RubyDefaultASTProber(env.instrumenter()));

        // TODO(CS, 28-Jan-15) this is global
        // TODO(CS, 28-Jan-15) maybe not do this for core?
        if (options.COVERAGE || options.COVERAGE_GLOBAL) {
            coverageTracker = new CoverageTracker();

            if (options.COVERAGE_GLOBAL) {
                env.instrumenter().install(coverageTracker);
            }
        } else {
            coverageTracker = null;
        }

        safepointManager = new SafepointManager(this);

        this.jrubyRuntime = jrubyRuntime;

        warnings = new Warnings(this);

        // Object space manager needs to come early before we create any objects
        objectSpaceManager = new ObjectSpaceManager(this);

        coreLibrary = new CoreLibrary(this);

        nativePlatform = NativePlatformFactory.createPlatform(this);
        rootLexicalScope = new LexicalScope(null, coreLibrary.getObjectClass());

        org.jruby.Main.printTruffleTimeMetric("before-load-nodes");
        coreLibrary.initialize();
        rubiniusPrimitiveManager = new RubiniusPrimitiveManager();
        rubiniusPrimitiveManager.addAnnotatedPrimitives();
        org.jruby.Main.printTruffleTimeMetric("after-load-nodes");

        codeLoader = new CodeLoader(this);
        featureLoader = new FeatureLoader(this);
        traceManager = new TraceManager(this);
        atExitManager = new AtExitManager(this);

        threadManager = new ThreadManager(this);
        threadManager.initialize();

        if (options.INSTRUMENTATION_SERVER_PORT != 0) {
            instrumentationServerManager = new InstrumentationServerManager(this, options.INSTRUMENTATION_SERVER_PORT);
            instrumentationServerManager.start();
        } else {
            instrumentationServerManager = null;
        }

        attachmentsManager = new AttachmentsManager(this);
        sourceCache = new SourceCache(new SourceLoader(this));

        final PrintStream configStandardOut = jrubyRuntime.getInstanceConfig().getOutput();
        debugStandardOut = (configStandardOut == System.out) ? null : configStandardOut;

        // Give the core library manager a chance to tweak some of those methods

        coreLibrary.initializeAfterMethodsAdded();

        // Set program arguments

        for (IRubyObject arg : ((org.jruby.RubyArray) this.jrubyRuntime.getObject().getConstant("ARGV")).toJavaArray()) {
            assert arg != null;

            ArrayOperations.append(coreLibrary.getArgv(), StringOperations.createString(this, StringOperations.encodeRope(arg.toString(), UTF8Encoding.INSTANCE)));
        }

        // Set the load path

        DynamicObject receiver = coreLibrary.getGlobalVariablesObject();
        final DynamicObject loadPath = (DynamicObject) receiver.get("$:", coreLibrary.getNilObject());

        for (IRubyObject path : ((org.jruby.RubyArray) this.jrubyRuntime.getLoadService().getLoadPath()).toJavaArray()) {
            String pathString = path.toString();

            if (!(pathString.endsWith("lib/ruby/2.2/site_ruby")
                    || pathString.endsWith("lib/ruby/shared")
                    || pathString.endsWith("lib/ruby/stdlib"))) {

                if (pathString.startsWith("uri:classloader:")) {
                    pathString = SourceLoader.JRUBY_SCHEME + pathString.substring("uri:classloader:".length());
                }

                ArrayOperations.append(loadPath, StringOperations.createString(this, StringOperations.encodeRope(pathString, UTF8Encoding.INSTANCE)));
            }
        }

        // Load our own stdlib path

        String home = this.jrubyRuntime.getInstanceConfig().getJRubyHome();

        if (home.startsWith("uri:classloader:")) {
            home = home.substring("uri:classloader:".length());

            while (home.startsWith("/")) {
                home = home.substring(1);
            }

            home = SourceLoader.JRUBY_SCHEME + "/" + home;
        }

        home = home + "/";

        // Libraries copied unmodified from MRI
        ArrayOperations.append(loadPath, StringOperations.createString(this, StringOperations.encodeRope(home + "lib/ruby/truffle/mri", UTF8Encoding.INSTANCE)));

        // Our own implementations
        ArrayOperations.append(loadPath, StringOperations.createString(this, StringOperations.encodeRope(home + "lib/ruby/truffle/truffle", UTF8Encoding.INSTANCE)));

        // Libraries from RubySL
        for (String lib : Arrays.asList("rubysl-strscan", "rubysl-stringio",
                "rubysl-complex", "rubysl-date", "rubysl-pathname",
                "rubysl-tempfile", "rubysl-socket", "rubysl-securerandom",
                "rubysl-timeout", "rubysl-webrick")) {
            ArrayOperations.append(loadPath, StringOperations.createString(this, StringOperations.encodeRope(home + "lib/ruby/truffle/rubysl/" + lib + "/lib", UTF8Encoding.INSTANCE)));
        }

        // Shims
        ArrayOperations.append(loadPath, StringOperations.createString(this, StringOperations.encodeRope(home + "lib/ruby/truffle/shims", UTF8Encoding.INSTANCE)));

        interopManager = new InteropManager(this);
    }

    public Object send(Object object, String methodName, DynamicObject block, Object... arguments) {
        CompilerAsserts.neverPartOfCompilation();

        assert block == null || RubyGuards.isRubyProc(block);

        final InternalMethod method = ModuleOperations.lookupMethod(coreLibrary.getMetaClass(object), methodName);

        if (method == null || method.isUndefined()) {
            return null;
        }

        return method.getCallTarget().call(
                RubyArguments.pack(null, null, method, DeclarationContext.METHOD, null, object, block, arguments));
    }

    public void shutdown() {
        atExitManager.runSystemExitHooks();

        if (instrumentationServerManager != null) {
            instrumentationServerManager.shutdown();
        }

        threadManager.shutdown();

        if (options.COVERAGE_GLOBAL) {
            coverageTracker.print(System.out);
        }

        if (callGraph != null) {
            callGraph.resolve();

            if (options.CALL_GRAPH_WRITE != null) {
                try (PrintStream stream = new PrintStream(options.CALL_GRAPH_WRITE, StandardCharsets.UTF_8.name())) {
                    new SimpleWriter(callGraph, stream).write();
                } catch (FileNotFoundException | UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void setInitialJRubyRootNode(org.jruby.ast.RootNode initialJRubyRootNode) {
        this.initialJRubyRootNode = initialJRubyRootNode;
    }

    public org.jruby.ast.RootNode getInitialJRubyRootNode() {
        return initialJRubyRootNode;
    }

    public Options getOptions() {
        return options;
    }

    public TruffleLanguage.Env getEnv() {
        return env;
    }

    public NativePlatform getNativePlatform() {
        return nativePlatform;
    }

    public JRubyInterop getJRubyInterop() {
        return jrubyInterop;
    }

    public Ruby getJRubyRuntime() {
        return jrubyRuntime;
    }

    public CoreLibrary getCoreLibrary() {
        return coreLibrary;
    }

    public PrintStream getDebugStandardOut() {
        return debugStandardOut;
    }

    public FeatureLoader getFeatureLoader() {
        return featureLoader;
    }

    public ObjectSpaceManager getObjectSpaceManager() {
        return objectSpaceManager;
    }

    public ThreadManager getThreadManager() {
        return threadManager;
    }

    public AtExitManager getAtExitManager() {
        return atExitManager;
    }

    public TraceManager getTraceManager() {
        return traceManager;
    }

    public Warnings getWarnings() {
        return warnings;
    }

    public SafepointManager getSafepointManager() {
        return safepointManager;
    }

    public LexicalScope getRootLexicalScope() {
        return rootLexicalScope;
    }

    public CompilerOptions getCompilerOptions() {
        return compilerOptions;
    }

    public RubiniusPrimitiveManager getRubiniusPrimitiveManager() {
        return rubiniusPrimitiveManager;
    }

    public CoverageTracker getCoverageTracker() {
        return coverageTracker;
    }

    public static RubyContext getLatestInstance() {
        return latestInstance;
    }

    public AttachmentsManager getAttachmentsManager() {
        return attachmentsManager;
    }

    public SourceCache getSourceCache() {
        return sourceCache;
    }

    public RopeTable getRopeTable() {
        return ropeTable;
    }

    public SymbolTable getSymbolTable() {
        return symbolTable;
    }

    public CallGraph getCallGraph() {
        return callGraph;
    }

    public CodeLoader getCodeLoader() {
        return codeLoader;
    }

    public InteropManager getInteropManager() {
        return interopManager;
    }
}
