/*
 * Copyright (c) 2015 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.nodes.array;

import com.oracle.truffle.api.dsl.ImportGuards;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.UnexpectedResultException;
import com.oracle.truffle.api.source.SourceSection;
import org.jruby.truffle.nodes.RubyNode;
import org.jruby.truffle.nodes.core.ArrayGuards;
import org.jruby.truffle.runtime.RubyContext;
import org.jruby.truffle.runtime.core.RubyArray;
import org.jruby.truffle.runtime.core.RubyNilClass;

@NodeChildren({
        @NodeChild(value="array", type=RubyNode.class),
        @NodeChild(value="index", type=RubyNode.class)
})
@ImportGuards(ArrayGuards.class)
public abstract class ArrayReadNormalizedNode extends RubyNode {

    public ArrayReadNormalizedNode(RubyContext context, SourceSection sourceSection) {
        super(context, sourceSection);
    }

    public ArrayReadNormalizedNode(ArrayReadNormalizedNode prev) {
        super(prev);
    }

    public abstract Object executeRead(VirtualFrame frame, RubyArray array, int index);

    @Specialization(
            guards="isNullArray"
    )
    public RubyNilClass readNull(RubyArray array, int index) {
        return getContext().getCoreLibrary().getNilObject();
    }

    @Specialization(
            guards={"isNormalisedInBounds", "isIntegerArray"}
    )
    public int readIntegerInBounds(RubyArray array, int index) {
        return ((int[]) array.getStore())[index];
    }

    @Specialization(
            guards={"isNormalisedInBounds", "isLongArray"}
    )
    public long readLongInBounds(RubyArray array, int index) {
        return ((long[]) array.getStore())[index];
    }

    @Specialization(
            guards={"isNormalisedInBounds", "isDoubleArray"}
    )
    public double readDoubleInBounds(RubyArray array, int index) {
        return ((double[]) array.getStore())[index];
    }

    @Specialization(
            guards={"isNormalisedInBounds", "isObjectArray"}
    )
    public Object readObjectInBounds(RubyArray array, int index) {
        return ((Object[]) array.getStore())[index];
    }

    @Specialization(
            guards="!isNormalisedInBounds"
    )
    public RubyNilClass readOutOfBounds(RubyArray array, int index) {
        return getContext().getCoreLibrary().getNilObject();
    }

}
