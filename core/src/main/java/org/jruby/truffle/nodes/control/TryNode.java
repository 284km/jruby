/*
 * Copyright (c) 2013, 2014 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.nodes.control;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.source.*;
import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.nodes.*;
import com.oracle.truffle.api.utilities.*;
import org.jruby.truffle.nodes.*;
import org.jruby.truffle.nodes.methods.ExceptionTranslatingNode;
import org.jruby.truffle.runtime.*;
import org.jruby.truffle.runtime.control.*;
import org.jruby.truffle.runtime.core.RubyBasicObject;

/**
 * Represents a block of code run with exception handlers. There's no {@code try} keyword in Ruby -
 * it's implicit - but it's similar to a try statement in any other language.
 */
public class TryNode extends RubyNode {

    @Child protected ExceptionTranslatingNode tryPart;
    @Children final RescueNode[] rescueParts;
    @Child protected RubyNode elsePart;

    private final BranchProfile elseProfile = new BranchProfile();
    private final BranchProfile controlFlowProfile = new BranchProfile();
    private final BranchProfile raiseExceptionProfile = new BranchProfile();

    public TryNode(RubyContext context, SourceSection sourceSection, ExceptionTranslatingNode tryPart, RescueNode[] rescueParts, RubyNode elsePart) {
        super(context, sourceSection);
        this.tryPart = tryPart;
        this.rescueParts = rescueParts;
        this.elsePart = elsePart;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        while (true) {
            getContext().getSafepointManager().poll();

            Object result;

            try {
                result = tryPart.execute(frame);
            } catch (ControlFlowException exception) {
                controlFlowProfile.enter();
                throw exception;
            } catch (RaiseException exception) {
                raiseExceptionProfile.enter();

                try {
                    return handleException(frame, exception);
                } catch (RetryException e) {
                    continue;
                }
            }

            elseProfile.enter();
            elsePart.executeVoid(frame);
            return result;
        }
    }

    @ExplodeLoop
    private Object handleException(VirtualFrame frame, RaiseException exception) {
        CompilerAsserts.neverPartOfCompilation();

        getContext().getCoreLibrary().getGlobalVariablesObject().setInstanceVariable("$!", exception.getRubyException());

        for (RescueNode rescue : rescueParts) {
            if (rescue.canHandle(frame, exception.getRubyException())) {
                return rescue.execute(frame);
            }
        }

        throw exception;
    }

}
