package org.jruby.compiler.ir.instructions;

import org.jruby.compiler.ir.Interp;
import org.jruby.compiler.ir.Operation;
import org.jruby.compiler.ir.operands.Variable;
import org.jruby.compiler.ir.representations.InlinerInfo;

/*
 * Assign rest arg passed into method to a result variable
 * The specific instruction differs between Ruby language versions
 */
public abstract class ReceiveRestArgBase extends ReceiveArgBase {
    public ReceiveRestArgBase(Variable result, int argIndex) {
        super(Operation.RECV_REST_ARG, result, argIndex);
    }
}
