/*
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.nodes;

import com.oracle.truffle.api.dsl.ImplicitCast;
import com.oracle.truffle.api.dsl.TypeSystem;
import org.jruby.truffle.runtime.NilPlaceholder;
import org.jruby.truffle.runtime.UndefinedPlaceholder;
import org.jruby.truffle.runtime.core.*;
import org.jruby.truffle.runtime.core.array.RubyArray;
import org.jruby.truffle.runtime.core.hash.RubyHash;
import org.jruby.truffle.runtime.core.range.IntegerFixnumRange;
import org.jruby.truffle.runtime.core.range.ObjectRange;
import org.jruby.truffle.runtime.core.range.RubyRange;
import org.jruby.truffle.runtime.core.RubyBasicObject;

import java.math.BigInteger;

/**
 * The list of types and type conversions that the AST interpreter knows about and can specialise
 * using. Used by the DSL.
 */
@TypeSystem({UndefinedPlaceholder.class, //
                NilPlaceholder.class, //
                boolean.class, //
                int.class, //
                long.class, //
                double.class, //
                BigInteger.class, //
                IntegerFixnumRange.class, //
                ObjectRange.class, //
                RubyArray.class, //
                RubyBignum.class, //
                RubyBinding.class, //
                RubyClass.class, //
                RubyContinuation.class, //
                RubyException.class, //
                RubyFiber.class, //
                RubyFile.class, //
                RubyFixnum.IntegerFixnum.class, //
                RubyFixnum.LongFixnum.class, //
                RubyFloat.class, //
                RubyHash.class, //
                RubyMatchData.class, //
                RubyModule.class, //
                RubyNilClass.class, //
                RubyProc.class, //
                RubyRange.class, //
                RubyRegexp.class, //
                RubyString.class, //
                RubyEncoding.class, //
                RubySymbol.class, //
                RubyThread.class, //
                RubyTime.class, //
                RubyTrueClass.class, //
                RubyFalseClass.class, //
                RubyObject.class, //
                RubyBasicObject.class, //
                Object[].class})
public class RubyTypes {

    @ImplicitCast
    public NilPlaceholder unboxNil(@SuppressWarnings("unused") RubyNilClass value) {
        return NilPlaceholder.INSTANCE;
    }

    @ImplicitCast
    public boolean unboxBoolean(RubyTrueClass value) {
        return true;
    }

    @ImplicitCast
    public boolean unboxBoolean(RubyFalseClass value) {
        return false;
    }

    @ImplicitCast
    public int unboxIntegerFixnum(RubyFixnum.IntegerFixnum value) {
        return value.getValue();
    }

    @ImplicitCast
    public long unboxLongFixnum(RubyFixnum.LongFixnum value) {
        return value.getValue();
    }

    @ImplicitCast
    public BigInteger unboxBignum(RubyBignum value) {
        return value.getValue();
    }

    @ImplicitCast
    public double unboxFloat(RubyFloat value) {
        return value.getValue();
    }

}
