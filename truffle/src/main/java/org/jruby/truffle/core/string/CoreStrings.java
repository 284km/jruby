/*
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.core.string;

import org.jruby.truffle.RubyContext;

public class CoreStrings {

    public final CoreString ASSIGNMENT;
    public final CoreString CLASS_VARIABLE;
    public final CoreString EXPRESSION;
    public final CoreString FALSE;
    public final CoreString GLOBAL_VARIABLE;
    public final CoreString INSTANCE_VARIABLE;
    public final CoreString LOCAL_VARIABLE;
    public final CoreString METHOD;
    public final CoreString NIL;
    public final CoreString SELF;
    public final CoreString TRUE;
    public final CoreString YIELD;

    public CoreStrings(RubyContext context) {
        ASSIGNMENT = new CoreString(context, "assignment");
        CLASS_VARIABLE = new CoreString(context, "class variable");
        EXPRESSION = new CoreString(context, "expression");
        FALSE = new CoreString(context, "false");
        GLOBAL_VARIABLE = new CoreString(context, "global-variable");
        INSTANCE_VARIABLE = new CoreString(context, "instance-variable");
        LOCAL_VARIABLE = new CoreString(context, "local-variable");
        METHOD = new CoreString(context, "method");
        NIL = new CoreString(context, "nil");
        SELF = new CoreString(context, "self");
        TRUE = new CoreString(context, "true");
        YIELD = new CoreString(context, "yield");
    }

}
