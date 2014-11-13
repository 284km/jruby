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

import com.oracle.truffle.api.source.*;
import com.oracle.truffle.api.dsl.*;
import com.oracle.truffle.api.frame.*;
import org.jruby.truffle.runtime.*;
import org.jruby.truffle.runtime.core.*;

@CoreClass(name = "Binding")
public abstract class BindingNodes {

    @CoreMethod(names = "local_variable_get", required = 1)
    public abstract static class LocalVariableGetNode extends CoreMethodNode {

        public LocalVariableGetNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public LocalVariableGetNode(LocalVariableGetNode prev) {
            super(prev);
        }

        @Specialization
        public Object localVariableGet(RubyBinding binding, RubySymbol symbol) {
            notDesignedForCompilation();

            final MaterializedFrame frame = binding.getFrame();
            return frame.getValue(frame.getFrameDescriptor().findFrameSlot(symbol.toString()));
        }
    }

    @CoreMethod(names = "local_variable_set", required = 2)
    public abstract static class LocalVariableSetNode extends CoreMethodNode {

        public LocalVariableSetNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public LocalVariableSetNode(LocalVariableSetNode prev) {
            super(prev);
        }

        @Specialization
        public Object localVariableSetNode(RubyBinding binding, RubySymbol symbol, Object value) {
            notDesignedForCompilation();

            final MaterializedFrame frame = binding.getFrame();
            frame.setObject(frame.getFrameDescriptor().findFrameSlot(symbol.toString()), value);
            return value;
        }
    }

}
