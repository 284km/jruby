/*
 * Copyright (c) 2013, 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.core.basicobject;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.interop.ForeignAccess;
import com.oracle.truffle.api.interop.Message;
import com.oracle.truffle.api.nodes.RootNode;
import org.jruby.truffle.RubyContext;
import org.jruby.truffle.interop.InteropHasSize;
import org.jruby.truffle.interop.InteropIsNull;
import org.jruby.truffle.interop.RubyInteropRootNode;
import org.jruby.truffle.interop.UnresolvedInteropExecuteAfterReadNode;
import org.jruby.truffle.interop.UnresolvedInteropReadNode;
import org.jruby.truffle.interop.UnresolvedInteropWriteNode;
import org.jruby.truffle.language.literal.BooleanLiteralNode;

public class BasicObjectForeignAccessFactory implements ForeignAccess.Factory10 {

    protected final RubyContext context;

    public BasicObjectForeignAccessFactory(RubyContext context) {
        this.context = context;
    }

    @Override
    public CallTarget accessIsNull() {
        return Truffle.getRuntime().createCallTarget(new RubyInteropRootNode(new InteropIsNull(context, null)));
    }

    @Override
    public CallTarget accessIsExecutable() {
        return Truffle.getRuntime().createCallTarget(RootNode.createConstantNode(false));
    }

    @Override
    public CallTarget accessIsBoxed() {
        return Truffle.getRuntime().createCallTarget(RootNode.createConstantNode(false));
    }

    @Override
    public CallTarget accessHasSize() {
        return Truffle.getRuntime().createCallTarget(new RubyInteropRootNode(new InteropHasSize(context, null)));
    }

    @Override
    public CallTarget accessGetSize() {
        return null;
    }

    @Override
    public CallTarget accessUnbox() {
        return null;
    }

    @Override
    public CallTarget accessRead() {
        return Truffle.getRuntime().createCallTarget(new RubyInteropRootNode(new UnresolvedInteropReadNode(context, null)));
    }

    @Override
    public CallTarget accessWrite() {
        return Truffle.getRuntime().createCallTarget(new RubyInteropRootNode(new UnresolvedInteropWriteNode(context, null)));
    }

    @Override
    public CallTarget accessExecute(int i) {
        return null;
    }

    @Override
    public CallTarget accessInvoke(int arity) {
        return Truffle.getRuntime().createCallTarget(new RubyInteropRootNode(new UnresolvedInteropExecuteAfterReadNode(context, null, arity)));
    }

    @Override
    public CallTarget accessNew(int argumentsLength) {
        return null;
    }

    @Override
    public CallTarget accessMessage(Message msg) {
        return null;
    }

}
