/*
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.nodes.core;

import java.math.*;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.source.*;
import com.oracle.truffle.api.dsl.*;
import com.oracle.truffle.api.utilities.BranchProfile;
import org.jruby.truffle.runtime.*;
import org.jruby.truffle.runtime.core.*;
import org.jruby.truffle.runtime.core.RubyArray;
import org.jruby.truffle.runtime.util.SlowPathBigInteger;

@CoreClass(name = "Float")
public abstract class FloatNodes {

    @CoreMethod(names = "+@", maxArgs = 0)
    public abstract static class PosNode extends CoreMethodNode {

        public PosNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public PosNode(PosNode prev) {
            super(prev);
        }

        @Specialization
        public double pos(double value) {
            return value;
        }

    }

    @CoreMethod(names = "-@", maxArgs = 0)
    public abstract static class NegNode extends CoreMethodNode {

        public NegNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public NegNode(NegNode prev) {
            super(prev);
        }

        @Specialization
        public double neg(double value) {
            return -value;
        }

    }

    @CoreMethod(names = "+", minArgs = 1, maxArgs = 1)
    public abstract static class AddNode extends CoreMethodNode {

        public AddNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public AddNode(AddNode prev) {
            super(prev);
        }

        @Specialization
        public double add(double a, int b) {
            return a + b;
        }

        @Specialization
        public double add(double a, long b) {
            return a + b;
        }

        @Specialization
        public double add(double a, double b) {
            return a + b;
        }

        @Specialization
        public double add(double a, BigInteger b) {
            return a + SlowPathBigInteger.doubleValue(b);
        }

    }

    @CoreMethod(names = "-", minArgs = 1, maxArgs = 1)
    public abstract static class SubNode extends CoreMethodNode {

        public SubNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public SubNode(SubNode prev) {
            super(prev);
        }

        @Specialization
        public double sub(double a, int b) {
            return a - b;
        }

        @Specialization
        public double sub(double a, long b) {
            return a - b;
        }

        @Specialization
        public double sub(double a, double b) {
            return a - b;
        }

        @Specialization
        public double sub(double a, BigInteger b) {
            return a - SlowPathBigInteger.doubleValue(b);
        }

    }

    @CoreMethod(names = "*", minArgs = 1, maxArgs = 1)
    public abstract static class MulNode extends CoreMethodNode {

        public MulNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public MulNode(MulNode prev) {
            super(prev);
        }

        @Specialization
        public double mul(double a, int b) {
            return a * b;
        }

        @Specialization
        public double mul(double a, long b) {
            return a * b;
        }

        @Specialization
        public double mul(double a, double b) {
            return a * b;
        }

        @Specialization
        public double mul(double a, BigInteger b) {
            return a * SlowPathBigInteger.doubleValue(b);
        }

    }

    @CoreMethod(names = "**", minArgs = 1, maxArgs = 1)
    public abstract static class PowNode extends CoreMethodNode {

        public PowNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public PowNode(PowNode prev) {
            super(prev);
        }

        @Specialization
        public double pow(double a, int b) {
            return Math.pow(a, b);
        }

        @Specialization
        public double pow(double a, long b) {
            return Math.pow(a, b);
        }

        @Specialization
        public double pow(double a, double b) {
            return Math.pow(a, b);
        }

        @Specialization
        public double pow(double a, BigInteger b) {
            return Math.pow(a, SlowPathBigInteger.doubleValue(b));
        }

    }

    @CoreMethod(names = "/", minArgs = 1, maxArgs = 1)
    public abstract static class DivNode extends CoreMethodNode {

        public DivNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public DivNode(DivNode prev) {
            super(prev);
        }

        @Specialization
        public double div(double a, int b) {
            return a / b;
        }

        @Specialization
        public double div(double a, long b) {
            return a / b;
        }

        @Specialization
        public double div(double a, double b) {
            return a / b;
        }

        @Specialization
        public double div(double a, BigInteger b) {
            return a / SlowPathBigInteger.doubleValue(b);
        }

    }

    @CoreMethod(names = "%", minArgs = 1, maxArgs = 1)
    public abstract static class ModNode extends CoreMethodNode {

        public ModNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public ModNode(ModNode prev) {
            super(prev);
        }

        @Specialization
        public double mod(@SuppressWarnings("unused") double a, @SuppressWarnings("unused") int b) {
            throw new UnsupportedOperationException();
        }

        @Specialization
        public double mod(@SuppressWarnings("unused") double a, @SuppressWarnings("unused") long b) {
            throw new UnsupportedOperationException();
        }

        @Specialization
        public double mod(@SuppressWarnings("unused") double a, @SuppressWarnings("unused") double b) {
            throw new UnsupportedOperationException();
        }

        @Specialization
        public double mod(@SuppressWarnings("unused") double a, @SuppressWarnings("unused") BigInteger b) {
            throw new UnsupportedOperationException();
        }

    }

    @CoreMethod(names = "divmod", minArgs = 1, maxArgs = 1)
    public abstract static class DivModNode extends CoreMethodNode {

        public DivModNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public DivModNode(DivModNode prev) {
            super(prev);
        }

        @Specialization
        public RubyArray divMod(@SuppressWarnings("unused") double a, @SuppressWarnings("unused") int b) {
            throw new UnsupportedOperationException();
        }

        @Specialization
        public RubyArray divMod(@SuppressWarnings("unused") double a, @SuppressWarnings("unused") long b) {
            throw new UnsupportedOperationException();
        }

        @Specialization
        public RubyArray divMod(@SuppressWarnings("unused") double a, @SuppressWarnings("unused") double b) {
            throw new UnsupportedOperationException();
        }

        @Specialization
        public RubyArray divMod(@SuppressWarnings("unused") double a, @SuppressWarnings("unused") BigInteger b) {
            throw new UnsupportedOperationException();
        }

    }

    @CoreMethod(names = "<", minArgs = 1, maxArgs = 1)
    public abstract static class LessNode extends CoreMethodNode {

        public LessNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public LessNode(LessNode prev) {
            super(prev);
        }

        @Specialization
        public boolean less(double a, int b) {
            return a < b;
        }

        @Specialization
        public boolean less(double a, long b) {
            return a < b;
        }

        @Specialization
        public boolean less(double a, double b) {
            return a < b;
        }

        @Specialization
        public boolean less(double a, BigInteger b) {
            return a < SlowPathBigInteger.doubleValue(b);
        }
    }

    @CoreMethod(names = "<=", minArgs = 1, maxArgs = 1)
    public abstract static class LessEqualNode extends CoreMethodNode {

        public LessEqualNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public LessEqualNode(LessEqualNode prev) {
            super(prev);
        }

        @Specialization
        public boolean lessEqual(double a, int b) {
            return a <= b;
        }

        @Specialization
        public boolean lessEqual(double a, long b) {
            return a <= b;
        }

        @Specialization
        public boolean lessEqual(double a, double b) {
            return a <= b;
        }

        @Specialization
        public boolean lessEqual(double a, BigInteger b) {
            return a <= SlowPathBigInteger.doubleValue(b);
        }
    }

    @CoreMethod(names = "==", minArgs = 1, maxArgs = 1)
    public abstract static class EqualNode extends CoreMethodNode {

        public EqualNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public EqualNode(EqualNode prev) {
            super(prev);
        }

        @Specialization
        public boolean equal(double a, int b) {
            return a == b;
        }

        @Specialization
        public boolean equal(double a, long b) {
            return a == b;
        }

        @Specialization
        public boolean equal(double a, double b) {
            return a == b;
        }

        @Specialization
        public boolean equal(double a, BigInteger b) {
            return a == SlowPathBigInteger.doubleValue(b);
        }
    }

    @CoreMethod(names = "!=", minArgs = 1, maxArgs = 1)
    public abstract static class NotEqualNode extends CoreMethodNode {

        public NotEqualNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public NotEqualNode(NotEqualNode prev) {
            super(prev);
        }

        @Specialization
        public boolean notEqual(double a, int b) {
            return a != b;
        }

        @Specialization
        public boolean notEqual(double a, long b) {
            return a != b;
        }

        @Specialization
        public boolean notEqual(double a, double b) {
            return a != b;
        }

        @Specialization
        public boolean notEqual(double a, BigInteger b) {
            return a != SlowPathBigInteger.doubleValue(b);
        }
    }

    @CoreMethod(names = ">=", minArgs = 1, maxArgs = 1)
    public abstract static class GreaterEqualNode extends CoreMethodNode {

        public GreaterEqualNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public GreaterEqualNode(GreaterEqualNode prev) {
            super(prev);
        }

        @Specialization
        public boolean greaterEqual(double a, int b) {
            return a >= b;
        }

        @Specialization
        public boolean greaterEqual(double a, long b) {
            return a >= b;
        }

        @Specialization
        public boolean greaterEqual(double a, double b) {
            return a >= b;
        }

        @Specialization
        public boolean greaterEqual(double a, BigInteger b) {
            return a >= SlowPathBigInteger.doubleValue(b);
        }
    }

    @CoreMethod(names = ">", minArgs = 1, maxArgs = 1)
    public abstract static class GreaterNode extends CoreMethodNode {

        public GreaterNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public GreaterNode(GreaterNode prev) {
            super(prev);
        }

        @Specialization
        public boolean equal(double a, int b) {
            return a > b;
        }

        @Specialization
        public boolean equal(double a, long b) {
            return a > b;
        }

        @Specialization
        public boolean equal(double a, double b) {
            return a > b;
        }

        @Specialization
        public boolean equal(double a, BigInteger b) {
            return a > SlowPathBigInteger.doubleValue(b);
        }
    }

    @CoreMethod(names = "abs", maxArgs = 0)
    public abstract static class AbsNode extends CoreMethodNode {

        public AbsNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public AbsNode(AbsNode prev) {
            super(prev);
        }

        @Specialization
        public double abs(double n) {
            return Math.abs(n);
        }

    }

    @CoreMethod(names = "infinite?", maxArgs = 0)
    public abstract static class InfiniteNode extends CoreMethodNode {

        public InfiniteNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public InfiniteNode(InfiniteNode prev) {
            super(prev);
        }

        @Specialization
        public Object infinite(double value) {
            if (Double.isInfinite(value)) {
                if (value < 0) {
                    return -1;
                } else {
                    return 1;
                }
            } else {
                return getContext().getCoreLibrary().getNilObject();
            }
        }

    }

    @CoreMethod(names = "nan?", maxArgs = 0)
    public abstract static class NaNNode extends CoreMethodNode {

        public NaNNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public NaNNode(NaNNode prev) {
            super(prev);
        }

        @Specialization
        public boolean nan(double value) {
            return Double.isNaN(value);
        }

    }

    @CoreMethod(names = "nonzero?", maxArgs = 0)
    public abstract static class NonZeroNode extends CoreMethodNode {

        public NonZeroNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public NonZeroNode(NonZeroNode prev) {
            super(prev);
        }

        @Specialization
        public Object nonZero(double value) {
            if (value == 0) {
                return false;
            } else {
                return value;
            }
        }

    }

    @CoreMethod(names = "round", maxArgs = 0)
    public abstract static class RoundNode extends CoreMethodNode {

        @Child protected FixnumOrBignumNode fixnumOrBignum;

        private final BranchProfile greaterZero = BranchProfile.create();
        private final BranchProfile lessZero = BranchProfile.create();

        public RoundNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
            fixnumOrBignum = new FixnumOrBignumNode();
        }

        public RoundNode(RoundNode prev) {
            super(prev);
            fixnumOrBignum = prev.fixnumOrBignum;
        }

        @Specialization
        public Object round(double n) {
            // Algorithm copied from JRuby - not shared as we want to branch profile it

            double f = n;

            if (f > 0.0) {
                greaterZero.enter();

                f = Math.floor(f);

                if (n - f >= 0.5) {
                    f += 1.0;
                }
            } else if (f < 0.0) {
                lessZero.enter();

                f = Math.ceil(f);

                if (f - n >= 0.5) {
                    f -= 1.0;
                }
            }

            return fixnumOrBignum.fixnumOrBignum(f);
        }

    }

    @CoreMethod(names = "to_f", maxArgs = 0)
    public abstract static class ToFNode extends CoreMethodNode {

        public ToFNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public ToFNode(ToFNode prev) {
            super(prev);
        }

        @Specialization
        public double toF(double value) {
            return value;
        }

    }

    @CoreMethod(names = {"to_s", "inspect"}, maxArgs = 0)
    public abstract static class ToSNode extends CoreMethodNode {

        public ToSNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public ToSNode(ToSNode prev) {
            super(prev);
        }

        @CompilerDirectives.SlowPath
        @Specialization
        public RubyString toS(double value) {
            return getContext().makeString(Double.toString(value));
        }

    }

}
