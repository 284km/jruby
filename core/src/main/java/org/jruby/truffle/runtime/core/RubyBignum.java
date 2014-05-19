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

import java.math.*;

/**
 * Represents the Ruby {@code Bignum} class.
 */
public class RubyBignum extends RubyObject implements Unboxable {

    private final BigInteger value;

    public RubyBignum(RubyClass bignumClass, BigInteger value) {
        super(bignumClass);

        assert value != null;

        this.value = value;
    }

    public BigInteger getValue() {
        return value;
    }

    @Override
    public Object unbox() {
        return value;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        RubyNode.notDesignedForCompilation();

        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof RubyBignum)) {
            return false;
        }
        RubyBignum other = (RubyBignum) obj;
        if (value == null) {
            if (other.value != null) {
                return false;
            }
        } else if (!value.equals(other.value)) {
            return false;
        }
        return true;
    }

}
