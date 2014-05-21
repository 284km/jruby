/*
 * Copyright (c) 2014 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.nodes.core;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.nodes.Node;
import org.jruby.truffle.runtime.ArrayUtils;
import org.jruby.truffle.runtime.RubyContext;

public abstract class ArrayBuilderNode extends Node {

    private final RubyContext context;
    private final boolean lengthKnown;

    public ArrayBuilderNode(RubyContext context, boolean maxLengthKnown) {
        this.context = context;
        this.lengthKnown = maxLengthKnown;

        if (!maxLengthKnown) {
            throw new UnsupportedOperationException();
        }
    }

    public abstract Object length(int length);

    public abstract Object append(Object store, int index, Object value);

    public void finish() {
    }

    protected RubyContext getContext() {
        return context;
    }

    protected boolean isMaxLengthKnown() {
        return lengthKnown;
    }

    public static class UninitializedArrayBuilderNode extends ArrayBuilderNode {

        private boolean couldUseInteger = true;
        private boolean couldUseLong = true;
        private boolean couldUseDouble = true;

        public UninitializedArrayBuilderNode(RubyContext context, boolean maxLengthKnown) {
            super(context, maxLengthKnown);
        }

        @Override
        public Object length(int length) {
            CompilerDirectives.transferToInterpreter();

            return new Object[length];
        }

        @Override
        public Object append(Object store, int index, Object value) {
            CompilerDirectives.transferToInterpreter();

            if (value instanceof Integer) {
                couldUseDouble = false;
            } else if (value instanceof Long) {
                couldUseInteger = false;
                couldUseDouble = false;
            } else if (value instanceof Double) {
                couldUseInteger = false;
                couldUseLong = false;
            } else {
                couldUseInteger = false;
                couldUseLong = false;
                couldUseDouble = false;
            }

            ((Object[]) store)[index] = value;
            return store;
        }

        @Override
        public void finish() {
            if (couldUseInteger) {
                replace(new IntegerArrayBuilderNode(getContext(), isMaxLengthKnown()));
            } else if (couldUseLong) {
                replace(new LongArrayBuilderNode(getContext(), isMaxLengthKnown()));
            } else if (couldUseDouble) {
                replace(new DoubleArrayBuilderNode(getContext(), isMaxLengthKnown()));
            } else {
                replace(new ObjectArrayBuilderNode(getContext(), isMaxLengthKnown()));
            }
        }

    }

    public static class IntegerArrayBuilderNode extends ArrayBuilderNode {

        public IntegerArrayBuilderNode(RubyContext context, boolean maxLengthKnown) {
            super(context, maxLengthKnown);
        }

        @Override
        public Object length(int length) {
            return new int[length];
        }

        @Override
        public Object append(Object store, int index, Object value) {
            // TODO(CS): inject probability
            if (value instanceof Integer) {
                ((int[]) store)[index] = (int) value;
                return store;
            } else {
                CompilerDirectives.transferToInterpreter();

                replace(new ObjectArrayBuilderNode(getContext(), isMaxLengthKnown()));

                final Object[] newStore = ArrayUtils.box((int[]) store);
                newStore[index] = value;
                return newStore;
            }
        }

    }

    public static class LongArrayBuilderNode extends ArrayBuilderNode {

        public LongArrayBuilderNode(RubyContext context, boolean maxLengthKnown) {
            super(context, maxLengthKnown);
        }

        @Override
        public Object length(int length) {
            return new long[length];
        }

        @Override
        public Object append(Object store, int index, Object value) {
            // TODO(CS): inject probability
            if (value instanceof Long) {
                ((long[]) store)[index] = (long) value;
                return store;
            } else if (value instanceof Integer) {
                ((long[]) store)[index] = (int) value;
                return store;
            } else {
                CompilerDirectives.transferToInterpreter();

                replace(new ObjectArrayBuilderNode(getContext(), isMaxLengthKnown()));

                final Object[] newStore = ArrayUtils.box((long[]) store);
                newStore[index] = value;
                return newStore;
            }
        }

    }

    public static class DoubleArrayBuilderNode extends ArrayBuilderNode {

        public DoubleArrayBuilderNode(RubyContext context, boolean maxLengthKnown) {
            super(context, maxLengthKnown);
        }

        @Override
        public Object length(int length) {
            return new double[length];
        }

        @Override
        public Object append(Object store, int index, Object value) {
            // TODO(CS): inject probability
            if (value instanceof Double) {
                ((double[]) store)[index] = (double) value;
                return store;
            } else {
                CompilerDirectives.transferToInterpreter();

                replace(new ObjectArrayBuilderNode(getContext(), isMaxLengthKnown()));

                final Object[] newStore = ArrayUtils.box((double[]) store);
                newStore[index] = value;
                return newStore;
            }
        }

    }

    public static class ObjectArrayBuilderNode extends ArrayBuilderNode {

        public ObjectArrayBuilderNode(RubyContext context, boolean maxLengthKnown) {
            super(context, maxLengthKnown);
        }

        @Override
        public Object length(int length) {
            return new Object[length];
        }

        @Override
        public Object append(Object store, int index, Object value) {
            ((Object[]) store)[index] = value;
            return store;
        }

    }

}
