/*
 * Copyright (c) 2013, 2014 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.nodes.supercall;

import com.oracle.truffle.api.Assumption;
import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.source.SourceSection;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.IndirectCallNode;
import org.jruby.common.IRubyWarnings;
import org.jruby.truffle.nodes.RubyNode;
import org.jruby.truffle.runtime.core.*;
import org.jruby.truffle.runtime.RubyArguments;
import org.jruby.truffle.runtime.RubyContext;
import org.jruby.truffle.runtime.control.RaiseException;
import org.jruby.truffle.runtime.core.RubyBasicObject;
import org.jruby.truffle.runtime.core.RubyClass;
import org.jruby.truffle.runtime.core.RubyModule;
import org.jruby.truffle.runtime.methods.RubyMethod;

public class GeneralSuperReCallNode extends AbstractGeneralSuperCallNode {

    private final boolean inBlock;

    public GeneralSuperReCallNode(RubyContext context, SourceSection sourceSection, String name, boolean inBlock) {
        super(context, sourceSection, name);
        this.inBlock = inBlock;
    }

    @ExplodeLoop
    @Override
    public final Object execute(VirtualFrame frame) {
        if (!guard()) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            lookup();
        }

        final Object[] superArguments;

        if (inBlock) {
            superArguments = RubyArguments.getDeclarationFrame(frame.getArguments()).getArguments();
        } else {
            superArguments = frame.getArguments();
        }

        return callNode.call(frame, superArguments);
    }

}
