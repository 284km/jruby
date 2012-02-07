package org.jruby.compiler.ir.operands;

import java.math.BigInteger;
import org.jruby.compiler.ir.targets.JVM;
import org.jruby.runtime.ThreadContext;

/*
 * Represents a literal fixnum.
 * 
 * Cache value so that when the same Fixnum Operand is copy-propagated across
 * multiple instructions, the same RubyFixnum object is created.  In addition,
 * the same constant across loops should be the same object.
 * 
 * So, in this example, the output should be false, true, true
 *
 * <pre>
 *   n = 0
 *   olda = nil
 *   while (n < 3)
 *     a = 34853
 *     p a.equal?(olda)
 *     olda = a
 *     n += 1
 *   end
 * </pre>
 */      
public class Fixnum extends ImmutableLiteral {
    final public Long value;
    private Object rubyFixnum;

    public Fixnum(Long val) {
        value = val;
    }

    public Fixnum(BigInteger val) { 
        this(val.longValue());
    }
    
    @Override
    public Object createCacheObject(ThreadContext context) {
        return context.getRuntime().newFixnum(value);
    }    

    @Override
    public String toString() { 
        return value + ":fixnum";
    }

// ---------- These methods below are used during compile-time optimizations ------- 

    public Operand computeValue(String methodName, Operand arg) {
        if (arg instanceof Fixnum) {
            if (methodName.equals("+")) return new Fixnum(value + ((Fixnum)arg).value);
            if (methodName.equals("-")) return new Fixnum(value - ((Fixnum)arg).value);
            if (methodName.equals("*")) return new Fixnum(value * ((Fixnum)arg).value);
            if (methodName.equals("/")) {
                Long divisor = ((Fixnum)arg).value;
                return divisor == 0L ? null : new Fixnum(value / divisor); // If divisor is zero, don't simplify!
            }
        } else if (arg instanceof Float) {
            if (methodName.equals("+")) return new Float(value + ((Float)arg).value);
            if (methodName.equals("-")) return new Float(value - ((Float)arg).value);
            if (methodName.equals("*")) return new Float(value * ((Float)arg).value);
            if (methodName.equals("/")) {
                Double divisor = ((Float)arg).value;
                return divisor == 0.0 ? null : new Float(value / divisor); // If divisor is zero, don't simplify!
            }
        }

        return null;
    }

    @Override
    public void compile(JVM jvm) {
        jvm.method().push(value);
    }
}
