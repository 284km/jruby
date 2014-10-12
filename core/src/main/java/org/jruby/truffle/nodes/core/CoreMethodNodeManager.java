/*
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.nodes.core;

import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.dsl.GeneratedBy;
import com.oracle.truffle.api.dsl.NodeFactory;
import org.jruby.runtime.Visibility;
import org.jruby.truffle.nodes.CoreSourceSection;
import org.jruby.truffle.nodes.RubyNode;
import org.jruby.truffle.nodes.RubyRootNode;
import org.jruby.truffle.nodes.control.SequenceNode;
import org.jruby.truffle.nodes.methods.ExceptionTranslatingNode;
import org.jruby.truffle.nodes.methods.arguments.*;
import org.jruby.truffle.nodes.objects.SelfNode;
import org.jruby.truffle.runtime.ModuleOperations;
import org.jruby.truffle.runtime.util.ArrayUtils;
import org.jruby.truffle.runtime.RubyContext;
import org.jruby.truffle.runtime.UndefinedPlaceholder;
import org.jruby.truffle.runtime.core.RubyClass;
import org.jruby.truffle.runtime.core.RubyModule;
import org.jruby.truffle.runtime.methods.Arity;
import org.jruby.truffle.runtime.methods.RubyMethod;
import org.jruby.truffle.runtime.methods.SharedMethodInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class CoreMethodNodeManager {

    public static void addStandardMethods(RubyClass rubyObjectClass) {
        for (MethodDetails methodDetails : getMethods()) {
            addMethod(rubyObjectClass, methodDetails);
        }
    }

    public static void addExtensionMethods(RubyClass rubyObjectClass, List<? extends NodeFactory<? extends CoreMethodNode>> nodeFactories) {
        final List<MethodDetails> methods = new ArrayList<>();
        getMethods(methods, nodeFactories);

        for (MethodDetails methodDetails : methods) {
            addMethod(rubyObjectClass, methodDetails);
        }
    }

    /**
     * Collect up all the core method nodes. Abstracted to allow the SVM to implement at compile
     * type.
     */
    public static List<MethodDetails> getMethods() {
        final List<MethodDetails> methods = new ArrayList<>();
        getMethods(methods, ArrayNodesFactory.getFactories());
        getMethods(methods, BasicObjectNodesFactory.getFactories());
        getMethods(methods, BindingNodesFactory.getFactories());
        getMethods(methods, BignumNodesFactory.getFactories());
        getMethods(methods, ClassNodesFactory.getFactories());
        getMethods(methods, ContinuationNodesFactory.getFactories());
        getMethods(methods, ComparableNodesFactory.getFactories());
        getMethods(methods, DirNodesFactory.getFactories());
        getMethods(methods, ExceptionNodesFactory.getFactories());
        getMethods(methods, FalseClassNodesFactory.getFactories());
        getMethods(methods, FiberNodesFactory.getFactories());
        getMethods(methods, FileNodesFactory.getFactories());
        getMethods(methods, FixnumNodesFactory.getFactories());
        getMethods(methods, FloatNodesFactory.getFactories());
        getMethods(methods, HashNodesFactory.getFactories());
        getMethods(methods, IONodesFactory.getFactories());
        getMethods(methods, KernelNodesFactory.getFactories());
        getMethods(methods, MainNodesFactory.getFactories());
        getMethods(methods, MatchDataNodesFactory.getFactories());
        getMethods(methods, MathNodesFactory.getFactories());
        getMethods(methods, ModuleNodesFactory.getFactories());
        getMethods(methods, NilClassNodesFactory.getFactories());
        getMethods(methods, ObjectSpaceNodesFactory.getFactories());
        getMethods(methods, ProcessNodesFactory.getFactories());
        getMethods(methods, ProcNodesFactory.getFactories());
        getMethods(methods, RangeNodesFactory.getFactories());
        getMethods(methods, RegexpNodesFactory.getFactories());
        getMethods(methods, SignalNodesFactory.getFactories());
        getMethods(methods, StringNodesFactory.getFactories());
        getMethods(methods, SymbolNodesFactory.getFactories());
        getMethods(methods, ThreadNodesFactory.getFactories());
        getMethods(methods, TimeNodesFactory.getFactories());
        getMethods(methods, TrueClassNodesFactory.getFactories());
        getMethods(methods, TruffleDebugNodesFactory.getFactories());
        getMethods(methods, EncodingNodesFactory.getFactories());
        return methods;
    }

    /**
     * Collect up the core methods created by a factory.
     */
    private static void getMethods(List<MethodDetails> methods, List<? extends NodeFactory<? extends CoreMethodNode>> nodeFactories) {
        for (NodeFactory<? extends RubyNode> nodeFactory : nodeFactories) {
            final GeneratedBy generatedBy = nodeFactory.getClass().getAnnotation(GeneratedBy.class);
            final Class<?> nodeClass = generatedBy.value();
            final CoreClass classAnnotation = nodeClass.getEnclosingClass().getAnnotation(CoreClass.class);
            final CoreMethod methodAnnotation = nodeClass.getAnnotation(CoreMethod.class);

            if (methodAnnotation != null) {
                methods.add(new MethodDetails(classAnnotation, methodAnnotation, nodeFactory));
            }
        }
    }

    private static void addMethods(RubyClass rubyObjectClass, List<MethodDetails> methods) {
        for (MethodDetails methodDetails : methods) {
            addMethod(rubyObjectClass, methodDetails);
        }
    }

    private static void addMethod(RubyClass rubyObjectClass, MethodDetails methodDetails) {
        assert rubyObjectClass != null;
        assert methodDetails != null;

        final RubyContext context = rubyObjectClass.getContext();

        RubyModule module;

        if (methodDetails.getClassAnnotation().name().equals("main")) {
            module = context.getCoreLibrary().getMainObject().getSingletonClass(null);
        } else {
            module = (RubyModule) ModuleOperations.lookupConstant(rubyObjectClass, methodDetails.getClassAnnotation().name()).getValue();
        }

        assert module != null : methodDetails.getClassAnnotation().name();

        final CoreMethod anno = methodDetails.getMethodAnnotation();

        final List<String> names = Arrays.asList(anno.names());
        assert names.size() >= 1;

        final String canonicalName = names.get(0);
        final List<String> aliases = names.subList(1, names.size());

        final Visibility visibility = anno.visibility();

        if (anno.isModuleFunction() && visibility != Visibility.PUBLIC) {
            System.err.println("WARNING: visibility ignored when isModuleFunction in " + methodDetails.getIndicativeName());
        }

        // Do not use needsSelf=true in module functions, it is either the module/class or the instance.
        final boolean needsSelf = !anno.isModuleFunction() && anno.needsSelf();

        final RubyRootNode rootNode = makeGenericMethod(context, methodDetails, needsSelf);

        final RubyMethod method = new RubyMethod(rootNode.getSharedMethodInfo(), canonicalName, module, visibility, false,
                Truffle.getRuntime().createCallTarget(rootNode), null);

        if (anno.isModuleFunction()) {
            module.addMethod(null, method.withNewVisibility(Visibility.PRIVATE));
            module.getSingletonClass(null).addMethod(null, method.withNewVisibility(Visibility.PUBLIC));
        } else {
            module.addMethod(null, method);
        }

        for (String alias : aliases) {
            final RubyMethod withAlias = method.withNewName(alias);

            module.addMethod(null, withAlias);

            if (anno.isModuleFunction()) {
                module.getSingletonClass(null).addMethod(null, withAlias.withNewVisibility(Visibility.PUBLIC));
            }
        }
    }

    private static RubyRootNode makeGenericMethod(RubyContext context, MethodDetails methodDetails, boolean needsSelf) {
        final CoreSourceSection sourceSection = new CoreSourceSection(methodDetails.getClassAnnotation().name(), methodDetails.getMethodAnnotation().names()[0]);

        final SharedMethodInfo sharedMethodInfo = new SharedMethodInfo(sourceSection, methodDetails.getIndicativeName(), false, null);

        final Arity arity = new Arity(methodDetails.getMethodAnnotation().minArgs(), methodDetails.getMethodAnnotation().maxArgs());

        final List<RubyNode> argumentsNodes = new ArrayList<>();

        if (needsSelf) {
            RubyNode readSelfNode = new SelfNode(context, sourceSection);

            if (methodDetails.getMethodAnnotation().lowerFixnumSelf()) {
                readSelfNode = new FixnumLowerNode(readSelfNode);
            }

            argumentsNodes.add(readSelfNode);
        }

        if (methodDetails.getMethodAnnotation().isSplatted()) {
            argumentsNodes.add(new ReadAllArgumentsNode(context, sourceSection));
        } else {
            if (arity.getMaximum() == Arity.NO_MAXIMUM) {
                throw new UnsupportedOperationException("if a core method isn't splatted, you need to specify a maximum");
            }

            for (int n = 0; n < arity.getMaximum(); n++) {
                RubyNode readArgumentNode = new ReadPreArgumentNode(context, sourceSection, n, MissingArgumentBehaviour.UNDEFINED);

                if (ArrayUtils.contains(methodDetails.getMethodAnnotation().lowerFixnumParameters(), n)) {
                    readArgumentNode = new FixnumLowerNode(readArgumentNode);
                }

                argumentsNodes.add(readArgumentNode);
            }
        }

        if (methodDetails.getMethodAnnotation().needsBlock()) {
            argumentsNodes.add(new ReadBlockNode(context, sourceSection, UndefinedPlaceholder.INSTANCE));
        }

        final RubyNode methodNode = methodDetails.getNodeFactory().createNode(context, sourceSection, argumentsNodes.toArray(new RubyNode[argumentsNodes.size()]));
        final CheckArityNode checkArity = new CheckArityNode(context, sourceSection, arity);
        final RubyNode block = SequenceNode.sequence(context, sourceSection, checkArity, methodNode);
        final ExceptionTranslatingNode exceptionTranslatingNode = new ExceptionTranslatingNode(context, sourceSection, block);

        return new RubyRootNode(context, sourceSection, null, sharedMethodInfo, exceptionTranslatingNode);
    }

    public static class MethodDetails {

        private final CoreClass classAnnotation;
        private final CoreMethod methodAnnotation;
        private final NodeFactory<? extends RubyNode> nodeFactory;

        public MethodDetails(CoreClass classAnnotation, CoreMethod methodAnnotation, NodeFactory<? extends RubyNode> nodeFactory) {
            assert classAnnotation != null;
            assert methodAnnotation != null;
            assert nodeFactory != null;
            this.classAnnotation = classAnnotation;
            this.methodAnnotation = methodAnnotation;
            this.nodeFactory = nodeFactory;
        }

        public CoreClass getClassAnnotation() {
            return classAnnotation;
        }

        public CoreMethod getMethodAnnotation() {
            return methodAnnotation;
        }

        public NodeFactory<? extends RubyNode> getNodeFactory() {
            return nodeFactory;
        }

        public String getIndicativeName() {
            return classAnnotation.name() + "#" + methodAnnotation.names()[0] + "(core)";
        }
    }

}
