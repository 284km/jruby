/*
 * Copyright (c) 2015 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.nodes.core;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.*;
import com.oracle.truffle.api.source.NullSourceSection;
import com.oracle.truffle.api.source.SourceSection;
import org.jruby.ast.ArgsNode;
import org.jruby.runtime.ArgumentDescriptor;
import org.jruby.runtime.Helpers;
import org.jruby.runtime.Visibility;
import org.jruby.truffle.nodes.RubyGuards;
import org.jruby.truffle.nodes.core.array.ArrayNodes;
import org.jruby.truffle.nodes.methods.CanBindMethodToModuleNode;
import org.jruby.truffle.nodes.methods.CanBindMethodToModuleNodeGen;
import org.jruby.truffle.nodes.objects.MetaClassNode;
import org.jruby.truffle.nodes.objects.MetaClassNodeGen;
import org.jruby.truffle.runtime.RubyContext;
import org.jruby.truffle.runtime.control.RaiseException;
import com.oracle.truffle.api.object.DynamicObject;
import org.jruby.truffle.runtime.methods.InternalMethod;

@CoreClass(name = "UnboundMethod")
public abstract class UnboundMethodNodes {

    @org.jruby.truffle.om.dsl.api.Layout
    public interface UnboundMethodLayout extends BasicObjectNodes.BasicObjectLayout {

        DynamicObjectFactory createUnboundMethodShape(DynamicObject logicalClass, DynamicObject metaClass);

        DynamicObject createUnboundMethod(DynamicObjectFactory factory, DynamicObject origin, InternalMethod method);

        boolean isUnboundMethod(DynamicObject object);

        DynamicObject getOrigin(DynamicObject object);
        InternalMethod getMethod(DynamicObject object);

    }

    public static final UnboundMethodLayout UNBOUND_METHOD_LAYOUT = UnboundMethodLayoutImpl.INSTANCE;

    public static DynamicObject createUnboundMethod(DynamicObject rubyClass, DynamicObject origin, InternalMethod method) {
        return UNBOUND_METHOD_LAYOUT.createUnboundMethod(ModuleNodes.getFields(rubyClass).factory, origin, method);
    }

    public static DynamicObject getOrigin(DynamicObject method) {
        return UNBOUND_METHOD_LAYOUT.getOrigin(method);
    }

    public static InternalMethod getMethod(DynamicObject method) {
        return UNBOUND_METHOD_LAYOUT.getMethod(method);
    }

    @CoreMethod(names = "==", required = 1)
    public abstract static class EqualNode extends CoreMethodArrayArgumentsNode {

        public EqualNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        @Specialization(guards = "isRubyUnboundMethod(other)")
        boolean equal(DynamicObject self, DynamicObject other) {
            return getMethod(self) == getMethod(other) && getOrigin(self) == getOrigin(other);
        }

        @Specialization(guards = "!isRubyUnboundMethod(other)")
        boolean equal(DynamicObject self, Object other) {
            return false;
        }

    }

    @CoreMethod(names = "arity")
    public abstract static class ArityNode extends CoreMethodArrayArgumentsNode {

        public ArityNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        @Specialization
        public int arity(DynamicObject method) {
            return getMethod(method).getSharedMethodInfo().getArity().getArityNumber();
        }

    }

    @CoreMethod(names = "bind", required = 1)
    public abstract static class BindNode extends CoreMethodArrayArgumentsNode {

        @Child private MetaClassNode metaClassNode;
        @Child private CanBindMethodToModuleNode canBindMethodToModuleNode;

        public BindNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
            metaClassNode = MetaClassNodeGen.create(context, sourceSection, null);
            canBindMethodToModuleNode = CanBindMethodToModuleNodeGen.create(context, sourceSection, null, null);
        }

        @Specialization
        public DynamicObject bind(VirtualFrame frame, DynamicObject unboundMethod, Object object) {
            final DynamicObject objectMetaClass = metaClass(frame, object);

            if (!canBindMethodToModuleNode.executeCanBindMethodToModule(getMethod(unboundMethod), objectMetaClass)) {
                CompilerDirectives.transferToInterpreter();
                final DynamicObject declaringModule = getMethod(unboundMethod).getDeclaringModule();
                if (RubyGuards.isRubyClass(declaringModule) && ClassNodes.isSingleton(declaringModule)) {
                    throw new RaiseException(getContext().getCoreLibrary().typeError(
                            "singleton method called for a different object", this));
                } else {
                    throw new RaiseException(getContext().getCoreLibrary().typeError(
                            "bind argument must be an instance of " + ModuleNodes.getFields(declaringModule).getName(), this));
                }
            }

            return MethodNodes.createMethod(getContext().getCoreLibrary().getMethodClass(), object, getMethod(unboundMethod));
        }

        protected DynamicObject metaClass(VirtualFrame frame, Object object) {
            return metaClassNode.executeMetaClass(frame, object);
        }

    }

    @CoreMethod(names = "name")
    public abstract static class NameNode extends CoreMethodArrayArgumentsNode {

        public NameNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        @Specialization
        public DynamicObject name(DynamicObject unboundMethod) {
            return getSymbol(getMethod(unboundMethod).getName());
        }

    }

    // TODO: We should have an additional method for this but we need to access it for #inspect.
    @CoreMethod(names = "origin", visibility = Visibility.PRIVATE)
    public abstract static class OriginNode extends CoreMethodArrayArgumentsNode {

        public OriginNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        @Specialization
        public DynamicObject origin(DynamicObject unboundMethod) {
            return getOrigin(unboundMethod);
        }

    }

    @CoreMethod(names = "owner")
    public abstract static class OwnerNode extends CoreMethodArrayArgumentsNode {

        public OwnerNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        @Specialization
        public DynamicObject owner(DynamicObject unboundMethod) {
            return getMethod(unboundMethod).getDeclaringModule();
        }

    }

    @CoreMethod(names = "parameters")
    public abstract static class ParametersNode extends CoreMethodArrayArgumentsNode {

        public ParametersNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        @TruffleBoundary
        @Specialization
        public DynamicObject parameters(DynamicObject method) {
            final ArgsNode argsNode = getMethod(method).getSharedMethodInfo().getParseTree().findFirstChild(ArgsNode.class);

            final ArgumentDescriptor[] argsDesc = Helpers.argsNodeToArgumentDescriptors(argsNode);

            return getContext().toTruffle(Helpers.argumentDescriptorsToParameters(getContext().getRuntime(),
                    argsDesc, true));
        }

    }

    @CoreMethod(names = "source_location")
    public abstract static class SourceLocationNode extends CoreMethodArrayArgumentsNode {

        public SourceLocationNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        @TruffleBoundary
        @Specialization
        public Object sourceLocation(DynamicObject unboundMethod) {
            SourceSection sourceSection = getMethod(unboundMethod).getSharedMethodInfo().getSourceSection();

            if (sourceSection instanceof NullSourceSection) {
                return nil();
            } else {
                DynamicObject file = createString(sourceSection.getSource().getName());
                return ArrayNodes.fromObjects(getContext().getCoreLibrary().getArrayClass(),
                        file, sourceSection.getStartLine());
            }
        }

    }

}
