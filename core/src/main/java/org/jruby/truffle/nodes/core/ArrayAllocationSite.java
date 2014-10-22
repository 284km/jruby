package org.jruby.truffle.nodes.core;

import com.oracle.truffle.api.Assumption;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.Truffle;
import org.jruby.truffle.runtime.RubyContext;

public class ArrayAllocationSite {

    @CompilerDirectives.CompilationFinal private boolean convertedIntToLong = false;
    private final Assumption assumption = Truffle.getRuntime().createAssumption();

    @CompilerDirectives.TruffleBoundary
    public void convertedIntToLong() {
        if (RubyContext.ARRAYS_OPTIMISTIC_LONG) {
            convertedIntToLong = true;
            assumption.invalidate();
        }
    }

    public boolean hasConvertedIntToLong() {
        if (RubyContext.ARRAYS_OPTIMISTIC_LONG) {
            assumption.isValid();
            return convertedIntToLong;
        } else {
            return false;
        }
    }

}
