/*
 * Copyright (c) 2013, 2014 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.runtime.core;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.source.Source;
import org.jcodings.specific.BIG5Encoding;
import org.jcodings.specific.EUCJPEncoding;
import org.jcodings.specific.SJISEncoding;
import org.jcodings.specific.USASCIIEncoding;
import org.jruby.runtime.Constants;
import org.jruby.runtime.Visibility;
import org.jruby.truffle.nodes.RubyNode;
import org.jruby.truffle.nodes.core.ArrayNodes;
import org.jruby.truffle.runtime.ModuleOperations;
import org.jruby.truffle.runtime.RubyCallStack;
import org.jruby.truffle.runtime.RubyContext;
import org.jruby.truffle.runtime.rubinius.RubiniusLibrary;
import org.jruby.truffle.translator.TranslatorDriver;
import org.jruby.util.cli.Options;
import org.jruby.util.cli.OutputStrings;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.Map;

public class CoreLibrary {

    private final RubyContext context;

    @CompilerDirectives.CompilationFinal private RubyClass argumentErrorClass;
    @CompilerDirectives.CompilationFinal private RubyClass arrayClass;
    @CompilerDirectives.CompilationFinal private RubyClass basicObjectClass;
    @CompilerDirectives.CompilationFinal private RubyClass bignumClass;
    @CompilerDirectives.CompilationFinal private RubyClass bindingClass;
    @CompilerDirectives.CompilationFinal private RubyClass classClass;
    @CompilerDirectives.CompilationFinal private RubyClass continuationClass;
    @CompilerDirectives.CompilationFinal private RubyClass dirClass;
    @CompilerDirectives.CompilationFinal private RubyClass encodingClass;
    @CompilerDirectives.CompilationFinal private RubyClass exceptionClass;
    @CompilerDirectives.CompilationFinal private RubyClass falseClass;
    @CompilerDirectives.CompilationFinal private RubyClass fiberClass;
    @CompilerDirectives.CompilationFinal private RubyClass fileClass;
    @CompilerDirectives.CompilationFinal private RubyClass fixnumClass;
    @CompilerDirectives.CompilationFinal private RubyClass floatClass;
    @CompilerDirectives.CompilationFinal private RubyClass hashClass;
    @CompilerDirectives.CompilationFinal private RubyClass integerClass;
    @CompilerDirectives.CompilationFinal private RubyClass ioClass;
    @CompilerDirectives.CompilationFinal private RubyClass loadErrorClass;
    @CompilerDirectives.CompilationFinal private RubyClass localJumpErrorClass;
    @CompilerDirectives.CompilationFinal private RubyClass matchDataClass;
    @CompilerDirectives.CompilationFinal private RubyClass moduleClass;
    @CompilerDirectives.CompilationFinal private RubyClass nameErrorClass;
    @CompilerDirectives.CompilationFinal private RubyClass nilClass;
    @CompilerDirectives.CompilationFinal private RubyClass noMethodErrorClass;
    @CompilerDirectives.CompilationFinal private RubyClass numericClass;
    @CompilerDirectives.CompilationFinal private RubyClass objectClass;
    @CompilerDirectives.CompilationFinal private RubyClass procClass;
    @CompilerDirectives.CompilationFinal private RubyClass processClass;
    @CompilerDirectives.CompilationFinal private RubyClass rangeClass;
    @CompilerDirectives.CompilationFinal private RubyClass rangeErrorClass;
    @CompilerDirectives.CompilationFinal private RubyClass regexpClass;
    @CompilerDirectives.CompilationFinal private RubyClass rubyTruffleErrorClass;
    @CompilerDirectives.CompilationFinal private RubyClass runtimeErrorClass;
    @CompilerDirectives.CompilationFinal private RubyClass standardErrorClass;
    @CompilerDirectives.CompilationFinal private RubyClass stringClass;
    @CompilerDirectives.CompilationFinal private RubyClass symbolClass;
    @CompilerDirectives.CompilationFinal private RubyClass syntaxErrorClass;
    @CompilerDirectives.CompilationFinal private RubyClass systemCallErrorClass;
    @CompilerDirectives.CompilationFinal private RubyClass systemExitClass;
    @CompilerDirectives.CompilationFinal private RubyClass threadClass;
    @CompilerDirectives.CompilationFinal private RubyClass timeClass;
    @CompilerDirectives.CompilationFinal private RubyClass trueClass;
    @CompilerDirectives.CompilationFinal private RubyClass typeErrorClass;
    @CompilerDirectives.CompilationFinal private RubyClass zeroDivisionErrorClass;
    @CompilerDirectives.CompilationFinal private RubyModule comparableModule;
    @CompilerDirectives.CompilationFinal private RubyModule configModule;
    @CompilerDirectives.CompilationFinal private RubyModule enumerableModule;
    @CompilerDirectives.CompilationFinal private RubyModule errnoModule;
    @CompilerDirectives.CompilationFinal private RubyModule gcModule;
    @CompilerDirectives.CompilationFinal private RubyModule kernelModule;
    @CompilerDirectives.CompilationFinal private RubyModule mathModule;
    @CompilerDirectives.CompilationFinal private RubyModule objectSpaceModule;
    @CompilerDirectives.CompilationFinal private RubyModule signalModule;
    @CompilerDirectives.CompilationFinal private RubyModule truffleModule;
    @CompilerDirectives.CompilationFinal private RubyModule truffleDebugModule;
    @CompilerDirectives.CompilationFinal private RubyClass edomClass;
    @CompilerDirectives.CompilationFinal private RubyClass encodingConverterClass;

    @CompilerDirectives.CompilationFinal private RubyArray argv;
    @CompilerDirectives.CompilationFinal private RubyBasicObject globalVariablesObject;
    @CompilerDirectives.CompilationFinal private RubyBasicObject mainObject;
    @CompilerDirectives.CompilationFinal private RubyFalseClass falseObject;
    @CompilerDirectives.CompilationFinal private RubyNilClass nilObject;
    @CompilerDirectives.CompilationFinal private RubyTrueClass trueObject;
    @CompilerDirectives.CompilationFinal private RubyHash envHash;

    private ArrayNodes.MinBlock arrayMinBlock;
    private ArrayNodes.MaxBlock arrayMaxBlock;

    @CompilerDirectives.CompilationFinal private RubiniusLibrary rubiniusLibrary;

    public CoreLibrary(RubyContext context) {
        this.context = context;
    }

    public void initialize() {
        // Create the cyclic classes and modules

        classClass = new RubyClass.RubyClassClass(context);
        basicObjectClass = RubyClass.createBootClass(context, "BasicObject");
        objectClass = RubyClass.createBootClass(context, "Object");
        moduleClass = new RubyModule.RubyModuleClass(context);

        // Close the cycles
        classClass.unsafeSetLogicalClass(classClass);

        objectClass.unsafeSetSuperclass(basicObjectClass);
        moduleClass.unsafeSetSuperclass(objectClass);
        classClass.unsafeSetSuperclass(moduleClass);

        classClass.getAdoptedByLexicalParent(objectClass, null);
        basicObjectClass.getAdoptedByLexicalParent(objectClass, null);
        objectClass.getAdoptedByLexicalParent(objectClass, null);
        moduleClass.getAdoptedByLexicalParent(objectClass, null);

        // BasicObject knows itself

        basicObjectClass.setConstant(null, "BasicObject", basicObjectClass);

        // Create all other classes and modules

        numericClass = new RubyClass(context, objectClass, objectClass, "Numeric");
        integerClass = new RubyClass(context, objectClass, numericClass, "Integer");

        exceptionClass = new RubyException.RubyExceptionClass(context, objectClass, objectClass, "Exception");
        standardErrorClass = new RubyException.RubyExceptionClass(context, objectClass, exceptionClass, "StandardError");

        ioClass = new RubyClass(context, objectClass, objectClass, "IO");

        argumentErrorClass = new RubyException.RubyExceptionClass(context, objectClass, standardErrorClass, "ArgumentError");
        arrayClass = new RubyArray.RubyArrayClass(context, objectClass);
        bignumClass = new RubyClass(context, objectClass, integerClass, "Bignum");
        bindingClass = new RubyClass(context, objectClass, objectClass, "Binding");
        comparableModule = new RubyModule(context, objectClass, "Comparable");
        configModule = new RubyModule(context, objectClass, "Config");
        continuationClass = new RubyClass(context, objectClass, objectClass, "Continuation");
        dirClass = new RubyClass(context, objectClass, objectClass, "Dir");
        encodingClass = new RubyEncoding.RubyEncodingClass(context, objectClass);
        errnoModule = new RubyModule(context, objectClass, "Errno");
        enumerableModule = new RubyModule(context, objectClass, "Enumerable");
        falseClass = new RubyClass(context, objectClass, objectClass, "FalseClass");
        fiberClass = new RubyFiber.RubyFiberClass(context, objectClass);
        fileClass = new RubyClass(context, objectClass, ioClass, "File");
        fixnumClass = new RubyClass(context, objectClass, integerClass, "Fixnum");
        floatClass = new RubyClass(context, objectClass, numericClass, "Float");
        gcModule = new RubyModule(context, objectClass, "GC");
        hashClass = new RubyHash.RubyHashClass(context, objectClass);
        kernelModule = new RubyModule(context, objectClass, "Kernel");
        loadErrorClass = new RubyException.RubyExceptionClass(context, objectClass, standardErrorClass, "LoadError");
        localJumpErrorClass = new RubyException.RubyExceptionClass(context, objectClass, standardErrorClass, "LocalJumpError");
        matchDataClass = new RubyClass(context, objectClass, objectClass, "MatchData");
        mathModule = new RubyModule(context, objectClass, "Math");
        nameErrorClass = new RubyClass(context, objectClass, standardErrorClass, "NameError");
        nilClass = new RubyClass(context, objectClass, objectClass, "NilClass");
        noMethodErrorClass = new RubyException.RubyExceptionClass(context, objectClass, nameErrorClass, "NoMethodError");
        objectSpaceModule = new RubyModule(context, objectClass, "ObjectSpace");
        procClass = new RubyProc.RubyProcClass(context, objectClass);
        processClass = new RubyClass(context, objectClass, objectClass, "Process");
        rangeClass = new RubyClass(context, objectClass, objectClass, "Range");
        rangeErrorClass = new RubyException.RubyExceptionClass(context, objectClass, standardErrorClass, "RangeError");
        regexpClass = new RubyRegexp.RubyRegexpClass(context, objectClass);
        rubyTruffleErrorClass = new RubyException.RubyExceptionClass(context, objectClass, standardErrorClass, "RubyTruffleError");
        runtimeErrorClass = new RubyException.RubyExceptionClass(context, objectClass, standardErrorClass, "RuntimeError");
        signalModule = new RubyModule(context, objectClass, "Signal");
        stringClass = new RubyString.RubyStringClass(context, objectClass, objectClass, "String");
        symbolClass = new RubyClass(context, objectClass, objectClass, "Symbol");
        syntaxErrorClass = new RubyException.RubyExceptionClass(context, objectClass, standardErrorClass, "SyntaxError");
        systemCallErrorClass = new RubyException.RubyExceptionClass(context, objectClass, standardErrorClass, "SystemCallError");
        systemExitClass = new RubyException.RubyExceptionClass(context, objectClass, exceptionClass, "SystemExit");
        threadClass = new RubyThread.RubyThreadClass(context, objectClass);
        timeClass = new RubyTime.RubyTimeClass(context, objectClass);
        trueClass = new RubyClass(context, objectClass, objectClass, "TrueClass");
        truffleModule = new RubyModule(context, objectClass, "Truffle");
        truffleDebugModule = new RubyModule(context, objectClass, "Debug");
        typeErrorClass = new RubyException.RubyExceptionClass(context, objectClass, standardErrorClass, "TypeError");
        zeroDivisionErrorClass = new RubyException.RubyExceptionClass(context, objectClass, standardErrorClass, "ZeroDivisionError");
        encodingConverterClass = new RubyEncodingConverter.RubyEncodingConverterClass(context, encodingClass, objectClass);

        // Includes

        objectClass.include(null, kernelModule);

        // Set constants

        objectClass.setConstant(null, "RUBY_VERSION", RubyString.fromJavaString(stringClass, "2.1.0"));
        objectClass.setConstant(null, "RUBY_PATCHLEVEL", 0);
        objectClass.setConstant(null, "RUBY_ENGINE", RubyString.fromJavaString(stringClass, "jrubytruffle"));
        objectClass.setConstant(null, "RUBY_PLATFORM", RubyString.fromJavaString(stringClass, "jvm"));

        final LinkedHashMap<Object, Object> configHashMap = new LinkedHashMap<>();
        configHashMap.put(RubyString.fromJavaString(stringClass, "ruby_install_name"), RubyString.fromJavaString(stringClass, "rubytruffle"));
        configHashMap.put(RubyString.fromJavaString(stringClass, "RUBY_INSTALL_NAME"), RubyString.fromJavaString(stringClass, "rubytruffle"));
        configHashMap.put(RubyString.fromJavaString(stringClass, "host_os"), RubyString.fromJavaString(stringClass, "unknown"));
        configHashMap.put(RubyString.fromJavaString(stringClass, "exeext"), RubyString.fromJavaString(stringClass, ""));
        configHashMap.put(RubyString.fromJavaString(stringClass, "EXEEXT"), RubyString.fromJavaString(stringClass, "rubytruffle"));

        edomClass = new RubyException.RubyExceptionClass(context, errnoModule, systemCallErrorClass, "EDOM");
        new RubyClass(context, errnoModule, systemCallErrorClass, "ENOENT");
        new RubyClass(context, errnoModule, systemCallErrorClass, "EPERM");
        new RubyClass(context, errnoModule, systemCallErrorClass, "ENOTEMPTY");
        new RubyClass(context, errnoModule, systemCallErrorClass, "EEXIST");
        new RubyClass(context, errnoModule, systemCallErrorClass, "EXDEV");
        new RubyClass(context, errnoModule, systemCallErrorClass, "EACCES");

        // TODO(cs): this should be a separate exception
        mathModule.setConstant(null, "DomainError", edomClass);

        // TODO(cs): the alias should be the other way round, Config is legacy (and should warn).
        objectClass.setConstant(null, "RbConfig", configModule);

        // Create some key objects

        mainObject = new RubyObject(objectClass);
        nilObject = new RubyNilClass(nilClass);
        trueObject = new RubyTrueClass(trueClass);
        falseObject = new RubyFalseClass(falseClass);

        // Create the globals object

        globalVariablesObject = new RubyBasicObject(objectClass);
        globalVariablesObject.switchToPrivateLayout();
        globalVariablesObject.setInstanceVariable("$LOAD_PATH", new RubyArray(arrayClass));
        globalVariablesObject.setInstanceVariable("$LOADED_FEATURES", new RubyArray(arrayClass));
        globalVariablesObject.setInstanceVariable("$:", globalVariablesObject.getInstanceVariable("$LOAD_PATH"));
        globalVariablesObject.setInstanceVariable("$\"", globalVariablesObject.getInstanceVariable("$LOADED_FEATURES"));

        initializeEncodingConstants();

        arrayMinBlock = new ArrayNodes.MinBlock(context);
        arrayMaxBlock = new ArrayNodes.MaxBlock(context);

        argv = new RubyArray(arrayClass);
        envHash = getSystemEnv();
        objectClass.setConstant(null, "ARGV", argv);
        objectClass.setConstant(null, "ENV", envHash);
        objectClass.setConstant(null, "TRUE", true);
        objectClass.setConstant(null, "FALSE", false);
        objectClass.setConstant(null, "NIL", nilObject);

        final RubyHash configHash = new RubyHash(hashClass, null, null, configHashMap, 0);
        configModule.setConstant(null, "CONFIG", configHash);

        floatClass.setConstant(null, "EPSILON", org.jruby.RubyFloat.EPSILON);
        floatClass.setConstant(null, "INFINITY", org.jruby.RubyFloat.INFINITY);
        floatClass.setConstant(null, "NAN", org.jruby.RubyFloat.NAN);

        mathModule.setConstant(null, "PI", Math.PI);
        mathModule.setConstant(null, "E", Math.E);

        fileClass.setConstant(null, "SEPARATOR", RubyString.fromJavaString(stringClass, File.separator));
        fileClass.setConstant(null, "Separator", RubyString.fromJavaString(stringClass, File.separator));
        fileClass.setConstant(null, "ALT_SEPARATOR", nilObject);
        fileClass.setConstant(null, "PATH_SEPARATOR", RubyString.fromJavaString(stringClass, File.pathSeparator));
        fileClass.setConstant(null, "FNM_SYSCASE", 0);

        globalVariablesObject.setInstanceVariable("$DEBUG", context.getRuntime().isDebug());
        globalVariablesObject.setInstanceVariable("$VERBOSE", context.getRuntime().warningsEnabled() ? context.getRuntime().isVerbose() : nilObject);

    }

    public void initializeAfterMethodsAdded() {
        // Just create a dummy object for $stdout - we can use Kernel#print and a special method TruffleDebug.flush_stdout

        final RubyBasicObject stdout = new RubyBasicObject(objectClass);
        stdout.getSingletonClass(null).addMethod(null, ModuleOperations.lookupMethod(stdout.getMetaClass(), "print").withVisibility(Visibility.PUBLIC));
        stdout.getSingletonClass(null).addMethod(null, ModuleOperations.lookupMethod(truffleDebugModule.getSingletonClass(null), "flush_stdout").withNewName("flush"));
        globalVariablesObject.setInstanceVariable("$stdout", stdout);

        objectClass.setConstant(null, "STDIN", new RubyBasicObject(objectClass));
        objectClass.setConstant(null, "STDOUT", globalVariablesObject.getInstanceVariable("$stdout"));
        objectClass.setConstant(null, "STDERR", globalVariablesObject.getInstanceVariable("$stdout"));
        objectClass.setConstant(null, "RUBY_RELEASE_DATE", context.makeString(Constants.COMPILE_DATE));
        objectClass.setConstant(null, "RUBY_DESCRIPTION", context.makeString(OutputStrings.getVersionString()));

        if (Options.TRUFFLE_LOAD_CORE.load()) {
            final String[] files = new String[]{
                    "jruby/truffle/core/kernel.rb"
            };

            for (String file : files) {
                loadRubyCore(file);
            }
        }

        rubiniusLibrary = new RubiniusLibrary(this);
    }

    public void loadRubyCore(String fileName) {
        final Source source;

        try {
            source = Source.fromReader(new InputStreamReader(context.getRuntime().getLoadService().getClassPathResource(context.getRuntime().getJRubyClassLoader(), fileName).getInputStream()), fileName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        context.execute(context, source, TranslatorDriver.ParserContext.TOP_LEVEL, mainObject, null, null);
    }

    public void initializeEncodingConstants() {
        encodingClass.setConstant(null, "US_ASCII", RubyEncoding.getEncoding(context, USASCIIEncoding.INSTANCE));
        encodingClass.setConstant(null, "ASCII_8BIT", RubyEncoding.getEncoding(context, USASCIIEncoding.INSTANCE));
        encodingClass.setConstant(null, "UTF_8", RubyEncoding.getEncoding(context, USASCIIEncoding.INSTANCE));
        encodingClass.setConstant(null, "EUC_JP", RubyEncoding.getEncoding(context, EUCJPEncoding.INSTANCE));
        encodingClass.setConstant(null, "Windows_31J", RubyEncoding.getEncoding(context, SJISEncoding.INSTANCE));
        encodingClass.setConstant(null, "Big5", RubyEncoding.getEncoding(context, BIG5Encoding.INSTANCE));

    }

    public RubyBasicObject box(Object object) {
        RubyNode.notDesignedForCompilation();

        // TODO(cs): pool common object instances like small Fixnums?

        if (object instanceof RubyBasicObject) {
            return (RubyBasicObject) object;
        }

        if (object instanceof Boolean) {
            if ((boolean) object) {
                return trueObject;
            } else {
                return falseObject;
            }
        }

        if (object instanceof Integer) {
            return new RubyFixnum.IntegerFixnum(fixnumClass, (int) object);
        }

        if (object instanceof Long) {
            return new RubyFixnum.LongFixnum(fixnumClass, (long) object);
        }

        if (object instanceof BigInteger) {
            return new RubyBignum(bignumClass, (BigInteger) object);
        }

        if (object instanceof Double) {
            return new RubyFloat(floatClass, (double) object);
        }

        if (object instanceof RubyNilClass) {
            return nilObject;
        }

        if (object instanceof String) {
            return context.makeString((String) object);
        }

        CompilerDirectives.transferToInterpreter();

        throw new UnsupportedOperationException("Don't know how to box " + object.getClass().getName());
    }

    /**
     * Convert a value to a boolean according to Ruby rules. Never fails.
     */
    public boolean isTruthy(Object value) {
        if (value instanceof Boolean) {
            return (boolean) value;
        } else {
            return value != nilObject && value != falseObject;
        }
    }

    public RubyException runtimeError(String message, Node currentNode) {
        CompilerAsserts.neverPartOfCompilation();
        return new RubyException(runtimeErrorClass, context.makeString(String.format("%s", message)), RubyCallStack.getBacktrace(currentNode));
    }

    public RubyException frozenError(String className, Node currentNode) {
        CompilerAsserts.neverPartOfCompilation();
        return runtimeError(String.format("FrozenError: can't modify frozen %s \n %s", className), currentNode);
    }

    public RubyException argumentError(String message, Node currentNode) {
        CompilerAsserts.neverPartOfCompilation();
        return new RubyException(argumentErrorClass, context.makeString(String.format("%s", message)), RubyCallStack.getBacktrace(currentNode));
    }

    public RubyException argumentError(int passed, int required, Node currentNode) {
        CompilerAsserts.neverPartOfCompilation();
        return argumentError(String.format("wrong number of arguments (%d for %d)", passed, required), currentNode);
    }

    public RubyException argumentError(int passed, int required, int optional, Node currentNode) {
        CompilerAsserts.neverPartOfCompilation();
        return argumentError(String.format("wrong number of arguments (%d for %d..%d)", passed, required, required+optional), currentNode);
    }

    public RubyException localJumpError(String message, Node currentNode) {
        CompilerAsserts.neverPartOfCompilation();
        return new RubyException(localJumpErrorClass, context.makeString(message), RubyCallStack.getBacktrace(currentNode));
    }

    public RubyException unexpectedReturn(Node currentNode) {
        CompilerAsserts.neverPartOfCompilation();
        return localJumpError("unexpected return", currentNode);
    }

    public RubyException noBlockToYieldTo(Node currentNode) {
        CompilerAsserts.neverPartOfCompilation();
        return localJumpError("no block given (yield)", currentNode);
    }

    public RubyException typeError(String message, Node currentNode) {
        CompilerAsserts.neverPartOfCompilation();
        return new RubyException(typeErrorClass, context.makeString(message), RubyCallStack.getBacktrace(currentNode));
    }

    public RubyException typeErrorCantDefineSingleton(Node currentNode) {
        CompilerAsserts.neverPartOfCompilation();
        return typeError("can't define singleton", currentNode);
    }

    public RubyException typeErrorShouldReturn(String object, String method, String expectedType, Node currentNode) {
        CompilerAsserts.neverPartOfCompilation();
        return typeError(String.format("%s#%s should return %s", object, method, expectedType), currentNode);
    }

    public RubyException typeErrorCantConvertTo(String from, String to, Node currentNode) {
        CompilerAsserts.neverPartOfCompilation();
        return typeError(String.format("can't convert %s to %s", from, to), currentNode);
    }

    public RubyException typeErrorCantConvertInto(String from, String to, Node currentNode) {
        CompilerAsserts.neverPartOfCompilation();
        return typeError(String.format("can't convert %s into %s", from, to), currentNode);
    }

    public RubyException typeErrorCantConvertInto(Object from, RubyClass to, Node currentNode) {
        CompilerAsserts.neverPartOfCompilation();
        return typeErrorCantConvertInto(box(from).getLogicalClass().getName(), to.getName(), currentNode);
    }

    public RubyException typeErrorIsNotA(String value, String expectedType, Node currentNode) {
        CompilerAsserts.neverPartOfCompilation();
        return typeError(String.format("%s is not a %s", value, expectedType), currentNode);
    }

    public RubyException nameError(String message, Node currentNode) {
        CompilerAsserts.neverPartOfCompilation();
        return new RubyException(nameErrorClass, context.makeString(message), RubyCallStack.getBacktrace(currentNode));
    }

    public RubyException nameErrorUninitializedConstant(RubyModule module, String name, Node currentNode) {
        CompilerAsserts.neverPartOfCompilation();
        return nameError(String.format("uninitialized constant %s::%s", module.getName(), name), currentNode);
    }

    public RubyException nameErrorPrivateConstant(RubyModule module, String name, Node currentNode) {
        CompilerAsserts.neverPartOfCompilation();
        return nameError(String.format("private constant %s::%s referenced", module.getName(), name), currentNode);
    }

    public RubyException nameErrorInstanceNameNotAllowable(String name, Node currentNode) {
        CompilerAsserts.neverPartOfCompilation();
        return nameError(String.format("`%s' is not allowable as an instance variable name", name), currentNode);
    }

    public RubyException nameErrorReadOnly(String name, Node currentNode) {
        CompilerAsserts.neverPartOfCompilation();
        return nameError(String.format("%s is a read-only variable", name), currentNode);
    }

    public RubyException noMethodError(String message, Node currentNode) {
        CompilerAsserts.neverPartOfCompilation();
        return new RubyException(context.getCoreLibrary().getNoMethodErrorClass(), context.makeString(message), RubyCallStack.getBacktrace(currentNode));
    }

    public RubyException noMethodError(String name, String object, Node currentNode) {
        CompilerAsserts.neverPartOfCompilation();
        return noMethodError(String.format("undefined method `%s' for %s", name, object), currentNode);
    }

    public RubyException privateMethodError(String name, String object, Node currentNode) {
        CompilerAsserts.neverPartOfCompilation();
        return noMethodError(String.format("private method `%s' called for %s", name, object), currentNode);
    }

    public RubyException loadError(String message, Node currentNode) {
        CompilerAsserts.neverPartOfCompilation();
        return new RubyException(context.getCoreLibrary().getLoadErrorClass(), context.makeString(message), RubyCallStack.getBacktrace(currentNode));
    }

    public RubyException loadErrorCannotLoad(String name, Node currentNode) {
        CompilerAsserts.neverPartOfCompilation();
        return loadError(String.format("cannot load such file -- %s", name), currentNode);
    }

    public RubyException zeroDivisionError(Node currentNode) {
        CompilerAsserts.neverPartOfCompilation();
        return new RubyException(context.getCoreLibrary().getZeroDivisionErrorClass(), context.makeString("divided by 0"), RubyCallStack.getBacktrace(currentNode));
    }

    public RubyException syntaxError(String message, Node currentNode) {
        CompilerAsserts.neverPartOfCompilation();
        return new RubyException(syntaxErrorClass, context.makeString(message), RubyCallStack.getBacktrace(currentNode));
    }

    public RubyException mathDomainError(String method, Node currentNode) {
        CompilerAsserts.neverPartOfCompilation();
        return new RubyException(edomClass, context.makeString(String.format("Numerical argument is out of domain - \"%s\"", method)), RubyCallStack.getBacktrace(currentNode));
    }

    public RubyBasicObject rangeError(String type, String value, String range, Node currentNode) {
        CompilerAsserts.neverPartOfCompilation();
        return new RubyException(rangeErrorClass, context.makeString(String.format("%s %s out of range of %s", type, value, range)), RubyCallStack.getBacktrace(currentNode));
    }

    public RubyException internalError(String message, Node currentNode) {
        CompilerAsserts.neverPartOfCompilation();
        return new RubyException(context.getCoreLibrary().getRubyTruffleErrorClass(), context.makeString("internal implementation error - " + message), RubyCallStack.getBacktrace(currentNode));
    }

    public RubyContext getContext() {
        return context;
    }

    public RubyClass getArrayClass() {
        return arrayClass;
    }

    public RubyClass getBignumClass() {
        return bignumClass;
    }

    public RubyClass getBindingClass() {
        return bindingClass;
    }

    public RubyClass getClassClass() {
        return classClass;
    }

    public RubyClass getContinuationClass() {
        return continuationClass;
    }

    public RubyClass getExceptionClass() { return exceptionClass; }

    public RubyClass getFalseClass() {
        return falseClass;
    }

    public RubyClass getFiberClass() {
        return fiberClass;
    }

    public RubyClass getFileClass() {
        return fileClass;
    }

    public RubyClass getFixnumClass() {
        return fixnumClass;
    }

    public RubyClass getFloatClass() {
        return floatClass;
    }

    public RubyClass getHashClass() {
        return hashClass;
    }

    public RubyClass getLoadErrorClass() {
        return loadErrorClass;
    }

    public RubyClass getMatchDataClass() {
        return matchDataClass;
    }

    public RubyClass getModuleClass() {
        return moduleClass;
    }

    public RubyClass getNameErrorClass() {
        return nameErrorClass;
    }

    public RubyClass getNilClass() {
        return nilClass;
    }

    public RubyClass getNoMethodErrorClass() {
        return noMethodErrorClass;
    }

    public RubyClass getObjectClass() {
        return objectClass;
    }

    public RubyClass getProcClass() {
        return procClass;
    }

    public RubyClass getRangeClass() {
        return rangeClass;
    }

    public RubyClass getRegexpClass() {
        return regexpClass;
    }

    public RubyClass getRubyTruffleErrorClass() {
        return rubyTruffleErrorClass;
    }

    public RubyClass getRuntimeErrorClass() {
        return runtimeErrorClass;
    }

    public RubyClass getStringClass() {
        return stringClass;
    }

    public RubyClass getEncodingClass(){ return encodingClass; }

    public RubyClass getSymbolClass() {
        return symbolClass;
    }

    public RubyClass getSyntaxErrorClass() {
        return syntaxErrorClass;
    }

    public RubyClass getThreadClass() {
        return threadClass;
    }

    public RubyClass getTimeClass() {
        return timeClass;
    }

    public RubyClass getTrueClass() {
        return trueClass;
    }

    public RubyClass getZeroDivisionErrorClass() {
        return zeroDivisionErrorClass;
    }

    public RubyModule getKernelModule() {
        return kernelModule;
    }

    public RubyArray getArgv() {
        return argv;
    }

    public RubyBasicObject getGlobalVariablesObject() {
        return globalVariablesObject;
    }

    public RubyArray getLoadPath() {
        return (RubyArray) globalVariablesObject.getInstanceVariable("$LOAD_PATH");
    }

    public RubyArray getLoadedFeatures() {
        return (RubyArray) globalVariablesObject.getInstanceVariable("$LOADED_FEATURES");
    }

    public RubyBasicObject getMainObject() {
        return mainObject;
    }

    public RubyTrueClass getTrueObject() {
        return trueObject;
    }

    public RubyFalseClass getFalseObject() {
        return falseObject;
    }

    public RubyNilClass getNilObject() {
        return nilObject;
    }

    public RubyHash getENV() {
        return envHash;
    }

    public RubyEncoding getDefaultEncoding() { return RubyEncoding.getEncoding(context, "US-ASCII"); }

    private RubyHash getSystemEnv() {
        final LinkedHashMap<Object, Object> storage = new LinkedHashMap<>();

        for (Map.Entry<String, String> variable : System.getenv().entrySet()) {
            storage.put(context.makeString(variable.getKey()), context.makeString(variable.getValue()));
        }

        return new RubyHash(context.getCoreLibrary().getHashClass(), null, null, storage, 0);
    }

    public ArrayNodes.MinBlock getArrayMinBlock() {
        return arrayMinBlock;
    }

    public ArrayNodes.MaxBlock getArrayMaxBlock() {
        return arrayMaxBlock;
    }

    public RubyClass getNumericClass() {
        return numericClass;
    }

    public RubyClass getIntegerClass() {
        return integerClass;
    }

    public RubiniusLibrary getRubiniusLibrary() {
        return rubiniusLibrary;
    }

    public RubyClass getArgumentErrorClass() {
        return argumentErrorClass;
    }

    public RubyClass getEncodingConverterClass() {
        return encodingConverterClass;
    }
}
