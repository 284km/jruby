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

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.dsl.*;
import com.oracle.truffle.api.nodes.*;
import org.jruby.truffle.nodes.*;
import org.jruby.truffle.runtime.*;
import org.jruby.truffle.runtime.core.RubyArray;

/**
 * Index an array, without using any method lookup. This isn't a call - it's an operation on a core
 * class.
 */
@NodeChildren({@NodeChild(value = "array", type = RubyNode.class)})
public abstract class ArrayIndexNode extends RubyNode {

    final int index;

    public ArrayIndexNode(RubyContext context, SourceSection sourceSection, int index) {
        super(context, sourceSection);
        this.index = index;
    }

    public ArrayIndexNode(ArrayIndexNode prev) {
        super(prev);
        index = prev.index;
    }

    @Specialization(guards = "isNull", order = 1)
    public NilPlaceholder getNull(RubyArray array) {
        return NilPlaceholder.INSTANCE;
    }

    @Specialization(guards = "isIntegerFixnum", rewriteOn=UnexpectedResultException.class, order = 2)
    public int getIntegerFixnumInBounds(RubyArray array) throws UnexpectedResultException {
        int normalisedIndex = array.normaliseIndex(index);

        if (normalisedIndex < 0 || normalisedIndex >= array.getSize()) {
            throw new UnexpectedResultException(NilPlaceholder.INSTANCE);
        } else {
            return ((int[]) array.getStore())[normalisedIndex];
        }
    }

    @Specialization(guards = "isIntegerFixnum", order = 3)
    public Object getIntegerFixnum(RubyArray array) {
        int normalisedIndex = array.normaliseIndex(index);

        if (normalisedIndex < 0 || normalisedIndex >= array.getSize()) {
            return NilPlaceholder.INSTANCE;
        } else {
            return ((int[]) array.getStore())[normalisedIndex];
        }
    }

    @Specialization(guards = "isLongFixnum", rewriteOn=UnexpectedResultException.class, order = 4)
    public long getLongFixnumInBounds(RubyArray array) throws UnexpectedResultException {
        int normalisedIndex = array.normaliseIndex(index);

        if (normalisedIndex < 0 || normalisedIndex >= array.getSize()) {
            throw new UnexpectedResultException(NilPlaceholder.INSTANCE);
        } else {
            return ((long[]) array.getStore())[normalisedIndex];
        }
    }

    @Specialization(guards = "isLongFixnum", order = 5)
    public Object getLongFixnum(RubyArray array) {
        int normalisedIndex = array.normaliseIndex(index);

        if (normalisedIndex < 0 || normalisedIndex >= array.getSize()) {
            return NilPlaceholder.INSTANCE;
        } else {
            return ((long[]) array.getStore())[normalisedIndex];
        }
    }

    @Specialization(guards = "isFloat", rewriteOn=UnexpectedResultException.class, order = 6)
    public double getFloatInBounds(RubyArray array) throws UnexpectedResultException {
        int normalisedIndex = array.normaliseIndex(index);

        if (normalisedIndex < 0 || normalisedIndex >= array.getSize()) {
            throw new UnexpectedResultException(NilPlaceholder.INSTANCE);
        } else {
            return ((double[]) array.getStore())[normalisedIndex];
        }
    }

    @Specialization(guards = "isFloat", order = 7)
    public Object getFloat(RubyArray array) {
        int normalisedIndex = array.normaliseIndex(index);

        if (normalisedIndex < 0 || normalisedIndex >= array.getSize()) {
            return NilPlaceholder.INSTANCE;
        } else {
            return ((double[]) array.getStore())[normalisedIndex];
        }
    }

    @Specialization(guards = "isObject", order = 8)
    public Object getObject(RubyArray array) {
        int normalisedIndex = array.normaliseIndex(index);

        if (normalisedIndex < 0 || normalisedIndex >= array.getSize()) {
            return NilPlaceholder.INSTANCE;
        } else {
            return ((Object[]) array.getStore())[normalisedIndex];
        }
    }

    // TODO(CS): copied and pasted from ArrayCoreMethodNode - need a way to use statics from other classes in the DSL

    protected boolean isNull(RubyArray array) {
        return array.getStore() == null;
    }

    protected boolean isIntegerFixnum(RubyArray array) {
        return array.getStore() instanceof int[];
    }

    protected boolean isLongFixnum(RubyArray array) {
        return array.getStore() instanceof long[];
    }

    protected boolean isFloat(RubyArray array) {
        return array.getStore() instanceof double[];
    }

    protected boolean isObject(RubyArray array) {
        return array.getStore() instanceof Object[];
    }

    protected boolean isOtherNull(RubyArray array, RubyArray other) {
        return other.getStore() == null;
    }

    protected boolean isOtherIntegerFixnum(RubyArray array, RubyArray other) {
        return other.getStore() instanceof int[];
    }

    protected boolean isOtherLongFixnum(RubyArray array, RubyArray other) {
        return other.getStore() instanceof long[];
    }

    protected boolean isOtherFloat(RubyArray array, RubyArray other) {
        return other.getStore() instanceof double[];
    }

    protected boolean isOtherObject(RubyArray array, RubyArray other) {
        return other.getStore() instanceof Object[];
    }

    protected boolean areBothNull(RubyArray a, RubyArray b) {
        return a.getStore() == null && b.getStore() == null;
    }

    protected boolean areBothIntegerFixnum(RubyArray a, RubyArray b) {
        return a.getStore() instanceof int[] && b.getStore() instanceof int[];
    }

    protected boolean areBothLongFixnum(RubyArray a, RubyArray b) {
        return a.getStore() instanceof long[] && b.getStore() instanceof long[];
    }

    protected boolean areBothFloat(RubyArray a, RubyArray b) {
        return a.getStore() instanceof double[] && b.getStore() instanceof double[];
    }

    protected boolean areBothObject(RubyArray a, RubyArray b) {
        return a.getStore() instanceof Object[] && b.getStore() instanceof Object[];
    }

}
