package org.jruby.ir.operands;

/**
 * This enum exists because we will frequently run into an arbitrary temporary variable
 * and we want to be able to quickly switch on type.
 */
public enum TemporaryVariableType {
    LOCAL, FLOAT, CLOSURE, CURRENT_MODULE, CURRENT_SCOPE
}
