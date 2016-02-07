/*
 * Copyright (c) 2013, 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.language.arguments;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.utilities.BranchProfile;
import com.oracle.truffle.api.source.SourceSection;
import org.jruby.truffle.language.RubyGuards;
import org.jruby.truffle.language.RubyNode;
import org.jruby.truffle.RubyContext;
import org.jruby.truffle.core.Layouts;

/**
 * Read an optional argument.
 */
public class ReadOptionalArgumentNode extends RubyNode {

    private final int index;
    private final int minimum;
    private final boolean considerRejectedKWArgs;
    @Child private RubyNode defaultValue;
    @Child private ReadRestArgumentNode readRestArgumentNode;
    private final boolean reduceMinimumWhenNoKWargs;
    @Child private ReadUserKeywordsHashNode readUserKeywordsHashNode;

    private final BranchProfile defaultValueProfile = BranchProfile.create();

    public ReadOptionalArgumentNode(RubyContext context, SourceSection sourceSection, int index, int minimum, boolean considerRejectedKWArgs, RubyNode defaultValue, ReadRestArgumentNode readRestArgumentNode, int requiredForKWArgs, boolean reduceMinimumWhenNoKWargs) {
        super(context, sourceSection);
        this.index = index;
        this.minimum = minimum;
        this.considerRejectedKWArgs = considerRejectedKWArgs;
        this.defaultValue = defaultValue;
        this.readRestArgumentNode = readRestArgumentNode;
        this.reduceMinimumWhenNoKWargs = reduceMinimumWhenNoKWargs;
        readUserKeywordsHashNode = new ReadUserKeywordsHashNode(context, sourceSection, requiredForKWArgs);
    }

    @Override
    public Object execute(VirtualFrame frame) {
        int actualMinimum = minimum;

        if (reduceMinimumWhenNoKWargs) {
            CompilerDirectives.transferToInterpreter();

            if (readUserKeywordsHashNode.execute(frame) == null) {
                actualMinimum--;
            }
        }

        if (RubyArguments.getArgumentsCount(frame.getArguments()) < actualMinimum) {
            defaultValueProfile.enter();

            if (considerRejectedKWArgs) {
                CompilerDirectives.transferToInterpreter();

                final Object rest = readRestArgumentNode.execute(frame);

                if (RubyGuards.isRubyArray(rest) && Layouts.ARRAY.getSize((DynamicObject) rest) > 0) {
                    return ruby("rest[0]", "rest", rest);
                }
            }

            return defaultValue.execute(frame);
        } else {
            return RubyArguments.getArgument(frame.getArguments(), index);
        }
    }

}
