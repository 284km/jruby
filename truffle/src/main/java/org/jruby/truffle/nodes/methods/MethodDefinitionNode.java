/*
 * Copyright (c) 2013, 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.nodes.methods;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.source.SourceSection;
import org.jruby.runtime.Visibility;
import org.jruby.truffle.nodes.RubyNode;
import org.jruby.truffle.runtime.RubyArguments;
import org.jruby.truffle.runtime.RubyContext;
import org.jruby.truffle.runtime.methods.InternalMethod;
import org.jruby.truffle.runtime.methods.SharedMethodInfo;

/**
 * Define a method from a method literal (def mymethod ... end).
 * That is, store the definition of a method and when executed
 * produce the executable object that results.
 */
public class MethodDefinitionNode extends RubyNode {

    private final String name;
    private final SharedMethodInfo sharedMethodInfo;
    private final CallTarget callTarget;

    @Child private GetDefaultDefineeNode getDefaultDefineeNode;

    public MethodDefinitionNode(RubyContext context, SourceSection sourceSection, String name, SharedMethodInfo sharedMethodInfo, CallTarget callTarget) {
        super(context, sourceSection);
        this.name = name;
        this.sharedMethodInfo = sharedMethodInfo;
        this.callTarget = callTarget;
    }

    public InternalMethod executeMethod(VirtualFrame frame) {
        final DynamicObject dummyModule = getContext().getCoreLibrary().getObjectClass();
        final Visibility dummyVisibility = Visibility.PUBLIC;

        final DynamicObject capturedDefaultDefinee;
        if (RubyArguments.getDeclarationContext(frame.getArguments()) == DeclarationContext.INSTANCE_EVAL) {
            if (getDefaultDefineeNode == null) {
                CompilerDirectives.transferToInterpreter();
                getDefaultDefineeNode = insert(new GetDefaultDefineeNode(getContext(), getSourceSection()));
            }
            capturedDefaultDefinee = getDefaultDefineeNode.execute(frame);
        } else {
            capturedDefaultDefinee = null;
        }

        return new InternalMethod(sharedMethodInfo, name, dummyModule, dummyVisibility, false, null, callTarget, null,
                capturedDefaultDefinee);
    }

    @Override
    public Object execute(VirtualFrame frame) {
        return executeMethod(frame);
    }

}
