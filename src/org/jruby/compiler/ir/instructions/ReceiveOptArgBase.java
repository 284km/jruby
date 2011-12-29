package org.jruby.compiler.ir.instructions;

import org.jruby.compiler.ir.Operation;
import org.jruby.compiler.ir.operands.UndefinedValue;
import org.jruby.compiler.ir.operands.Variable;
import org.jruby.compiler.ir.representations.InlinerInfo;
import org.jruby.runtime.builtin.IRubyObject;

/** 
 * Assign the 'index' argument to 'dest' which can be undefined since
 * this is an optional argument into the method/block.
 *
 * The specific instruction differs between Ruby language versions.
 */
public abstract class ReceiveOptArgBase extends ReceiveArgBase {
    public ReceiveOptArgBase(Variable result, int index) {
        super(Operation.RECV_OPT_ARG, result, index);
    }

    public abstract Object receiveOptArg(IRubyObject[] args);
}
