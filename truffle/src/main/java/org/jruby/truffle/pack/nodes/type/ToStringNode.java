package org.jruby.truffle.pack.nodes.type;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.utilities.ConditionProfile;
import org.jruby.truffle.nodes.dispatch.CallDispatchHeadNode;
import org.jruby.truffle.nodes.dispatch.DispatchHeadNodeFactory;
import org.jruby.truffle.nodes.dispatch.DispatchNode;
import org.jruby.truffle.nodes.dispatch.MissingBehavior;
import org.jruby.truffle.nodes.objects.IsTaintedNode;
import org.jruby.truffle.nodes.objects.IsTaintedNodeFactory;
import org.jruby.truffle.pack.nodes.PackNode;
import org.jruby.truffle.pack.runtime.Nil;
import org.jruby.truffle.pack.runtime.NoImplicitConversionException;
import org.jruby.truffle.runtime.RubyContext;
import org.jruby.truffle.runtime.core.RubyNilClass;
import org.jruby.truffle.runtime.core.RubyString;
import org.jruby.util.ByteList;

@NodeChildren({
        @NodeChild(value = "value", type = PackNode.class),
})
public abstract class ToStringNode extends PackNode {

    private final RubyContext context;

    @Child private CallDispatchHeadNode toStrNode;
    @Child private IsTaintedNode isTaintedNode;

    private final ConditionProfile taintedProfile = ConditionProfile.createBinaryProfile();

    public ToStringNode(RubyContext context) {
        this.context = context;
        isTaintedNode = IsTaintedNodeFactory.create(context, getEncapsulatingSourceSection(), null);
    }

    public ToStringNode(ToStringNode prev) {
        context = prev.context;
        toStrNode = prev.toStrNode;
        isTaintedNode = prev.isTaintedNode;
    }

    public abstract Object executeToString(VirtualFrame frame, Object object);

    @Specialization
    public Nil toString(VirtualFrame frame, RubyNilClass nil) {
        return Nil.INSTANCE;
    }

    @Specialization
    public ByteList toString(VirtualFrame frame, RubyString string) {
        if (taintedProfile.profile(isTaintedNode.executeIsTainted(string))) {
            setTainted(frame);
        }

        return string.getByteList();
    }

    @Specialization(guards = "!isRubyString(object)")
    public ByteList toString(VirtualFrame frame, Object object) {
        if (toStrNode == null) {
            CompilerDirectives.transferToInterpreter();
            toStrNode = insert(DispatchHeadNodeFactory.createMethodCall(context, true, MissingBehavior.RETURN_MISSING));
        }

        final Object value = toStrNode.call(frame, object, "to_str", null);

        if (value instanceof RubyString) {
            if (taintedProfile.profile(isTaintedNode.executeIsTainted(value))) {
                setTainted(frame);
            }

            return ((RubyString) value).getByteList();
        }

        CompilerDirectives.transferToInterpreter();

        if (value == DispatchNode.MISSING) {
            throw new NoImplicitConversionException(object, "String");
        }

        throw new NoImplicitConversionException(object, "String");
    }

}
