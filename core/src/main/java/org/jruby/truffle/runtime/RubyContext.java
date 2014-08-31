/*
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.runtime;

import java.io.*;
import java.math.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.atomic.*;

import com.oracle.truffle.api.instrument.SourceCallback;
import org.jruby.Ruby;
import org.jruby.*;
import com.oracle.truffle.api.*;
import com.oracle.truffle.api.source.*;
import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.nodes.*;
import org.jruby.runtime.builtin.IRubyObject;
import org.jruby.truffle.TruffleHooks;
import org.jruby.truffle.nodes.RubyNode;
import org.jruby.truffle.nodes.debug.RubyASTProber;
import org.jruby.truffle.runtime.control.*;
import org.jruby.truffle.runtime.core.*;
import org.jruby.truffle.runtime.core.RubyArray;
import org.jruby.truffle.runtime.core.RubyBinding;
import org.jruby.truffle.runtime.core.RubyModule;
import org.jruby.truffle.runtime.core.RubyString;
import org.jruby.truffle.runtime.core.RubySymbol;
import org.jruby.truffle.runtime.core.RubyBasicObject;
import org.jruby.truffle.runtime.subsystems.*;
import org.jruby.truffle.translator.TranslatorDriver;
import org.jruby.util.ByteList;
import org.jruby.util.cli.Options;

/**
 * The global state of a running Ruby system.
 */
public class RubyContext extends ExecutionContext {

    public static final boolean PRINT_RUNTIME = Options.TRUFFLE_PRINT_RUNTIME.load();
    public static final boolean TRACE = Options.TRUFFLE_TRACE.load();
    public static final boolean OBJECTSPACE = Options.TRUFFLE_OBJECTSPACE.load();
    public static final boolean EXCEPTIONS_PRINT_JAVA = Options.TRUFFLE_EXCEPTIONS_PRINT_JAVA.load();
    public static final boolean LITERALS_INT = Options.TRUFFLE_LITERALS_INT.load();
    public static final int ARRAYS_UNINITIALIZED_SIZE = Options.TRUFFLE_ARRAYS_UNINITIALIZED_SIZE.load();
    public static final boolean ARRAYS_INT = Options.TRUFFLE_ARRAYS_INT.load();
    public static final boolean ARRAYS_LONG = Options.TRUFFLE_ARRAYS_LONG.load();
    public static final boolean ARRAYS_DOUBLE = Options.TRUFFLE_ARRAYS_DOUBLE.load();
    public static final boolean ARRAYS_OPTIMISTIC_LONG = Options.TRUFFLE_ARRAYS_OPTIMISTIC_LONG.load();
    public static final int ARRAYS_SMALL = Options.TRUFFLE_ARRAYS_SMALL.load();
    public static final int HASHES_SMALL = Options.TRUFFLE_HASHES_SMALL.load();
    public static final boolean COMPILER_PASS_LOOPS_THROUGH_BLOCKS = Options.TRUFFLE_COMPILER_PASS_LOOPS_THROUGH_BLOCKS.load();
    public static final boolean ALLOW_SIMPLE_SOURCE_SECTIONS = Options.TRUFFLE_ALLOW_SIMPLE_SOURCE_SECTIONS.load();

    private final Ruby runtime;
    private final TranslatorDriver translator;
    private final RubyASTProber astProber;
    private final CoreLibrary coreLibrary;
    private final FeatureManager featureManager;
    private final TraceManager traceManager;
    private final ObjectSpaceManager objectSpaceManager;
    private final ThreadManager threadManager;
    private final FiberManager fiberManager;
    private final AtExitManager atExitManager;
    private final RubySymbol.SymbolTable symbolTable = new RubySymbol.SymbolTable(this);
    private final Warnings warnings;

    private SourceCallback sourceCallback = null;

    private final AtomicLong nextObjectID = new AtomicLong(0);

    public RubyContext(Ruby runtime) {
        assert runtime != null;

        this.runtime = runtime;
        translator = new TranslatorDriver(this);
        astProber = new RubyASTProber();

        warnings = new Warnings(this);

        // Object space manager needs to come early before we create any objects
        objectSpaceManager = new ObjectSpaceManager(this);

        // See note in CoreLibrary#initialize to see why we need to break this into two statements
        coreLibrary = new CoreLibrary(this);
        coreLibrary.initialize();

        featureManager = new FeatureManager(this);
        traceManager = new TraceManager();
        atExitManager = new AtExitManager();

        // Must initialize threads before fibers

        threadManager = new ThreadManager(this);
        fiberManager = new FiberManager(this);
    }

    public void load(Source source, RubyNode currentNode) {
        execute(this, source, TranslatorDriver.ParserContext.TOP_LEVEL, coreLibrary.getMainObject(), null, currentNode);
    }

    public void loadFile(String fileName, RubyNode currentNode) {
        if (new File(fileName).isAbsolute()) {
            loadFileAbsolute(fileName, currentNode);
        } else {
            loadFileAbsolute(this.getRuntime().getCurrentDirectory() + File.separator + fileName, currentNode);
        }
    }

    private void loadFileAbsolute(String fileName, RubyNode currentNode) {
        final Source source;

        if (fileName.endsWith("spec/ruby/language/precedence_spec.rb") || fileName.endsWith("spec/ruby/language/predefined_spec.rb")) {
            // TODO(CS): we have trouble working out where unicode characters are

            String code;

            try {
                code = new String(Files.readAllBytes(Paths.get(fileName)), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            code = code.replace("“", " '");
            code = code.replace("”", " '");

            source = Source.fromText(code, fileName);
        } else {
            try {
                source = Source.fromFileName(fileName);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        final String code = source.getCode();
        if (code == null) {
            throw new RuntimeException("Can't read file " + fileName);
        }
        coreLibrary.getLoadedFeatures().slowPush(makeString(fileName));
        execute(this, source, TranslatorDriver.ParserContext.TOP_LEVEL, coreLibrary.getMainObject(), null, currentNode);
    }

    public RubySymbol.SymbolTable getSymbolTable() {
        return symbolTable;
    }

    @CompilerDirectives.SlowPath
    public RubySymbol newSymbol(String name) {
        return symbolTable.getSymbol(name);
    }

    @CompilerDirectives.SlowPath
    public RubySymbol newSymbol(ByteList name) {
        return symbolTable.getSymbol(name);
    }

    public Object eval(String code, RubyNode currentNode) {
        final Source source = Source.fromText(code, "(eval)");
        return execute(this, source, TranslatorDriver.ParserContext.TOP_LEVEL, coreLibrary.getMainObject(), null, currentNode);
    }

    public Object eval(String code, RubyModule module, RubyNode currentNode) {
        final Source source = Source.fromText(code, "(eval)");
        return execute(this, source, TranslatorDriver.ParserContext.MODULE, module, null, currentNode);
    }

    public Object eval(String code, RubyBinding binding, RubyNode currentNode) {
        final Source source = Source.fromText(code, "(eval)");
        return execute(this, source, TranslatorDriver.ParserContext.TOP_LEVEL, binding.getSelf(), binding.getFrame(), currentNode);
    }

    public void runShell(Node node, MaterializedFrame frame) {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        MaterializedFrame existingLocals = frame;

        String prompt = "Ruby> ";
        if (node != null) {
            final SourceSection src = node.getSourceSection();
            if (src != null) {
                prompt = (src.getSource().getName() + ":" + src.getStartLine() + "> ");
            }
        }

        while (true) {
            try {
                System.out.print(prompt);
                final String line = reader.readLine();

                final ShellResult result = evalShell(line, existingLocals);

                System.out.println("=> " + result.getResult());

                existingLocals = result.getFrame();
            } catch (BreakShellException e) {
                return;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public ShellResult evalShell(String code, MaterializedFrame existingLocals) {
        final Source source = Source.fromText(code, "shell");
        return (ShellResult) execute(this, source, TranslatorDriver.ParserContext.SHELL, coreLibrary.getMainObject(), existingLocals, null);
    }

    public Object execute(RubyContext context, Source source, TranslatorDriver.ParserContext parserContext, Object self, MaterializedFrame parentFrame, RubyNode currentNode) {
        final RubyParserResult parseResult = translator.parse(context, source, parserContext, parentFrame, currentNode);
        final CallTarget callTarget = Truffle.getRuntime().createCallTarget(parseResult.getRootNode());
        return callTarget.call(RubyArguments.pack(null, parentFrame, self, null, new Object[]{}));
    }

    public long getNextObjectID() {
        // TODO(CS): We can theoretically run out of long values

        final long id = nextObjectID.getAndIncrement();

        if (id < 0) {
            nextObjectID.set(Long.MIN_VALUE);
            throw new RuntimeException("Object IDs exhausted");
        }

        return id;
    }

    public void shutdown() {
        atExitManager.run();

        threadManager.leaveGlobalLock();

        objectSpaceManager.shutdown();

        if (fiberManager != null) {
            fiberManager.shutdown();
        }
    }

    public RubyString makeString(String string) {
        return RubyString.fromJavaString(coreLibrary.getStringClass(), string);
    }

    public RubyString makeString(char string) {
        return makeString(Character.toString(string));
    }

    public IRubyObject toJRuby(Object object) {
        RubyNode.notDesignedForCompilation();

        if (object instanceof NilPlaceholder) {
            return runtime.getNil();
        } else if (object == getCoreLibrary().getKernelModule()) {
            return runtime.getKernel();
        } else if (object == getCoreLibrary().getMainObject()) {
            return runtime.getTopSelf();
        } else if (object instanceof Boolean) {
            return runtime.newBoolean((boolean) object);
        } else if (object instanceof Integer) {
            return runtime.newFixnum((int) object);
        } else if (object instanceof Long) {
            return runtime.newFixnum((long) object);
        } else if (object instanceof Double) {
            return runtime.newFloat((double) object);
        } else if (object instanceof RubyString) {
            return toJRuby((RubyString) object);
        } else if (object instanceof RubyArray) {
            return toJRuby((RubyArray) object);
        } else {
            throw getRuntime().newRuntimeError("cannot pass " + object + " to JRuby");
        }
    }

    public org.jruby.RubyArray toJRuby(RubyArray array) {
        RubyNode.notDesignedForCompilation();

        final Object[] objects = array.slowToArray();
        final IRubyObject[] store = new IRubyObject[objects.length];

        for (int n = 0; n < objects.length; n++) {
            store[n] = toJRuby(objects[n]);
        }

        return runtime.newArray(store);
    }

    public org.jruby.RubyString toJRuby(RubyString string) {
        return runtime.newString(string.getBytes());
    }

    public Object toTruffle(IRubyObject object) {
        RubyNode.notDesignedForCompilation();

        if (object == runtime.getTopSelf()) {
            return getCoreLibrary().getMainObject();
        } else if (object == runtime.getKernel()) {
            return getCoreLibrary().getKernelModule();
        } else if (object instanceof RubyNil) {
            return NilPlaceholder.INSTANCE;
        } else if (object instanceof org.jruby.RubyBoolean.True) {
            return true;
        } else if (object instanceof org.jruby.RubyBoolean.False) {
            return false;
        } else if (object instanceof org.jruby.RubyFixnum) {
            final long value = ((org.jruby.RubyFixnum) object).getLongValue();

            if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
                throw new UnsupportedOperationException();
            }

            return (int) value;
        } else if (object instanceof org.jruby.RubyFloat) {
            return ((org.jruby.RubyFloat) object).getDoubleValue();
        } else if (object instanceof org.jruby.RubyString) {
            return toTruffle((org.jruby.RubyString) object);
        } else {
            throw object.getRuntime().newRuntimeError("cannot pass " + object.inspect() + " to Truffle");
        }
    }

    public RubyString toTruffle(org.jruby.RubyString string) {
        return new RubyString(getCoreLibrary().getStringClass(), string.getByteList());
    }

    public Ruby getRuntime() {
        return runtime;
    }

    public CoreLibrary getCoreLibrary() {
        return coreLibrary;
    }

    public FeatureManager getFeatureManager() {
        return featureManager;
    }

    public ObjectSpaceManager getObjectSpaceManager() {
        return objectSpaceManager;
    }

    public FiberManager getFiberManager() {
        return fiberManager;
    }

    public ThreadManager getThreadManager() {
        return threadManager;
    }

    public TranslatorDriver getTranslator() {
        return translator;
    }

    /**
     * Utility method to check if an object should be visible in a Ruby program. Used in assertions
     * at method boundaries to check that only values we want to be visible to the programmer become
     * so.
     */
    public static boolean shouldObjectBeVisible(Object object) {
        return object instanceof UndefinedPlaceholder || //
                object instanceof Boolean || //
                object instanceof Integer || //
                object instanceof Long || //
                object instanceof BigInteger || //
                object instanceof Double || //
                object instanceof RubyBasicObject || //
                object instanceof NilPlaceholder;
    }

    public static boolean shouldObjectsBeVisible(Object... objects) {
        return shouldObjectsBeVisible(objects.length, objects);
    }

    public static boolean shouldObjectsBeVisible(int length, Object... objects) {
        for (Object object : Arrays.asList(objects).subList(0, length)) {
            if (!shouldObjectBeVisible(object)) {
                return false;
            }
        }

        return true;
    }

    public AtExitManager getAtExitManager() {
        return atExitManager;
    }

    @Override
    public String getLanguageShortName() {
        return "ruby";
    }

    @Override
    public void setSourceCallback(SourceCallback sourceCallback) {
        this.sourceCallback = sourceCallback;
    }

    public SourceCallback getSourceCallback() {
        return sourceCallback;
    }

    public TruffleHooks getHooks() {
        return (TruffleHooks) runtime.getInstanceConfig().getTruffleHooks();
    }

    public RubyASTProber getASTProber() {
        return astProber;
    }

    public TraceManager getTraceManager() {
        return traceManager;
    }

    public Warnings getWarnings() {
        return warnings;
    }

}
