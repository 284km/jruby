/*
 * Copyright (c) 2013, 2014 Oracle and/or its affiliates. All rights reserved. This
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
import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.utilities.*;
import org.jruby.truffle.runtime.*;
import org.jruby.truffle.runtime.core.*;
import org.jruby.truffle.runtime.control.*;
import org.jruby.truffle.runtime.core.RubyArray;
import org.jruby.truffle.runtime.util.SlowPathBigInteger;

@CoreClass(name = "Bignum")
public abstract class BignumNodes {

    @CoreMethod(names = "-@")
    public abstract static class NegNode extends CoreMethodNode {

        public NegNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public NegNode(NegNode prev) {
            super(prev);
        }

        @Specialization
        public BigInteger neg(BigInteger value) {
            return SlowPathBigInteger.negate(value);
        }

    }

    @CoreMethod(names = "+", required = 1)
    public abstract static class AddNode extends CoreMethodNode {

        @Child protected FixnumOrBignumNode fixnumOrBignum;

        public AddNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
            fixnumOrBignum = new FixnumOrBignumNode();
        }

        public AddNode(AddNode prev) {
            super(prev);
            fixnumOrBignum = prev.fixnumOrBignum;
        }

        @Specialization
        public Object add(BigInteger a, int b) {
            return SlowPathBigInteger.add(a, BigInteger.valueOf(b));
        }

        @Specialization
        public Object add(BigInteger a, long b) {
            return SlowPathBigInteger.add(a, BigInteger.valueOf(b));
        }

        @Specialization
        public double add(BigInteger a, double b) {
            return SlowPathBigInteger.doubleValue(a) + b;
        }

        @Specialization
        public Object add(BigInteger a, BigInteger b) {
            return fixnumOrBignum.fixnumOrBignum(SlowPathBigInteger.add(a, b));
        }

    }

    @CoreMethod(names = "-", required = 1)
    public abstract static class SubNode extends CoreMethodNode {

        @Child protected FixnumOrBignumNode fixnumOrBignum;

        public SubNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
            fixnumOrBignum = new FixnumOrBignumNode();
        }

        public SubNode(SubNode prev) {
            super(prev);
            fixnumOrBignum = prev.fixnumOrBignum;
        }

        @Specialization
        public Object sub(BigInteger a, int b) {
            return SlowPathBigInteger.subtract(a, BigInteger.valueOf(b));
        }

        @Specialization
        public Object sub(BigInteger a, long b) {
            return SlowPathBigInteger.subtract(a, BigInteger.valueOf(b));
        }

        @Specialization
        public double sub(BigInteger a, double b) {
            return SlowPathBigInteger.doubleValue(a) - b;
        }

        @Specialization
        public Object sub(BigInteger a, BigInteger b) {
            return fixnumOrBignum.fixnumOrBignum(SlowPathBigInteger.subtract(a, b));
        }

    }

    @CoreMethod(names = "*", required = 1)
    public abstract static class MulNode extends CoreMethodNode {

        public MulNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public MulNode(MulNode prev) {
            super(prev);
        }

        @Specialization
        public Object mul(BigInteger a, int b) {
            return SlowPathBigInteger.multiply(a, BigInteger.valueOf(b));
        }

        @Specialization
        public Object mul(BigInteger a, long b) {
            return SlowPathBigInteger.multiply(a, BigInteger.valueOf(b));
        }

        @Specialization
        public double mul(BigInteger a, double b) {
            return SlowPathBigInteger.doubleValue(a) * b;
        }

        @Specialization
        public Object mul(BigInteger a, BigInteger b) {
            return RubyFixnum.fixnumOrBignum(SlowPathBigInteger.multiply(a, b));
        }

    }

    @CoreMethod(names = "**", required = 1)
    public abstract static class PowNode extends CoreMethodNode {

        public PowNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public PowNode(PowNode prev) {
            super(prev);
        }

        @CompilerDirectives.SlowPath
        @Specialization
        public BigInteger pow(BigInteger a, int b) {
            return SlowPathBigInteger.pow(a, b);
        }

        @Specialization
        public double pow(BigInteger a, double b) {
            return Math.pow(SlowPathBigInteger.doubleValue(a), b);
        }

        @Specialization
        public BigInteger pow(BigInteger a, BigInteger b) {
            notDesignedForCompilation();

            BigInteger result = BigInteger.ONE;

            for (BigInteger n = BigInteger.ZERO; b.compareTo(n) < 0; n = n.add(BigInteger.ONE)) {
                result = SlowPathBigInteger.multiply(result, a);
            }

            return result;
        }

    }

    @CoreMethod(names = "/", required = 1)
    public abstract static class DivNode extends CoreMethodNode {

        public DivNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public DivNode(DivNode prev) {
            super(prev);
        }

        @Specialization
        public Object div(BigInteger a, int b) {
            return SlowPathBigInteger.divide(a, BigInteger.valueOf(b));
        }

        @Specialization
        public Object div(BigInteger a, long b) {
            return SlowPathBigInteger.divide(a, BigInteger.valueOf(b));
        }

        @Specialization
        public double div(BigInteger a, double b) {
            return SlowPathBigInteger.doubleValue(a) / b;
        }

        @Specialization
        public Object div(BigInteger a, BigInteger b) {
            return RubyFixnum.fixnumOrBignum(SlowPathBigInteger.divide(a, b));
        }

    }

    @CoreMethod(names = "%", required = 1)
    public abstract static class ModNode extends CoreMethodNode {

        public ModNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public ModNode(ModNode prev) {
            super(prev);
        }

        @Specialization
        public Object mod(BigInteger a, int b) {
            return RubyFixnum.fixnumOrBignum(SlowPathBigInteger.mod(a, BigInteger.valueOf(b)));
        }

        @Specialization
        public Object mod(BigInteger a, long b) {
            return RubyFixnum.fixnumOrBignum(SlowPathBigInteger.mod(a, BigInteger.valueOf(b)));
        }

        @Specialization
        public Object mod(BigInteger a, BigInteger b) {
            return RubyFixnum.fixnumOrBignum(SlowPathBigInteger.mod(a, b));
        }

    }

    @CoreMethod(names = "divmod", required = 1)
    public abstract static class DivModNode extends CoreMethodNode {

        @Child protected GeneralDivModNode divModNode;

        public DivModNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
            divModNode = new GeneralDivModNode(context);
        }

        public DivModNode(DivModNode prev) {
            super(prev);
            divModNode = new GeneralDivModNode(getContext());
        }

        @Specialization
        public RubyArray divMod(BigInteger a, int b) {
            return divModNode.execute(a, b);
        }

        @Specialization
        public RubyArray divMod(BigInteger a, long b) {
            return divModNode.execute(a, b);
        }

        @Specialization
        public RubyArray divMod(BigInteger a, BigInteger b) {
            return divModNode.execute(a, b);
        }

    }

    @CoreMethod(names = "<", required = 1)
    public abstract static class LessNode extends CoreMethodNode {

        public LessNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public LessNode(LessNode prev) {
            super(prev);
        }

        @Specialization
        public boolean less(BigInteger a, int b) {
            return SlowPathBigInteger.compareTo(a, BigInteger.valueOf(b)) < 0;
        }

        @Specialization
        public boolean less(BigInteger a, long b) {
            return SlowPathBigInteger.compareTo(a, BigInteger.valueOf(b)) < 0;
        }

        @Specialization
        public boolean less(BigInteger a, double b) {
            return a.compareTo(BigInteger.valueOf((long) b)) < 0;
        }

        @Specialization
        public boolean less(BigInteger a, BigInteger b) {
            return a.compareTo(b) < 0;
        }
    }

    @CoreMethod(names = "<=", required = 1)
    public abstract static class LessEqualNode extends CoreMethodNode {

        public LessEqualNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public LessEqualNode(LessEqualNode prev) {
            super(prev);
        }

        @Specialization
        public boolean lessEqual(BigInteger a, int b) {
            return SlowPathBigInteger.compareTo(a, BigInteger.valueOf(b)) <= 0;
        }

        @Specialization
        public boolean lessEqual(BigInteger a, long b) {
            return SlowPathBigInteger.compareTo(a, BigInteger.valueOf(b)) <= 0;
        }

        @Specialization
        public boolean lessEqual(BigInteger a, double b) {
            return a.compareTo(BigInteger.valueOf((long) b)) <= 0;
        }

        @Specialization
        public boolean lessEqual(BigInteger a, BigInteger b) {
            return a.compareTo(b) <= 0;
        }
    }

    @CoreMethod(names = {"==", "eql?"}, required = 1)
    public abstract static class EqualNode extends CoreMethodNode {

        public EqualNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public EqualNode(EqualNode prev) {
            super(prev);
        }

        @Specialization
        public boolean equal(BigInteger a, int b) {
            return a.equals(BigInteger.valueOf(b));
        }

        @Specialization
        public boolean equal(BigInteger a, long b) {
            return a.equals(BigInteger.valueOf(b));
        }

        @Specialization
        public boolean equal(BigInteger a, double b) {
            return a.equals(BigInteger.valueOf((long) b));
        }

        @Specialization
        public boolean equal(BigInteger a, BigInteger b) {
            return a.equals(b);
        }
    }

    @CoreMethod(names = "<=>", required = 1)
    public abstract static class CompareNode extends CoreMethodNode {

        public CompareNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public CompareNode(CompareNode prev) {
            super(prev);
        }

        @Specialization
        public int compare(BigInteger a, int b) {
            return SlowPathBigInteger.compareTo(a, BigInteger.valueOf(b));
        }

        @Specialization
        public int compare(BigInteger a, long b) {
            return SlowPathBigInteger.compareTo(a, BigInteger.valueOf(b));
        }

        @Specialization
        public int compare(BigInteger a, double b) {
            return a.compareTo(BigInteger.valueOf((long) b));
        }

        @Specialization
        public int compare(BigInteger a, BigInteger b) {
            return a.compareTo(b);
        }
    }

    @CoreMethod(names = ">=", required = 1)
    public abstract static class GreaterEqualNode extends CoreMethodNode {

        public GreaterEqualNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public GreaterEqualNode(GreaterEqualNode prev) {
            super(prev);
        }

        @Specialization
        public boolean greaterEqual(BigInteger a, int b) {
            return SlowPathBigInteger.compareTo(a, BigInteger.valueOf(b)) >= 0;
        }

        @Specialization
        public boolean greaterEqual(BigInteger a, long b) {
            return SlowPathBigInteger.compareTo(a, BigInteger.valueOf(b)) >= 0;
        }

        @Specialization
        public boolean greaterEqual(BigInteger a, double b) {
            return a.compareTo(BigInteger.valueOf((long) b)) >= 0;
        }

        @Specialization
        public boolean greaterEqual(BigInteger a, BigInteger b) {
            return a.compareTo(b) >= 0;
        }
    }

    @CoreMethod(names = ">", required = 1)
    public abstract static class GreaterNode extends CoreMethodNode {

        public GreaterNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public GreaterNode(GreaterNode prev) {
            super(prev);
        }

        @Specialization
        public boolean greater(BigInteger a, int b) {
            return SlowPathBigInteger.compareTo(a, BigInteger.valueOf(b)) > 0;
        }

        @Specialization
        public boolean greater(BigInteger a, long b) {
            return SlowPathBigInteger.compareTo(a, BigInteger.valueOf(b)) > 0;
        }

        @Specialization
        public boolean greater(BigInteger a, double b) {
            return a.compareTo(BigInteger.valueOf((long) b)) > 0;
        }

        @Specialization
        public boolean greater(BigInteger a, BigInteger b) {
            return a.compareTo(b) > 0;
        }
    }

    @CoreMethod(names = "&", required = 1)
    public abstract static class BitAndNode extends CoreMethodNode {

        private final FixnumOrBignumNode fixnumOrBignumNode;

        public BitAndNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
            fixnumOrBignumNode = new FixnumOrBignumNode();
        }

        public BitAndNode(BitAndNode prev) {
            super(prev);
            fixnumOrBignumNode = prev.fixnumOrBignumNode;
        }

        @Specialization
        public Object bitAnd(BigInteger a, int b) {
            return fixnumOrBignumNode.fixnumOrBignum(SlowPathBigInteger.and(a, BigInteger.valueOf(b)));
        }

        @Specialization
        public Object bitAnd(BigInteger a, long b) {
            return fixnumOrBignumNode.fixnumOrBignum(SlowPathBigInteger.and(a, BigInteger.valueOf(b)));
        }

        @Specialization
        public Object bitAnd(BigInteger a, BigInteger b) {
            return fixnumOrBignumNode.fixnumOrBignum(SlowPathBigInteger.and(a, b));
        }
    }

    @CoreMethod(names = "|", required = 1)
    public abstract static class BitOrNode extends CoreMethodNode {

        private final FixnumOrBignumNode fixnumOrBignumNode;

        public BitOrNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
            fixnumOrBignumNode = new FixnumOrBignumNode();
        }

        public BitOrNode(BitOrNode prev) {
            super(prev);
            fixnumOrBignumNode = prev.fixnumOrBignumNode;
        }

        @Specialization
        public Object bitOr(BigInteger a, int b) {
            return fixnumOrBignumNode.fixnumOrBignum(SlowPathBigInteger.or(a, BigInteger.valueOf(b)));
        }

        @Specialization
        public Object bitOr(BigInteger a, long b) {
            return fixnumOrBignumNode.fixnumOrBignum(SlowPathBigInteger.or(a, BigInteger.valueOf(b)));
        }

        @Specialization
        public Object bitOr(BigInteger a, BigInteger b) {
            return fixnumOrBignumNode.fixnumOrBignum(SlowPathBigInteger.or(a, b));
        }
    }

    @CoreMethod(names = "^", required = 1)
    public abstract static class BitXOrNode extends CoreMethodNode {

        private final FixnumOrBignumNode fixnumOrBignumNode;

        public BitXOrNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
            fixnumOrBignumNode = new FixnumOrBignumNode();
        }

        public BitXOrNode(BitXOrNode prev) {
            super(prev);
            fixnumOrBignumNode = prev.fixnumOrBignumNode;
        }

        @Specialization
        public Object bitXOr(BigInteger a, int b) {
            return fixnumOrBignumNode.fixnumOrBignum(SlowPathBigInteger.xor(a, BigInteger.valueOf(b)));
        }

        @Specialization
        public Object bitXOr(BigInteger a, long b) {
            return fixnumOrBignumNode.fixnumOrBignum(SlowPathBigInteger.xor(a, BigInteger.valueOf(b)));
        }

        @Specialization
        public Object bitXOr(BigInteger a, BigInteger b) {
            return fixnumOrBignumNode.fixnumOrBignum(SlowPathBigInteger.xor(a, b));
        }
    }

    @CoreMethod(names = "<<", required = 1)
    public abstract static class LeftShiftNode extends CoreMethodNode {

        @Child protected FixnumOrBignumNode fixnumOrBignum;

        private final BranchProfile bLessThanZero = new BranchProfile();

        public LeftShiftNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
            fixnumOrBignum = new FixnumOrBignumNode();
        }

        public LeftShiftNode(LeftShiftNode prev) {
            super(prev);
            fixnumOrBignum = prev.fixnumOrBignum;
        }

        @Specialization
        public Object leftShift(BigInteger a, int b) {
            if (b >= 0) {
                return fixnumOrBignum.fixnumOrBignum(SlowPathBigInteger.shiftLeft(a, b));
            } else {
                bLessThanZero.enter();
                return fixnumOrBignum.fixnumOrBignum(SlowPathBigInteger.shiftRight(a, -b));
            }
        }

    }

    @CoreMethod(names = ">>", required = 1)
    public abstract static class RightShiftNode extends CoreMethodNode {

        @Child protected FixnumOrBignumNode fixnumOrBignum;

        private final BranchProfile bLessThanZero = new BranchProfile();

        public RightShiftNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
            fixnumOrBignum = new FixnumOrBignumNode();
        }

        public RightShiftNode(RightShiftNode prev) {
            super(prev);
            fixnumOrBignum = prev.fixnumOrBignum;
        }

        @Specialization
        public Object leftShift(BigInteger a, int b) {
            if (b >= 0) {
                return fixnumOrBignum.fixnumOrBignum(SlowPathBigInteger.shiftRight(a, b));
            } else {
                bLessThanZero.enter();
                return fixnumOrBignum.fixnumOrBignum(SlowPathBigInteger.shiftLeft(a, -b));
            }
        }

    }

    @CoreMethod(names = "times", needsBlock = true)
    public abstract static class TimesNode extends YieldingCoreMethodNode {

        private final BranchProfile breakProfile = new BranchProfile();
        private final BranchProfile nextProfile = new BranchProfile();
        private final BranchProfile redoProfile = new BranchProfile();

        public TimesNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public TimesNode(TimesNode prev) {
            super(prev);
        }

        @Specialization
        public Object times(VirtualFrame frame, BigInteger n, RubyProc block) {
            notDesignedForCompilation();

            outer: for (BigInteger i = BigInteger.ZERO; i.compareTo(n) < 0; i = i.add(BigInteger.ONE)) {
                while (true) {
                    try {
                        yield(frame, block, i);
                        continue outer;
                    } catch (BreakException e) {
                        breakProfile.enter();
                        return e.getResult();
                    } catch (NextException e) {
                        nextProfile.enter();
                        continue outer;
                    } catch (RedoException e) {
                        redoProfile.enter();
                    }
                }
            }

            return n;
        }

    }

    @CoreMethod(names = {"to_s", "inspect"})
    public abstract static class ToSNode extends CoreMethodNode {

        public ToSNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public ToSNode(ToSNode prev) {
            super(prev);
        }

        @CompilerDirectives.SlowPath
        @Specialization
        public RubyString toS(BigInteger value) {
            return getContext().makeString(value.toString());
        }

    }

}
