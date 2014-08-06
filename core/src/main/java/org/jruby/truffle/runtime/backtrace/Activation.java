/*
 * Copyright (c) 2014 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.runtime.backtrace;

import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.nodes.Node;
import org.jruby.truffle.runtime.methods.RubyMethod;

public class Activation {

    final RubyMethod method;
    final Node callNode;
    final MaterializedFrame materializedFrame;

    public Activation(RubyMethod method, Node callNode, MaterializedFrame materializedFrame) {
        this.method = method;
        this.callNode = callNode;
        this.materializedFrame = materializedFrame;
    }

    public RubyMethod getMethod() {
        return method;
    }

    public Node getCallNode() {
        return callNode;
    }

    public MaterializedFrame getMaterializedFrame() {
        return materializedFrame;
    }
}
