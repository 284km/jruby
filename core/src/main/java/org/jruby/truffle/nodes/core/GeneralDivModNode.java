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

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.utilities.BranchProfile;
import org.jruby.truffle.runtime.RubyContext;
import org.jruby.truffle.runtime.core.RubyArray;
import org.jruby.truffle.runtime.core.RubyBignum;

import java.math.BigInteger;

public class GeneralDivModNode extends Node {

    private final RubyContext context;

    @Child protected FixnumOrBignumNode fixnumOrBignumQuotient;
    @Child protected FixnumOrBignumNode fixnumOrBignumRemainder;

    private final BranchProfile bZeroProfile = new BranchProfile();
    private final BranchProfile bMinusOneProfile = new BranchProfile();
    private final BranchProfile bigIntegerFixnumProfile = new BranchProfile();
    private final BranchProfile useFixnumPairProfile = new BranchProfile();
    private final BranchProfile useObjectPairProfile = new BranchProfile();

    public GeneralDivModNode(RubyContext context) {
        assert context != null;
        this.context = context;
        fixnumOrBignumQuotient = new FixnumOrBignumNode();
        fixnumOrBignumRemainder = new FixnumOrBignumNode();
    }

    public RubyArray execute(int a, int b) {
        return divMod(a, b);
    }

    public RubyArray execute(int a, long b) {
        return divMod(a, b);
    }

    public RubyArray execute(int a, BigInteger b) {
        return divMod(BigInteger.valueOf(a), b);
    }

    public RubyArray execute(long a, int b) {
        return divMod(a, b);
    }

    public RubyArray execute(long a, long b) {
        return divMod(a, b);
    }

    public RubyArray execute(long a, RubyBignum b) {
        return divMod(BigInteger.valueOf(a), b.bigIntegerValue());
    }

    public RubyArray execute(RubyBignum a, int b) {
        return divMod(a.bigIntegerValue(), BigInteger.valueOf(b));
    }

    public RubyArray execute(RubyBignum a, long b) {
        return divMod(a.bigIntegerValue(), BigInteger.valueOf(b));
    }

    public RubyArray execute(RubyBignum a, RubyBignum b) {
        return divMod(a.bigIntegerValue(), b.bigIntegerValue());
    }

    /*
     * div-mod algorithms copied from org.jruby.RubyFixnum and org.jruby.RubyBignum. See license and contributors there.
     */

    @CompilerDirectives.SlowPath
    private RubyArray divMod(long a, long b) {
        if (b == 0) {
            bZeroProfile.enter();
            throw new ArithmeticException("divide by zero");
        }

        long mod;
        Object integerDiv;

        if (b == -1) {
            bMinusOneProfile.enter();

            if (a == Long.MIN_VALUE) {
                integerDiv = BigInteger.valueOf(a).negate();
            } else {
                integerDiv = -a;
            }
            mod = 0;
        } else {
            long div = a / b;
            mod = a - b * div;
            if (mod < 0 && b > 0 || mod > 0 && b < 0) {
                div -= 1;
                mod += b;
            }
            integerDiv = div;
        }

        if (integerDiv instanceof Long && ((long) integerDiv) >= Integer.MIN_VALUE && ((long) integerDiv) <= Integer.MAX_VALUE && mod >= Integer.MIN_VALUE && mod <= Integer.MAX_VALUE) {
            useFixnumPairProfile.enter();
            return new RubyArray(context.getCoreLibrary().getArrayClass(), new int[]{(int) (long) integerDiv, (int) mod}, 2);
        } else if (integerDiv instanceof Long) {
            useObjectPairProfile.enter();
            return new RubyArray(context.getCoreLibrary().getArrayClass(), new Object[]{integerDiv, mod}, 2);
        } else {
            useObjectPairProfile.enter();
            return new RubyArray(context.getCoreLibrary().getArrayClass(), new Object[]{
                    fixnumOrBignumQuotient.fixnumOrBignum(create((BigInteger) integerDiv)),
                    mod}, 2);
        }
    }

    @CompilerDirectives.SlowPath
    private RubyArray divMod(BigInteger a, BigInteger b) {
        if (b.signum() == 0) {
            bZeroProfile.enter();
            throw new ArithmeticException("divide by zero");
        }

        final BigInteger[] bigIntegerResults = a.divideAndRemainder(a);

        if ((a.signum() * b.signum()) == -1 && bigIntegerResults[1].signum() != 0) {
            bigIntegerFixnumProfile.enter();
            bigIntegerResults[0] = bigIntegerResults[0].subtract(BigInteger.ONE);
            bigIntegerResults[1] = b.add(bigIntegerResults[1]);
        }

        return new RubyArray(context.getCoreLibrary().getArrayClass(), new Object[]{
                fixnumOrBignumQuotient.fixnumOrBignum(create(bigIntegerResults[0])),
                fixnumOrBignumRemainder.fixnumOrBignum(create(bigIntegerResults[1]))}, 2);
    }

    public RubyBignum create(BigInteger value) {
        return new RubyBignum(context.getCoreLibrary().getBignumClass(), value);
    }

}
