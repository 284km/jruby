package org.jruby.ir.runtime;

import org.jruby.exceptions.Unrescuable;
import org.jruby.runtime.DynamicScope;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;

public class IRReturnJump extends RuntimeException implements Unrescuable {
    public DynamicScope methodToReturnFrom;
    public Object returnValue;

    private IRReturnJump() {}

    // See https://jira.codehaus.org/browse/JRUBY-6523
    // Dont use static threadlocals because they leak classloaders.
    // Instead, use soft/weak references so that GC can collect these.

    private static ThreadLocal<Reference<IRReturnJump>> threadLocalRJ = new ThreadLocal<Reference<IRReturnJump>>();

    public static IRReturnJump create(DynamicScope scope, Object rv) {
        IRReturnJump rj;
        Reference<IRReturnJump> rjRef = threadLocalRJ.get();
        if (rjRef == null || (rj = rjRef.get()) == null) {
            rj = new IRReturnJump();
            threadLocalRJ.set(new SoftReference<IRReturnJump>(rj));
        }
        rj.methodToReturnFrom = scope;
        rj.returnValue = rv;
        return rj;
    }

    @Override
    public String toString() {
        return "IRReturnJump:<" + methodToReturnFrom + ":" + returnValue + ">";
    }
}
