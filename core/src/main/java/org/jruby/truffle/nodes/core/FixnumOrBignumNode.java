/*
 * Copyright (c) 2014 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.nodes.core;

import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.utilities.BranchProfile;
import org.jruby.truffle.runtime.RubyContext;
import org.jruby.truffle.runtime.core.RubyBignum;

import java.math.BigDecimal;
import java.math.BigInteger;

public class FixnumOrBignumNode extends Node {

    public FixnumOrBignumNode(RubyContext context) {
        this.context = context;
    }

    private final RubyContext context;

    private final BranchProfile lowerProfile = BranchProfile.create();
    private final BranchProfile integerFromBignumProfile = BranchProfile.create();
    private final BranchProfile longFromBignumProfile = BranchProfile.create();

    private final BranchProfile integerFromDoubleProfile = BranchProfile.create();
    private final BranchProfile longFromDoubleProfile = BranchProfile.create();

    private final BranchProfile bignumProfile = BranchProfile.create();
    private final BranchProfile checkLongProfile = BranchProfile.create();

    public Object fixnumOrBignum(RubyBignum value) {
        if (value.compareTo(Long.MIN_VALUE) >= 0 && value.compareTo(Long.MAX_VALUE) <= 0) {
            lowerProfile.enter();

            final long longValue = value.longValue();

            if (longValue >= Integer.MIN_VALUE && longValue <= Integer.MAX_VALUE) {
                integerFromBignumProfile.enter();
                return (int) longValue;
            } else {
                longFromBignumProfile.enter();
                return longValue;
            }
        } else {
            return value;
        }
    }

    public Object fixnumOrBignum(double value) {
        if (value > Integer.MIN_VALUE && value < Integer.MAX_VALUE) {
            integerFromDoubleProfile.enter();
            return (int) value;
        }

        checkLongProfile.enter();

        if (value > Long.MIN_VALUE && value < Long.MAX_VALUE) {
            longFromDoubleProfile.enter();
            return (long) value;
        }

        bignumProfile.enter();

        return new RubyBignum(context.getCoreLibrary().getBignumClass(), new BigDecimal(value).toBigInteger());
    }

}
