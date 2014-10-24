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

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.source.*;
import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.nodes.*;
import com.oracle.truffle.api.utilities.BranchProfile;
import org.jruby.truffle.nodes.*;
import org.jruby.truffle.runtime.*;
import org.jruby.truffle.runtime.core.RubyArray;

/**
 * Concatenate arrays.
 */
public final class ArrayConcatNode extends RubyNode {

    @Children protected final RubyNode[] children;
    @Child protected ArrayBuilderNode arrayBuilderNode;

    private final BranchProfile appendArrayProfile = BranchProfile.create();
    private final BranchProfile appendObjectProfile = BranchProfile.create();

    public ArrayConcatNode(RubyContext context, SourceSection sourceSection, RubyNode[] children) {
        super(context, sourceSection);
        assert children.length > 1;
        this.children = children;
        arrayBuilderNode = new ArrayBuilderNode.UninitializedArrayBuilderNode(context);
    }

    @ExplodeLoop
    @Override
    public RubyArray execute(VirtualFrame frame) {
        Object store = arrayBuilderNode.start();
        int length = 0;

        for (int n = 0; n < children.length; n++) {
            final Object childObject = children[n].execute(frame);

            if (childObject instanceof RubyArray) {
                appendArrayProfile.enter();
                final RubyArray childArray = (RubyArray) childObject;
                store = arrayBuilderNode.ensure(store, length + childArray.getSize());
                store = arrayBuilderNode.append(store, length, childArray);
                length += childArray.getSize();
            } else {
                appendObjectProfile.enter();
                store = arrayBuilderNode.ensure(store, length + 1);
                store = arrayBuilderNode.append(store, length, childObject);
                length++;
            }
        }

        return new RubyArray(getContext().getCoreLibrary().getArrayClass(), arrayBuilderNode.finish(store, length), length);
    }

    @ExplodeLoop
    @Override
    public void executeVoid(VirtualFrame frame) {
        for (int n = 0; n < children.length; n++) {
            children[n].executeVoid(frame);
        }
    }

}
