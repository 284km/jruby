/*
 * Copyright (c) 2013, 2014 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.nodes.core;

import java.math.*;
import java.util.*;

import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.NodeUtil;
import com.oracle.truffle.api.source.*;
import com.oracle.truffle.api.dsl.*;
import com.oracle.truffle.api.frame.VirtualFrame;
import org.jruby.runtime.Visibility;
import org.jruby.truffle.nodes.RubyCallNode;
import org.jruby.truffle.nodes.RubyNode;
import org.jruby.truffle.nodes.cast.UnboxingNodeFactory;
import org.jruby.truffle.nodes.dispatch.Dispatch;
import org.jruby.truffle.nodes.dispatch.DispatchHeadNode;
import org.jruby.truffle.nodes.yield.YieldDispatchHeadNode;
import org.jruby.truffle.runtime.*;
import org.jruby.truffle.runtime.control.RaiseException;
import org.jruby.truffle.runtime.core.*;
import org.jruby.util.cli.Options;

@CoreClass(name = "BasicObject")
public abstract class BasicObjectNodes {

    @CoreMethod(names = "!")
    public abstract static class NotNode extends CoreMethodNode {

        public NotNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public NotNode(NotNode prev) {
            super(prev);
        }

        @Specialization
        public boolean not(Object value) {
            return !getContext().getCoreLibrary().isTruthy(value);
        }

    }

    @CoreMethod(names = "!=", required = 1)
    public abstract static class NotEqualNode extends CoreMethodNode {

        @Child protected DispatchHeadNode equalNode;

        public NotEqualNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
            equalNode = new DispatchHeadNode(context);
        }

        public NotEqualNode(NotEqualNode prev) {
            super(prev);
            equalNode = prev.equalNode;
        }

        @Specialization
        public boolean equal(VirtualFrame frame, Object a, Object b) {
            return !equalNode.callIsTruthy(frame, a, "==", null, b);
        }

    }

    @CoreMethod(names = "__id__", needsSelf = true)
    public abstract static class IDNode extends CoreMethodNode {

        public IDNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public IDNode(IDNode prev) {
            super(prev);
        }

        @Specialization
        public long id(RubyBasicObject object) {
            notDesignedForCompilation();

            return object.getObjectID();
        }

    }

    @CoreMethod(names = {"equal?", "=="}, required = 1)
    public abstract static class ReferenceEqualNode extends CoreMethodNode {

        public ReferenceEqualNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public ReferenceEqualNode(ReferenceEqualNode prev) {
            super(prev);
        }

        // The @CreateCast is not applied when using this, so the caller needs to unbox itself.
        protected abstract boolean executeEqualWithUnboxed(VirtualFrame frame, Object a, Object b);

        @CreateCast("arguments") public RubyNode[] createCast(RubyNode[] arguments) {
            return new RubyNode[]{
                UnboxingNodeFactory.create(getContext(), getSourceSection(), arguments[0]),
                UnboxingNodeFactory.create(getContext(), getSourceSection(), arguments[1])
            };
        }

        @Specialization public boolean equal(boolean a, boolean b) { return a == b; }
        @Specialization public boolean equal(int a, int b) { return a == b; }
        @Specialization public boolean equal(long a, long b) { return a == b; }
        @Specialization public boolean equal(double a, double b) { return a == b; }
        @Specialization public boolean equal(BigInteger a, BigInteger b) { return a == b; } // On purpose, Bignum are not #equal?

        @Specialization public boolean equal(RubyBasicObject a, RubyBasicObject b) {
            assert !(a instanceof Unboxable) && !(b instanceof Unboxable);
            return a == b;
        }

        @Specialization(guards = {"isNotRubyBasicObject(arguments[0])", "isNotRubyBasicObject(arguments[1])", "notSameClass"})
        public boolean equal(Object a, Object b) {
            assert !(a instanceof Unboxable) && !(b instanceof Unboxable);
            return false;
        }

        @Specialization(guards = "isNotRubyBasicObject(arguments[0])")
        public boolean equal(Object a, RubyBasicObject b) {
            assert !(b instanceof Unboxable);
            return false;
        }

        @Specialization(guards = "isNotRubyBasicObject(arguments[1])")
        public boolean equal(RubyBasicObject a, Object b) {
            assert !(a instanceof Unboxable);
            return false;
        }

        protected boolean isNotRubyBasicObject(Object value) {
            return !(value instanceof RubyBasicObject);
        }

        protected boolean notSameClass(Object a, Object b) {
            return a.getClass() != b.getClass();
        }

    }

    @CoreMethod(names = "initialize", needsSelf = false, visibility = Visibility.PRIVATE)
    public abstract static class InitializeNode extends CoreMethodNode {

        public InitializeNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public InitializeNode(InitializeNode prev) {
            super(prev);
        }

        @Specialization
        public RubyNilClass initialize() {
            return getContext().getCoreLibrary().getNilObject();
        }

    }

    @CoreMethod(names = "instance_eval", needsBlock = true, optional = 1)
    public abstract static class InstanceEvalNode extends CoreMethodNode {

        @Child protected YieldDispatchHeadNode yield;

        public InstanceEvalNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
            yield = new YieldDispatchHeadNode(context);
        }

        public InstanceEvalNode(InstanceEvalNode prev) {
            super(prev);
            yield = prev.yield;
        }

        @Specialization
        public Object instanceEval(VirtualFrame frame, RubyBasicObject receiver, RubyString string, UndefinedPlaceholder block) {
            notDesignedForCompilation();

            return getContext().eval(string.toString(), receiver, this);
        }

        @Specialization
        public Object instanceEval(VirtualFrame frame, RubyBasicObject receiver, UndefinedPlaceholder string, RubyProc block) {
            notDesignedForCompilation();

            if (receiver instanceof RubyFixnum || receiver instanceof RubySymbol) {
                throw new RaiseException(getContext().getCoreLibrary().typeError("no class to make alias", this));
            }

            return yield.dispatchWithModifiedSelf(frame, block, receiver);
        }

        @Specialization
        public Object instanceEval(VirtualFrame frame, Object self, UndefinedPlaceholder string, RubyProc block) {
            notDesignedForCompilation();

            return instanceEval(frame, getContext().getCoreLibrary().box(self), string, block);
        }

    }

    @CoreMethod(names = "method_missing", needsBlock = true, argumentsAsArray = true, visibility = Visibility.PRIVATE)
    public abstract static class MethodMissingNode extends CoreMethodNode {

        public MethodMissingNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public MethodMissingNode(MethodMissingNode prev) {
            super(prev);
        }

        @Specialization
        public Object methodMissing(RubyBasicObject self, Object[] args, @SuppressWarnings("unused") UndefinedPlaceholder block) {
            notDesignedForCompilation();

            final RubySymbol name = (RubySymbol) args[0];
            final Object[] sentArgs = Arrays.copyOfRange(args, 1, args.length);
            return methodMissing(self, name, sentArgs, null);
        }

        @Specialization
        public Object methodMissing(RubyBasicObject self, Object[] args, RubyProc block) {
            notDesignedForCompilation();

            final RubySymbol name = (RubySymbol) args[0];
            final Object[] sentArgs = Arrays.copyOfRange(args, 1, args.length);
            return methodMissing(self, name, sentArgs, block);
        }

        private Object methodMissing(RubyBasicObject self, RubySymbol name, Object[] args, RubyProc block) {
            if (lastCallWasVCall()) {
                throw new RaiseException(getContext().getCoreLibrary().nameErrorNoMethod(name.toString(), self.toString(), this));
            } else {
                throw new RaiseException(getContext().getCoreLibrary().noMethodError(name.toString(), self.toString(), this));
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

    @CoreMethod(names = "__send__", needsBlock = true, required = 1, argumentsAsArray = true)
    public abstract static class SendNode extends CoreMethodNode {

        @Child protected DispatchHeadNode dispatchNode;

        public SendNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);

            dispatchNode = new DispatchHeadNode(context, true, Options.TRUFFLE_DISPATCH_METAPROGRAMMING_ALWAYS_INDIRECT.load(), false, Dispatch.MissingBehavior.CALL_METHOD_MISSING);

            if (Options.TRUFFLE_DISPATCH_METAPROGRAMMING_ALWAYS_UNCACHED.load()) {
                dispatchNode.forceUncached();
            }
        }

        public SendNode(SendNode prev) {
            super(prev);
            dispatchNode = prev.dispatchNode;
        }

        @Specialization
        public Object send(VirtualFrame frame, RubyBasicObject self, Object[] args, @SuppressWarnings("unused") UndefinedPlaceholder block) {
            final Object name = args[0];
            final Object[] sendArgs = Arrays.copyOfRange(args, 1, args.length);
            return dispatchNode.call(frame, self, name, null, sendArgs);
        }

        @Specialization
        public Object send(VirtualFrame frame, RubyBasicObject self, Object[] args, RubyProc block) {
            final Object name = args[0];
            final Object[] sendArgs = Arrays.copyOfRange(args, 1, args.length);
            return dispatchNode.call(frame, self, name, block, sendArgs);
        }

    }

}
