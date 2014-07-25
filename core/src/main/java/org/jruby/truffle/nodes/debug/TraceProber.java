/*
 * Copyright (c) 2013, 2014 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.nodes.debug;

import com.oracle.truffle.api.instrument.StandardSyntaxTag;
import com.oracle.truffle.api.instrument.SyntaxTag;
import com.oracle.truffle.api.nodes.Node;
import org.jruby.truffle.nodes.RubyNode;

public class TraceProber implements RubyNodeProber {

    @Override
    public RubyNode probeAsStatement(RubyNode node) {
        final RubyWrapper wrapper;

        if (node instanceof RubyWrapper) {
            wrapper = (RubyWrapper) node;
        } else {
            wrapper = new RubyWrapper(node.getContext(), node.getEncapsulatingSourceSection(), node);
        }

        wrapper.tagAs(StandardSyntaxTag.STATEMENT);
        wrapper.getProbe().addInstrument(new TraceInstrument(node.getContext(), node.getEncapsulatingSourceSection()));

        return wrapper;
    }

    @Override
    public RubyNode probeAsPeriodic(RubyNode node) {
        return node;
    }

    @Override
    public Node probeAs(Node node, SyntaxTag syntaxTag, Object... objects) {
        throw new UnsupportedOperationException();
    }

}
