/*
 * Copyright (c) 2014, 2015 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.language.backtrace;

import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.nodes.Node;

public class Activation {

    public static final Activation OMITTED_LIMIT = new Activation(null, null);
    public static final Activation OMITTED_UNUSED = new Activation(null, null);

    private final Node callNode;
    private final MaterializedFrame materializedFrame;

    public Activation(Node callNode, MaterializedFrame materializedFrame) {
        this.callNode = callNode;
        this.materializedFrame = materializedFrame;
    }

    public Node getCallNode() {
        return callNode;
    }

    public MaterializedFrame getMaterializedFrame() {
        return materializedFrame;
    }
}
