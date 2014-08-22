/*
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.nodes.methods.arguments;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.source.*;
import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.nodes.*;
import com.oracle.truffle.api.utilities.*;
import org.jruby.truffle.nodes.*;
import org.jruby.truffle.runtime.*;

/**
 * Read pre-optional argument.
 */
public class ReadPreArgumentNode extends RubyNode {

    private final int index;

    private final BranchProfile outOfRangeProfile = new BranchProfile();
    private final MissingArgumentBehaviour missingArgumentBehaviour;

    public ReadPreArgumentNode(RubyContext context, SourceSection sourceSection, int index, MissingArgumentBehaviour missingArgumentBehaviour) {
        super(context, sourceSection);
        this.index = index;
        this.missingArgumentBehaviour = missingArgumentBehaviour;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        if (index >= RubyArguments.getUserArgumentsCount(frame.getArguments())) {
            outOfRangeProfile.enter();

            switch (missingArgumentBehaviour) {
                case RUNTIME_ERROR:
                    break;

                case UNDEFINED:
                    return UndefinedPlaceholder.INSTANCE;

                case NIL:
                    return NilPlaceholder.INSTANCE;
            }
        }

        return RubyArguments.getUserArgument(frame.getArguments(), index);
    }

}
