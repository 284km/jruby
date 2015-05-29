/*
 * Copyright (c) 2013, 2015 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.nodes.core.array;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.source.SourceSection;
import org.jruby.truffle.nodes.RubyNode;
import org.jruby.truffle.runtime.RubyContext;
import org.jruby.truffle.runtime.core.RubyArray;
import org.jruby.truffle.runtime.array.ArrayUtils;

@NodeChildren({@NodeChild(value = "array", type = RubyNode.class)})
@ImportStatic(ArrayGuards.class)
public abstract class ArrayGetTailNode extends RubyNode {

    final int index;

    public ArrayGetTailNode(RubyContext context, SourceSection sourceSection, int index) {
        super(context, sourceSection);
        this.index = index;
    }

    @Specialization(guards = "isNull(array)")
    public RubyArray getTailNull(RubyArray array) {
        CompilerDirectives.transferToInterpreter();

        return ArrayNodes.createEmptyArray(getContext().getCoreLibrary().getArrayClass());
    }

    @Specialization(guards = "isIntegerFixnum(array)")
    public RubyArray getTailIntegerFixnum(RubyArray array) {
        CompilerDirectives.transferToInterpreter();

        if (index >= ArrayNodes.getSize(array)) {
            return ArrayNodes.createEmptyArray(getContext().getCoreLibrary().getArrayClass());
        } else {
            return ArrayNodes.createArray(getContext().getCoreLibrary().getArrayClass(), ArrayUtils.extractRange((int[]) ArrayNodes.getStore(array), index, ArrayNodes.getSize(array)), ArrayNodes.getSize(array) - index);
        }
    }

    @Specialization(guards = "isLongFixnum(array)")
    public RubyArray getTailLongFixnum(RubyArray array) {
        CompilerDirectives.transferToInterpreter();

        if (index >= ArrayNodes.getSize(array)) {
            return ArrayNodes.createEmptyArray(getContext().getCoreLibrary().getArrayClass());
        } else {
            return ArrayNodes.createArray(getContext().getCoreLibrary().getArrayClass(), ArrayUtils.extractRange((long[]) ArrayNodes.getStore(array), index, ArrayNodes.getSize(array)), ArrayNodes.getSize(array) - index);
        }
    }

    @Specialization(guards = "isFloat(array)")
    public RubyArray getTailFloat(RubyArray array) {
        CompilerDirectives.transferToInterpreter();

        if (index >= ArrayNodes.getSize(array)) {
            return ArrayNodes.createEmptyArray(getContext().getCoreLibrary().getArrayClass());
        } else {
            return ArrayNodes.createArray(getContext().getCoreLibrary().getArrayClass(), ArrayUtils.extractRange((double[]) ArrayNodes.getStore(array), index, ArrayNodes.getSize(array)), ArrayNodes.getSize(array) - index);
        }
    }

    @Specialization(guards = "isObject(array)")
    public RubyArray getTailObject(RubyArray array) {
        CompilerDirectives.transferToInterpreter();

        if (index >= ArrayNodes.getSize(array)) {
            return ArrayNodes.createEmptyArray(getContext().getCoreLibrary().getArrayClass());
        } else {
            return ArrayNodes.createArray(getContext().getCoreLibrary().getArrayClass(), ArrayUtils.extractRange((Object[]) ArrayNodes.getStore(array), index, ArrayNodes.getSize(array)), ArrayNodes.getSize(array) - index);
        }
    }

}
