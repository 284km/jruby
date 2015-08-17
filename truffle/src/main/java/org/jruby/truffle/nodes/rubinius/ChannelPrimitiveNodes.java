/*
 * Copyright (c) 2015 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.nodes.rubinius;

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.source.SourceSection;
import org.jruby.truffle.nodes.core.ClassNodes;
import org.jruby.truffle.runtime.RubyContext;

public abstract class ChannelPrimitiveNodes {

    @RubiniusPrimitive(name = "channel_new")
    public static abstract class ChannelNewPrimitiveNode extends RubiniusPrimitiveNode {

        public ChannelNewPrimitiveNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        @Specialization
        public DynamicObject channelNew() {
            final DynamicObject channelClass = getContext().getCoreLibrary().getRubiniusChannelClass();
            return ClassNodes.CLASS_LAYOUT.getInstanceFactory(channelClass).newInstance();
        }

    }

}
