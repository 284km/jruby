/*
 * Copyright (c) 2013, 2014 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.runtime.core;

import org.jruby.truffle.nodes.RubyNode;
import org.jruby.truffle.runtime.subsystems.ObjectSpaceManager;

/**
 * Represents the Ruby {@code MatchData} class.
 */
public class RubyMatchData extends RubyObject {

    private final Object[] values;

    public RubyMatchData(RubyClass rubyClass, Object[] values) {
        super(rubyClass);
        this.values = values;
    }

    public Object[] valuesAt(int... indices) {
        RubyNode.notDesignedForCompilation();

        final Object[] result = new Object[indices.length];

        for (int n = 0; n < indices.length; n++) {
            result[n] = values[indices[n]];
        }

        return result;
    }

    public Object[] getValues() {
        return values;
    }

    @Override
    public void visitObjectGraphChildren(ObjectSpaceManager.ObjectGraphVisitor visitor) {
        for (Object object : values) {
            if (object instanceof RubyBasicObject) {
                ((RubyBasicObject) object).visitObjectGraph(visitor);
            }
        }
    }

}
