/*
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.nodes.cast;

import com.oracle.truffle.api.source.*;
import com.oracle.truffle.api.dsl.*;
import com.oracle.truffle.api.frame.*;
import org.jruby.truffle.nodes.*;
import org.jruby.truffle.nodes.dispatch.DispatchHeadNode;
import org.jruby.truffle.runtime.*;
import org.jruby.truffle.runtime.core.*;

/**
 * Casts an object to a Ruby Proc object.
 */
@NodeChild("child")
public abstract class ProcCastNode extends RubyNode {

    @Child protected DispatchHeadNode toProc;

    public ProcCastNode(RubyContext context, SourceSection sourceSection) {
        super(context, sourceSection);
        toProc = new DispatchHeadNode(context);
    }

    public ProcCastNode(ProcCastNode prev) {
        super(prev);
        toProc = prev.toProc;
    }

    @Specialization
    public NilPlaceholder doNil(NilPlaceholder nil) {
        return nil;
    }

    @Specialization
    public RubyProc doRubyProc(RubyProc proc) {
        return proc;
    }

    @Specialization
    public RubyProc doObject(VirtualFrame frame, RubyBasicObject object) {
        notDesignedForCompilation();

        return (RubyProc) toProc.dispatch(frame, object, "to_proc", null);
    }

}
