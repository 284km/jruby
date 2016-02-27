/*
 * Copyright (c) 2015, 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.language.globals;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.source.SourceSection;
import org.jcodings.specific.UTF8Encoding;
import org.jruby.truffle.RubyContext;
import org.jruby.truffle.language.RubyNode;
import org.jruby.truffle.language.objects.ReadObjectFieldNode;
import org.jruby.truffle.language.objects.ReadObjectFieldNodeGen;
import org.jruby.truffle.language.threadlocal.ThreadLocalObjectNode;
import org.jruby.truffle.language.threadlocal.ThreadLocalObjectNodeGen;

public class ReadThreadLocalGlobalVariableNode extends RubyNode {

    private final boolean alwaysDefined;

    @Child private ThreadLocalObjectNode threadLocalVariablesObjectNode;
    @Child private ReadObjectFieldNode readNode;

    public ReadThreadLocalGlobalVariableNode(RubyContext context, SourceSection sourceSection, String name, boolean alwaysDefined) {
        super(context, sourceSection);
        this.alwaysDefined = alwaysDefined;
        this.threadLocalVariablesObjectNode = ThreadLocalObjectNodeGen.create(context, sourceSection);
        readNode = ReadObjectFieldNodeGen.create(getContext(), name, nil());
    }

    @Override
    public Object execute(VirtualFrame frame) {
        final DynamicObject threadLocalVariablesObject = threadLocalVariablesObjectNode.executeDynamicObject(frame);
        return readNode.execute(threadLocalVariablesObject);
    }

    @Override
    public Object isDefined(VirtualFrame frame) {
        CompilerDirectives.transferToInterpreter();
        final DynamicObject threadLocalVariablesObject = threadLocalVariablesObjectNode.executeDynamicObject(frame);

        if (alwaysDefined || readNode.execute(threadLocalVariablesObject) != nil()) {
            return create7BitString("global-variable", UTF8Encoding.INSTANCE);
        } else {
            return nil();
        }
    }

}
