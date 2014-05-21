/*
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.runtime.core;

import org.jruby.truffle.nodes.RubyNode;
import org.jruby.truffle.runtime.*;

/**
 * Represents the Ruby {@code NilClass} class.
 */
public class RubyNilClass extends RubyObject {

    public RubyNilClass(RubyClass rubyClass) {
        super(rubyClass);
    }

    /**
     * Given a reference, produce either {@code nil} or the object. .
     */
    public static Object instanceOrNil(Object object) {
        RubyNode.notDesignedForCompilation();

        if (object == null) {
            return NilPlaceholder.INSTANCE;
        } else {
            return object;
        }
    }

    @Override
    public boolean equals(Object other) {
        RubyNode.notDesignedForCompilation();

        return other instanceof RubyNilClass || other instanceof NilPlaceholder;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    public static boolean isNil(Object block) {
        RubyNode.notDesignedForCompilation();

        return block instanceof NilPlaceholder || block instanceof RubyNilClass;
    }

}
