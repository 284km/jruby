package org.jruby.truffle.pack.nodes.type;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import org.jruby.runtime.builtin.IRubyObject;
import org.jruby.truffle.nodes.dispatch.CallDispatchHeadNode;
import org.jruby.truffle.nodes.dispatch.DispatchHeadNodeFactory;
import org.jruby.truffle.nodes.dispatch.DispatchNode;
import org.jruby.truffle.nodes.dispatch.MissingBehavior;
import org.jruby.truffle.pack.nodes.PackNode;
import org.jruby.truffle.pack.nodes.SourceNode;
import org.jruby.truffle.pack.runtime.CantConvertException;
import org.jruby.truffle.pack.runtime.NoImplicitConversionException;
import org.jruby.truffle.runtime.RubyContext;
import org.jruby.truffle.runtime.core.RubyBignum;
import org.jruby.truffle.runtime.core.RubyNilClass;

import java.math.BigInteger;

@NodeChildren({
        @NodeChild(value = "value", type = PackNode.class),
})
public abstract class ToLongNode extends PackNode {

    private final RubyContext context;

    @Child private CallDispatchHeadNode toIntNode;

    @CompilerDirectives.CompilationFinal private boolean seenInt;
    @CompilerDirectives.CompilationFinal private boolean seenLong;
    @CompilerDirectives.CompilationFinal private boolean seenBigInteger;

    public ToLongNode(RubyContext context) {
        this.context = context;
    }

    public ToLongNode(ToLongNode prev) {
        context = prev.context;
        toIntNode = prev.toIntNode;
        seenInt = prev.seenInt;
        seenLong = prev.seenLong;
    }

    public abstract long executeToLong(VirtualFrame frame, Object object);

    @Specialization
    public long toLong(VirtualFrame frame, boolean object) {
        CompilerDirectives.transferToInterpreter();
        throw new NoImplicitConversionException(object, "Integer");
    }

    @Specialization
    public long toLong(VirtualFrame frame, int object) {
        return object;
    }

    @Specialization
    public long toLong(VirtualFrame frame, long object) {
        return object;
    }

    @Specialization
    public long toLong(VirtualFrame frame, RubyBignum object) {
        // A truncated value is exactly what we want
        return object.bigIntegerValue().longValue();
    }

    @Specialization
    public long toLong(VirtualFrame frame, BigInteger object) {
        // A truncated value is exactly what we want
        return object.longValue();
    }

    @Specialization
    public long toLong(VirtualFrame frame, RubyNilClass nil) {
        CompilerDirectives.transferToInterpreter();
        throw new NoImplicitConversionException(nil, "Integer");
    }

    @Specialization(guards = {"!isBoolean(object)", "!isInteger(object)", "!isLong(object)", "!isBigInteger(object)", "!isRubyBignum(object)", "!isRubyNilClass(object)"})
    public long toLong(VirtualFrame frame, Object object) {
        if (toIntNode == null) {
            CompilerDirectives.transferToInterpreter();
            toIntNode = insert(DispatchHeadNodeFactory.createMethodCall(context, true, MissingBehavior.RETURN_MISSING));
        }

        final Object value = toIntNode.call(frame, object, "to_int", null);

        if (seenInt && value instanceof Integer) {
            return toLong(frame, (int) value);
        }

        if (seenLong && value instanceof Long) {
            return toLong(frame, (long) value);
        }

        if (seenBigInteger && value instanceof BigInteger) {
            return toLong(frame, (BigInteger) value);
        }

        CompilerDirectives.transferToInterpreterAndInvalidate();

        if (value == DispatchNode.MISSING) {
            throw new NoImplicitConversionException(object, "Integer");
        }

        if (value instanceof Integer) {
            seenInt = true;
            return toLong(frame, (int) value);
        }

        if (value instanceof Long) {
            seenLong = true;
            return toLong(frame, (long) value);
        }

        if (value instanceof BigInteger) {
            seenBigInteger = true;
            return toLong(frame, (BigInteger) value);
        }

        // TODO CS 5-April-15 missing the (Object#to_int gives String) part

        throw new CantConvertException("can't convert Object to Integer");
    }

}
