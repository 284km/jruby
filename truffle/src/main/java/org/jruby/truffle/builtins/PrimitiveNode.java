/*
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */

package org.jruby.truffle.builtins;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.source.SourceSection;
import org.jruby.truffle.RubyContext;
import org.jruby.truffle.language.RubyNode;
import org.jruby.truffle.language.SourceIndexLength;

@GenerateNodeFactory
public abstract class PrimitiveNode extends RubyNode {

    protected static final Object FAILURE = null;

    public PrimitiveNode() {
    }

    public PrimitiveNode(RubyContext context, SourceSection sourceSection) {
        super(context, sourceSection);
    }

    public PrimitiveNode(RubyContext context, SourceIndexLength sourceSection) {
        super(context, sourceSection);
    }

}
