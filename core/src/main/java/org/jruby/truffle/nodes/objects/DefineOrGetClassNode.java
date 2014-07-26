/*
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.nodes.objects;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.nodes.UnexpectedResultException;
import org.jruby.truffle.nodes.*;
import org.jruby.truffle.runtime.*;
import org.jruby.truffle.runtime.control.*;
import org.jruby.truffle.runtime.core.*;

/**
 * Define a new class, or get the existing one of the same name.
 */
public class DefineOrGetClassNode extends RubyNode {

    private final String name;
    @Child protected RubyNode parentModule;
    @Child protected RubyNode superClass;

    public DefineOrGetClassNode(RubyContext context, SourceSection sourceSection, String name, RubyNode parentModule, RubyNode superClass) {
        super(context, sourceSection);
        this.name = name;
        this.parentModule = parentModule;
        this.superClass = superClass;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        notDesignedForCompilation();

        final RubyContext context = getContext();

        RubyModule parentModuleObject;

        try {
            parentModuleObject = parentModule.executeRubyModule(frame);
        } catch (UnexpectedResultException e) {
            throw new RaiseException(context.getCoreLibrary().typeErrorIsNotA(e.getResult().toString(), "module", this));
        }

        // Look for a current definition of the class, or create a new one

        final RubyModule.RubyConstant constant = parentModuleObject.lookupConstant(name);

        RubyClass definingClass;

        if (constant == null) {
            final RubyClass superClassObject = (RubyClass) superClass.execute(frame);

            if (superClassObject instanceof RubyException.RubyExceptionClass) {
                definingClass = new RubyException.RubyExceptionClass(superClassObject, name);
            } else if (superClassObject instanceof RubyString.RubyStringClass) {
                definingClass = new RubyString.RubyStringClass(superClassObject);
            } else {
                definingClass = new RubyClass(this, parentModuleObject, superClassObject, name);
            }

            parentModuleObject.setConstant(this, name, definingClass);
            parentModuleObject.getSingletonClass(this).setConstant(this, name, definingClass);
        } else {
            if (constant.value instanceof RubyClass) {
                definingClass = (RubyClass) constant.value;
            } else {
                throw new RaiseException(context.getCoreLibrary().typeErrorIsNotA(constant.value.toString(), "class", this));
            }
        }

        return definingClass;
    }
}
