/*
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.nodes;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.nodes.*;
import org.jruby.truffle.runtime.methods.RubyMethod;

/**
 * The root node in an AST for a method. Unlike {@link RubyNode}, this has a single entry point,
 * {@link #execute}, which Truffle knows about and can create a {@link CallTarget} from.
 */
public class RubyRootNode extends RootNode {

    // The method refers to root node, and vice versa, so this field is only compilation final and is set ex post to close the loop

    @CompilerDirectives.CompilationFinal private RubyMethod method;

    @Child protected RubyNode body;

    public RubyRootNode(SourceSection sourceSection, FrameDescriptor frameDescriptor, RubyNode body) {
        super(sourceSection, frameDescriptor);
        assert body != null;
        this.body = body;
    }

    public void setMethod(RubyMethod method) {
        assert this.method != null;
        this.method = method;
    }

    public RubyMethod getMethod() {
        return method;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        return body.execute(frame);
    }

    @Override
    public String toString() {
        final SourceSection sourceSection = getSourceSection();
        final String source = sourceSection == null ? "<unknown>" : sourceSection.toString();
        return "Method " + method.getName() + ":" + source + "@" + Integer.toHexString(hashCode());
    }

}
