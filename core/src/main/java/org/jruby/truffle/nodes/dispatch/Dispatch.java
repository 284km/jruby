/*
 * Copyright (c) 2014 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.nodes.dispatch;

public class Dispatch {

    public static enum MissingBehavior {
        RETURN_MISSING,
        CALL_CONST_MISSING,
        CALL_METHOD_MISSING
    }

    public static enum DispatchAction {
        READ_CONSTANT(false),
        CALL(true),
        RESPOND(true);

        private final boolean isCall;

        private DispatchAction(boolean isCall) {
            this.isCall = isCall;
        }

        public boolean isCall() {
            return isCall;
        }
    }

    public static final Object MISSING = new Object();

}
