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
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.source.SourceSection;

import org.jruby.truffle.nodes.RubyGuards;
import org.jruby.truffle.nodes.locals.ReadFrameSlotNode;
import org.jruby.truffle.nodes.locals.ReadFrameSlotNodeGen;
import org.jruby.truffle.nodes.locals.WriteFrameSlotNode;
import org.jruby.truffle.nodes.locals.WriteFrameSlotNodeGen;
import org.jruby.truffle.nodes.objects.AllocateObjectNode;
import org.jruby.truffle.nodes.objects.AllocateObjectNodeGen;
import org.jruby.truffle.runtime.RubyArguments;
import org.jruby.truffle.runtime.RubyContext;
import org.jruby.truffle.runtime.ThreadLocalObject;
import org.jruby.truffle.runtime.control.RaiseException;
import org.jruby.truffle.runtime.core.ArrayOperations;
import org.jruby.truffle.runtime.layouts.Layouts;

@CoreClass(name = "Binding")
public abstract class BindingNodes {

    @CoreMethod(names = { "dup", "clone" })
    public abstract static class DupNode extends UnaryCoreMethodNode {

        @Child private AllocateObjectNode allocateObjectNode;

        public DupNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
            allocateObjectNode = AllocateObjectNodeGen.create(context, sourceSection, null, null);
        }

        @Specialization
        public DynamicObject dup(DynamicObject binding) {
            DynamicObject copy = allocateObjectNode.allocate(
                    Layouts.BASIC_OBJECT.getLogicalClass(binding),
                    Layouts.BINDING.getFrame(binding));
            return copy;
        }

    }

    @CoreMethod(names = "local_variable_get", required = 1)
    public abstract static class LocalVariableGetNode extends CoreMethodArrayArgumentsNode {

        private final DynamicObject dollarUnderscore;

        public LocalVariableGetNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
            dollarUnderscore = getSymbol("$_");
        }

        @Specialization(guards = {
                "isRubySymbol(symbol)",
                "symbol == cachedSymbol",
                "!isLastLine(cachedSymbol)",
                "getFrameDescriptor(binding) == cachedFrameDescriptor",
        }, limit = "getCacheLimit()")
        public Object localVariableGetCached(DynamicObject binding, DynamicObject symbol,
                                             @Cached("symbol") DynamicObject cachedSymbol,
                                             @Cached("getFrameDescriptor(binding)") FrameDescriptor cachedFrameDescriptor,
                                             @Cached("findFrameSlot(binding, symbol)") FrameSlot cachedFrameSlot,
                                             @Cached("createReadNode(cachedFrameSlot)") ReadFrameSlotNode readLocalVariableNode) {
            if (cachedFrameSlot == null) {
                CompilerDirectives.transferToInterpreter();
                throw new RaiseException(getContext().getCoreLibrary().nameErrorLocalVariableNotDefined(Layouts.SYMBOL.getString(symbol), binding, this));
            } else {
                return readLocalVariableNode.executeRead(Layouts.BINDING.getFrame(binding));
            }
        }

        @TruffleBoundary
        @Specialization(guards = {"isRubySymbol(symbol)", "!isLastLine(symbol)"})
        public Object localVariableGetUncached(DynamicObject binding, DynamicObject symbol) {
            final MaterializedFrame frame = Layouts.BINDING.getFrame(binding);
            final FrameSlot frameSlot = frame.getFrameDescriptor().findFrameSlot(Layouts.SYMBOL.getString(symbol));

            if (frameSlot == null) {
                throw new RaiseException(getContext().getCoreLibrary().nameErrorLocalVariableNotDefined(Layouts.SYMBOL.getString(symbol), binding, this));
            }

            return frame.getValue(frameSlot);
        }

        @TruffleBoundary
        @Specialization(guards = {"isRubySymbol(symbol)", "isLastLine(symbol)"})
        public Object localVariableGetLastLine(DynamicObject binding, DynamicObject symbol) {
            final MaterializedFrame frame = Layouts.BINDING.getFrame(binding);
            final FrameSlot frameSlot = frame.getFrameDescriptor().findFrameSlot(Layouts.SYMBOL.getString(symbol));

            if (frameSlot == null) {
                throw new RaiseException(getContext().getCoreLibrary().nameErrorLocalVariableNotDefined(Layouts.SYMBOL.getString(symbol), binding, this));
            }

            final Object value = frame.getValue(frameSlot);
            if (value instanceof ThreadLocalObject) {
                return ((ThreadLocalObject) value).get();
            } else {
                return value;
            }
        }

        protected FrameDescriptor getFrameDescriptor(DynamicObject binding) {
            assert RubyGuards.isRubyBinding(binding);
            return Layouts.BINDING.getFrame(binding).getFrameDescriptor();
        }

        protected FrameSlot findFrameSlot(DynamicObject binding, DynamicObject symbol) {
            assert RubyGuards.isRubyBinding(binding);
            assert RubyGuards.isRubySymbol(symbol);

            final String symbolString = Layouts.SYMBOL.getString(symbol);

            MaterializedFrame frame = Layouts.BINDING.getFrame(binding);

            while (frame != null) {
                final FrameSlot frameSlot = frame.getFrameDescriptor().findFrameSlot(symbolString);

                if (frameSlot != null) {
                    return frameSlot;
                }

                frame = RubyArguments.getDeclarationFrame(frame.getArguments());
            }

            return null;
        }

        protected ReadFrameSlotNode createReadNode(FrameSlot frameSlot) {
            if (frameSlot == null) {
                return null;
            } else {
                return ReadFrameSlotNodeGen.create(frameSlot);
            }
        }

        protected boolean isLastLine(DynamicObject symbol) {
            return symbol == dollarUnderscore;
        }

        protected int getCacheLimit() {
            return getContext().getOptions().BINDING_LOCAL_VARIABLE_CACHE;
        }

    }

    @CoreMethod(names = "local_variable_set", required = 2)
    public abstract static class LocalVariableSetNode extends CoreMethodArrayArgumentsNode {

        private final DynamicObject dollarUnderscore;

        public LocalVariableSetNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
            dollarUnderscore = getSymbol("$_");
        }

        @Specialization(guards = {
                "isRubySymbol(symbol)",
                "!isLastLine(symbol)",
                "getFrameDescriptor(binding) == cachedFrameDescriptor",
                "symbol == cachedSymbol"
        }, limit = "getCacheLimit()")
        public Object localVariableSetCached(DynamicObject binding, DynamicObject symbol, Object value,
                                             @Cached("symbol") DynamicObject cachedSymbol,
                                             @Cached("createWriteNode(findFrameSlot(binding, symbol))") WriteFrameSlotNode writeLocalVariableNode,
                                             @Cached("getFrameDescriptor(binding)") FrameDescriptor cachedFrameDescriptor) {
            return writeLocalVariableNode.executeWrite(Layouts.BINDING.getFrame(binding), value);
        }

        @TruffleBoundary
        @Specialization(guards = {"isRubySymbol(symbol)", "!isLastLine(symbol)"})
        public Object localVariableSetUncached(DynamicObject binding, DynamicObject symbol, Object value) {
            final MaterializedFrame frame = Layouts.BINDING.getFrame(binding);
            final FrameSlot frameSlot = findFrameSlot(binding, symbol);
            frame.setObject(frameSlot, value);
            return value;
        }

        @TruffleBoundary
        @Specialization(guards = {"isRubySymbol(symbol)", "isLastLine(symbol)"})
        public Object localVariableSetLastLine(DynamicObject binding, DynamicObject symbol, Object value) {
            final MaterializedFrame frame = Layouts.BINDING.getFrame(binding);
            final FrameSlot frameSlot = frame.getFrameDescriptor().findFrameSlot(Layouts.SYMBOL.getString(symbol));
            frame.setObject(frameSlot, ThreadLocalObject.wrap(getContext(), value));
            return value;
        }

        protected FrameDescriptor getFrameDescriptor(DynamicObject binding) {
            assert RubyGuards.isRubyBinding(binding);
            return Layouts.BINDING.getFrame(binding).getFrameDescriptor();
        }

        protected FrameSlot findFrameSlot(DynamicObject binding, DynamicObject symbol) {
            assert RubyGuards.isRubyBinding(binding);
            assert RubyGuards.isRubySymbol(symbol);

            final String symbolString = Layouts.SYMBOL.getString(symbol);

            MaterializedFrame frame = Layouts.BINDING.getFrame(binding);

            while (frame != null) {
                final FrameSlot frameSlot = frame.getFrameDescriptor().findFrameSlot(symbolString);

                if (frameSlot != null) {
                    return frameSlot;
                }

                frame = RubyArguments.getDeclarationFrame(frame.getArguments());
            }

            return Layouts.BINDING.getFrame(binding).getFrameDescriptor().addFrameSlot(symbolString);
        }

        protected WriteFrameSlotNode createWriteNode(FrameSlot frameSlot) {
            return WriteFrameSlotNodeGen.create(frameSlot);
        }

        protected boolean isLastLine(DynamicObject symbol) {
            return symbol == dollarUnderscore;
        }

        protected int getCacheLimit() {
            return getContext().getOptions().BINDING_LOCAL_VARIABLE_CACHE;
        }
    }

    @CoreMethod(names = "local_variables")
    public abstract static class LocalVariablesNode extends CoreMethodArrayArgumentsNode {

        public LocalVariablesNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        @TruffleBoundary
        @Specialization
        public DynamicObject localVariables(DynamicObject binding) {
            final DynamicObject array = Layouts.ARRAY.createArray(getContext().getCoreLibrary().getArrayFactory(), null, 0);

            MaterializedFrame frame = Layouts.BINDING.getFrame(binding);

            while (frame != null) {
                for (Object name : frame.getFrameDescriptor().getIdentifiers()) {
                    if (name instanceof String) {
                        ArrayOperations.append(array, getSymbol((String) name));
                    }
                }

                frame = RubyArguments.getDeclarationFrame(frame.getArguments());
            }

            return array;
        }
    }

    @CoreMethod(names = "allocate", constructor = true)
    public abstract static class AllocateNode extends CoreMethodArrayArgumentsNode {

        public AllocateNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        @TruffleBoundary
        @Specialization
        public DynamicObject allocate(DynamicObject rubyClass) {
            throw new RaiseException(getContext().getCoreLibrary().typeErrorAllocatorUndefinedFor(rubyClass, this));
        }

    }

}
