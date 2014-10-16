/*
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.nodes.methods;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.source.*;
import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.nodes.*;
import org.jruby.truffle.nodes.*;
import org.jruby.truffle.runtime.*;
import org.jruby.truffle.runtime.core.*;

public class AliasNode extends RubyNode {

    @Child protected RubyNode module;
    final String newName;
    final String oldName;

    public AliasNode(RubyContext context, SourceSection sourceSection, RubyNode module, String newName, String oldName) {
        super(context, sourceSection);
        this.module = module;
        this.newName = newName;
        this.oldName = oldName;
    }

    @Override
    public void executeVoid(VirtualFrame frame) {
        notDesignedForCompilation();

        final Object object = module.execute(frame);

        if (object instanceof RubyModule) {
            // Module definition or class_eval
            ((RubyModule) object).alias(this, newName, oldName);
        } else {
            // instance_eval?
            ((RubyBasicObject) object).getSingletonClass(this).alias(this, newName, oldName);
        }
    }

    @Override
    public Object execute(VirtualFrame frame) {
        executeVoid(frame);
        return getContext().getCoreLibrary().getNilObject();
    }

}
