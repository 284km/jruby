/*
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.core.exception;

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.source.SourceSection;
import org.jruby.truffle.Layouts;
import org.jruby.truffle.RubyContext;
import org.jruby.truffle.builtins.CoreClass;
import org.jruby.truffle.builtins.CoreMethod;
import org.jruby.truffle.builtins.CoreMethodArrayArgumentsNode;
import org.jruby.truffle.builtins.Primitive;
import org.jruby.truffle.builtins.PrimitiveArrayArgumentsNode;
import org.jruby.truffle.language.objects.AllocateObjectNode;
import org.jruby.truffle.language.objects.AllocateObjectNodeGen;

import static org.jruby.truffle.core.array.ArrayHelpers.createArray;

@CoreClass("NoMethodError")
public abstract class NoMethodErrorNodes {

    @CoreMethod(names = "allocate", constructor = true)
    public abstract static class AllocateNode extends CoreMethodArrayArgumentsNode {

        @Child private AllocateObjectNode allocateObjectNode;

        public AllocateNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
            allocateObjectNode = AllocateObjectNodeGen.create(context, sourceSection, null, null);
        }

        @Specialization
        public DynamicObject allocateNoMethodError(DynamicObject rubyClass) {
            return allocateObjectNode.allocate(rubyClass, nil(), null, null, nil(), createArray(getContext(), null, 0));
        }

    }

    @CoreMethod(names = "args")
    public abstract static class ArgsNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public Object args(DynamicObject self) {
            return Layouts.NO_METHOD_ERROR.getArgs(self);
        }

    }

    @Primitive(name = "no_method_error_set_args")
    public abstract static class ArgsSetNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        public Object setArgs(DynamicObject error, DynamicObject args) {
            Layouts.NO_METHOD_ERROR.setArgs(error, args);
            return args;
        }

    }


}
