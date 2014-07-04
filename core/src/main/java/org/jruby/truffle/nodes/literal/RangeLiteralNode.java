/*
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.nodes.literal;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.source.*;
import com.oracle.truffle.api.dsl.*;
import com.oracle.truffle.api.nodes.*;
import com.oracle.truffle.api.utilities.BranchProfile;
import org.jruby.truffle.nodes.*;
import org.jruby.truffle.runtime.*;
import org.jruby.truffle.runtime.core.RubyRange;

@NodeChildren({@NodeChild("begin"), @NodeChild("end")})
public abstract class RangeLiteralNode extends RubyNode {

    private final boolean excludeEnd;

    private final BranchProfile beginIntegerProfile = new BranchProfile();
    private final BranchProfile beginLongProfile = new BranchProfile();
    private final BranchProfile endIntegerProfile = new BranchProfile();
    private final BranchProfile endLongProfile = new BranchProfile();
    private final BranchProfile objectProfile = new BranchProfile();

    public RangeLiteralNode(RubyContext context, SourceSection sourceSection, boolean excludeEnd) {
        super(context, sourceSection);
        this.excludeEnd = excludeEnd;
    }

    public RangeLiteralNode(RangeLiteralNode prev) {
        this(prev.getContext(), prev.getSourceSection(), prev.excludeEnd);
    }

    @Specialization(order = 1)
    public RubyRange.IntegerFixnumRange doRange(int begin, int end) {
        return new RubyRange.IntegerFixnumRange(getContext().getCoreLibrary().getRangeClass(), begin, end, excludeEnd);
    }

    @Specialization(order = 2)
    public RubyRange.LongFixnumRange doRange(int begin, long end) {
        return new RubyRange.LongFixnumRange(getContext().getCoreLibrary().getRangeClass(), begin, end, excludeEnd);
    }

    @Specialization(order = 3)
    public RubyRange.LongFixnumRange doRange(long begin, long end) {
        return new RubyRange.LongFixnumRange(getContext().getCoreLibrary().getRangeClass(), begin, end, excludeEnd);
    }

    @Specialization(order = 4)
    public RubyRange.LongFixnumRange doRange(long begin, int end) {
        return new RubyRange.LongFixnumRange(getContext().getCoreLibrary().getRangeClass(), begin, end, excludeEnd);
    }

    @Specialization(order = 5)
    public Object doRange(Object begin, Object end) {
        if (begin instanceof Integer) {
            beginIntegerProfile.enter();

            if (end instanceof Integer) {
                endIntegerProfile.enter();
                return doRange((int) begin, (int) end);
            }

            if (end instanceof Long) {
                endLongProfile.enter();
                return doRange((int) begin, (long) end);
            }
        } else if (begin instanceof Long) {
            beginLongProfile.enter();

            if (end instanceof Integer) {
                endIntegerProfile.enter();
                return doRange((long) begin, (int) end);
            }

            if (end instanceof Long) {
                endLongProfile.enter();
                return doRange((long) begin, (long) end);
            }
        }

        objectProfile.enter();

        return new RubyRange.ObjectRange(getContext().getCoreLibrary().getRangeClass(), begin, end, excludeEnd);
    }

}
