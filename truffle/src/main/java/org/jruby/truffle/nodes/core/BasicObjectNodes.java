/*
 * Copyright (c) 2013, 2015 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.nodes.core;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.dsl.CreateCast;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.NodeUtil;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.source.SourceSection;
import org.jruby.runtime.Visibility;
import org.jruby.truffle.nodes.RubyGuards;
import org.jruby.truffle.nodes.RubyNode;
import org.jruby.truffle.nodes.cast.BooleanCastNodeGen;
import org.jruby.truffle.nodes.dispatch.CallDispatchHeadNode;
import org.jruby.truffle.nodes.dispatch.DispatchHeadNodeFactory;
import org.jruby.truffle.nodes.dispatch.MissingBehavior;
import org.jruby.truffle.nodes.dispatch.RubyCallNode;
import org.jruby.truffle.nodes.methods.UnsupportedOperationBehavior;
import org.jruby.truffle.nodes.objects.AllocateObjectNode;
import org.jruby.truffle.nodes.objects.AllocateObjectNodeGen;
import org.jruby.truffle.nodes.yield.YieldDispatchHeadNode;
import org.jruby.truffle.runtime.NotProvided;
import org.jruby.truffle.runtime.RubyContext;
import org.jruby.truffle.runtime.control.RaiseException;
import org.jruby.truffle.runtime.layouts.Layouts;

@CoreClass(name = "BasicObject")
public abstract class BasicObjectNodes {

    @CoreMethod(names = "!")
    public abstract static class NotNode extends UnaryCoreMethodNode {

        public NotNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        @CreateCast("operand") public RubyNode createCast(RubyNode operand) {
            return BooleanCastNodeGen.create(getContext(), getSourceSection(), operand);
        }

        @Specialization
        public boolean not(boolean value) {
            return !value;
        }

    }

    @CoreMethod(names = "!=", required = 1)
    public abstract static class NotEqualNode extends CoreMethodArrayArgumentsNode {

        @Child private CallDispatchHeadNode equalNode;

        public NotEqualNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
            equalNode = DispatchHeadNodeFactory.createMethodCall(context);
        }

        @Specialization
        public boolean equal(VirtualFrame frame, Object a, Object b) {
            return !equalNode.callBoolean(frame, a, "==", null, b);
        }

    }

    @CoreMethod(names = { "equal?", "==" }, required = 1)
    public abstract static class ReferenceEqualNode extends BinaryCoreMethodNode {

        public ReferenceEqualNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public abstract boolean executeReferenceEqual(VirtualFrame frame, Object a, Object b);

        @Specialization public boolean equal(boolean a, boolean b) { return a == b; }
        @Specialization public boolean equal(int a, int b) { return a == b; }
        @Specialization public boolean equal(long a, long b) { return a == b; }
        @Specialization public boolean equal(double a, double b) { return Double.doubleToRawLongBits(a) == Double.doubleToRawLongBits(b); }

        @Specialization public boolean equal(DynamicObject a, DynamicObject b) {
            return a == b;
        }

        @Specialization(guards = {"isNotDynamicObject(a)", "isNotDynamicObject(b)", "notSameClass(a, b)"})
        public boolean equal(Object a, Object b) {
            return false;
        }

        @Specialization(guards = "isNotDynamicObject(a)")
        public boolean equal(Object a, DynamicObject b) {
            return false;
        }

        @Specialization(guards = "isNotDynamicObject(b)")
        public boolean equal(DynamicObject a, Object b) {
            return false;
        }

        protected boolean isNotDynamicObject(Object value) {
            return !(value instanceof DynamicObject);
        }

        protected boolean notSameClass(Object a, Object b) {
            return a.getClass() != b.getClass();
        }

    }

    @CoreMethod(names = "initialize", needsSelf = false)
    public abstract static class InitializeNode extends CoreMethodArrayArgumentsNode {

        public InitializeNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        @Specialization
        public DynamicObject initialize() {
            return nil();
        }

    }

    @CoreMethod(names = "instance_eval", needsBlock = true, optional = 1, unsupportedOperationBehavior = UnsupportedOperationBehavior.ARGUMENT_ERROR)
    public abstract static class InstanceEvalNode extends CoreMethodArrayArgumentsNode {

        @Child private YieldDispatchHeadNode yield;

        public InstanceEvalNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
            yield = new YieldDispatchHeadNode(context);
        }

        @CompilerDirectives.TruffleBoundary
        @Specialization(guards = "isRubyString(string)")
        public Object instanceEval(Object receiver, DynamicObject string, NotProvided block) {
            return getContext().instanceEval(Layouts.STRING.getByteList(string), receiver, this);
        }

        @Specialization(guards = "isRubyProc(block)")
        public Object instanceEval(VirtualFrame frame, Object receiver, NotProvided string, DynamicObject block) {
            return yield.dispatchWithModifiedSelf(frame, block, receiver, receiver);
        }

    }

    @CoreMethod(names = "instance_exec", needsBlock = true, rest = true)
    public abstract static class InstanceExecNode extends YieldingCoreMethodNode {

        public InstanceExecNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        @Specialization(guards = "isRubyProc(block)")
        public Object instanceExec(VirtualFrame frame, Object receiver, Object[] arguments, DynamicObject block) {
            CompilerDirectives.transferToInterpreter();

            return yieldWithModifiedSelf(frame, block, receiver, arguments);
        }

        @Specialization
        public Object instanceExec(Object receiver, Object[] arguments, NotProvided block) {
            CompilerDirectives.transferToInterpreter();

            throw new RaiseException(getContext().getCoreLibrary().localJumpError("no block given", this));
        }

    }

    @CoreMethod(names = "method_missing", needsBlock = true, rest = true, optional = 1, visibility = Visibility.PRIVATE)
    public abstract static class MethodMissingNode extends CoreMethodArrayArgumentsNode {

        public MethodMissingNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        @Specialization
        public Object methodMissingNoName(Object self, NotProvided name, Object[] args, NotProvided block) {
            CompilerDirectives.transferToInterpreter();
            throw new RaiseException(getContext().getCoreLibrary().argumentError("no id given", this));
        }

        @Specialization
        public Object methodMissingNoBlock(Object self, DynamicObject name, Object[] args, NotProvided block) {
            return methodMissingBlock(self, name, args, (DynamicObject) null);
        }

        @Specialization(guards = "isRubyProc(block)")
        public Object methodMissingBlock(Object self, DynamicObject name, Object[] args, DynamicObject block) {
            return methodMissing(self, name, args, block);
        }

        private Object methodMissing(Object self, DynamicObject name, Object[] args, DynamicObject block) {
            CompilerDirectives.transferToInterpreter();

            assert block == null || RubyGuards.isRubyProc(block);

            // TODO: should not be a call to Java toString(), but rather sth like name_err_mesg_to_str() in MRI error.c
            if (lastCallWasVCall()) {
                throw new RaiseException(
                        getContext().getCoreLibrary().nameErrorUndefinedLocalVariableOrMethod(
                                Layouts.SYMBOL.getString(name),
                                Layouts.MODULE.getFields(getContext().getCoreLibrary().getLogicalClass(self)).getName(),
                                this));
            } else {
                throw new RaiseException(getContext().getCoreLibrary().noMethodErrorOnReceiver(Layouts.SYMBOL.getString(name), self, this));
            }
        }

        private boolean lastCallWasVCall() {
            final RubyCallNode callNode = NodeUtil.findParent(Truffle.getRuntime().getCallerFrame().getCallNode(), RubyCallNode.class);

            if (callNode == null) {
                return false;
            }

            return callNode.isVCall();

        }

    }

    @CoreMethod(names = "__send__", needsBlock = true, rest = true, required = 1)
    public abstract static class SendNode extends CoreMethodArrayArgumentsNode {

        @Child private CallDispatchHeadNode dispatchNode;

        public SendNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);

            dispatchNode = new CallDispatchHeadNode(context, true,
                    MissingBehavior.CALL_METHOD_MISSING);
        }

        @Specialization
        public Object send(VirtualFrame frame, Object self, Object name, Object[] args, NotProvided block) {
            return send(frame, self, name, args, (DynamicObject) null);
        }

        @Specialization(guards = "isRubyProc(block)")
        public Object send(VirtualFrame frame, Object self, Object name, Object[] args, DynamicObject block) {
            return dispatchNode.call(frame, self, name, block, args);
        }

    }

    @CoreMethod(names = "allocate", constructor = true)
    public abstract static class AllocateNode extends CoreMethodArrayArgumentsNode {

        @Child private AllocateObjectNode allocateObjectNode;

        public AllocateNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
            allocateObjectNode = AllocateObjectNodeGen.create(context, sourceSection, null, null);
        }

        @Specialization
        public DynamicObject allocate(DynamicObject rubyClass) {
            return allocateObjectNode.allocate(rubyClass);
        }

    }

}
