/*
 * Copyright (c) 2013, 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.core.hash;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.source.SourceSection;
import org.jruby.truffle.RubyContext;
import org.jruby.truffle.core.basicobject.BasicObjectForeignAccessFactory;
import org.jruby.truffle.interop.InteropNode;
import org.jruby.truffle.interop.RubyInteropRootNode;

public class HashForeignAccessFactory extends BasicObjectForeignAccessFactory {

    public HashForeignAccessFactory(RubyContext context) {
        super(context);
    }

    @Override
    public CallTarget accessHasSize() {
        return Truffle.getRuntime().createCallTarget(new RubyInteropRootNode(InteropNode.createHasSizePropertyTrue(context, SourceSection.createUnavailable("", ""))));
    }

    @Override
    public CallTarget accessGetSize() {
        return Truffle.getRuntime().createCallTarget(new RubyInteropRootNode(InteropNode.createGetSize(context, SourceSection.createUnavailable("", ""))));
    }

}
