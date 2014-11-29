/*
 * Copyright (c) 2013, 2014 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.nodes.objects;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.source.*;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.UnexpectedResultException;
import org.jruby.truffle.nodes.*;
import org.jruby.truffle.nodes.objectstorage.*;
import org.jruby.truffle.runtime.*;
import org.jruby.truffle.runtime.control.RaiseException;
import org.jruby.truffle.runtime.core.RubyBasicObject;
import org.jruby.truffle.runtime.core.RubyMatchData;
import org.jruby.truffle.runtime.objectstorage.*;

public class WriteInstanceVariableNode extends RubyNode implements WriteNode {

    private final RespecializeHook hook = new RespecializeHook() {

        @Override
        public void hookRead(ObjectStorage object, String name) {
            final RubyBasicObject rubyObject = (RubyBasicObject) object;

            if (!rubyObject.hasPrivateLayout()) {
                rubyObject.updateLayoutToMatchClass();
            }
        }

        @Override
        public void hookWrite(ObjectStorage object, String name, Object value) {
            final RubyBasicObject rubyObject = (RubyBasicObject) object;

            if (!rubyObject.hasPrivateLayout()) {
                rubyObject.updateLayoutToMatchClass();
            }

            rubyObject.setInstanceVariable(name, value);
        }

    };

    @Child protected RubyNode receiver;
    @Child protected RubyNode rhs;
    @Child protected WriteHeadObjectFieldNode writeNode;
    private final boolean isGlobal;

    public WriteInstanceVariableNode(RubyContext context, SourceSection sourceSection, String name, RubyNode receiver, RubyNode rhs, boolean isGlobal) {
        super(context, sourceSection);
        this.receiver = receiver;
        this.rhs = rhs;
        writeNode = new WriteHeadObjectFieldNode(name, hook);
        this.isGlobal = isGlobal;
    }

    @Override
    public int executeIntegerFixnum(VirtualFrame frame) throws UnexpectedResultException {
        final Object object = receiver.execute(frame);

        if (object instanceof RubyBasicObject) {
            try {
                final int value = rhs.executeIntegerFixnum(frame);

                writeNode.execute((RubyBasicObject) object, value);
                return value;
            } catch (UnexpectedResultException e) {
                writeNode.execute((RubyBasicObject) object, e.getResult());
                throw e;
            }
        } else {
            CompilerDirectives.transferToInterpreter();
            throw new RaiseException(getContext().getCoreLibrary().frozenError(getContext().getCoreLibrary().getLogicalClass(object).getName(), this));
        }
    }

    @Override
    public long executeLongFixnum(VirtualFrame frame) throws UnexpectedResultException {
        final Object object = receiver.execute(frame);

        if (object instanceof RubyBasicObject) {
            try {
                final long value = rhs.executeLongFixnum(frame);

                writeNode.execute((RubyBasicObject) object, value);
                return value;
            } catch (UnexpectedResultException e) {
                writeNode.execute((RubyBasicObject) object, e.getResult());
                throw e;
            }
        } else {
            CompilerDirectives.transferToInterpreter();
            throw new RaiseException(getContext().getCoreLibrary().frozenError(getContext().getCoreLibrary().getLogicalClass(object).getName(), this));
        }
    }

    @Override
    public double executeFloat(VirtualFrame frame) throws UnexpectedResultException {
        final Object object = receiver.execute(frame);

        if (object instanceof RubyBasicObject) {
            try {
                final double value = rhs.executeFloat(frame);

                writeNode.execute((RubyBasicObject) object, value);
                return value;
            } catch (UnexpectedResultException e) {
                writeNode.execute((RubyBasicObject) object, e.getResult());
                throw e;
            }
        } else {
            CompilerDirectives.transferToInterpreter();
            throw new RaiseException(getContext().getCoreLibrary().frozenError(getContext().getCoreLibrary().getLogicalClass(object).getName(), this));
        }
    }

    @Override
    public Object execute(VirtualFrame frame) {
        final Object object = receiver.execute(frame);
        final Object value = rhs.execute(frame);

        if (object instanceof RubyBasicObject) {
            writeNode.execute((RubyBasicObject) object, value);
        } else {
            CompilerDirectives.transferToInterpreter();
            throw new RaiseException(getContext().getCoreLibrary().frozenError(getContext().getCoreLibrary().getLogicalClass(object).getName(), this));
        }

        return value;
    }

    @Override
    public RubyNode makeReadNode() {
        return new ReadInstanceVariableNode(getContext(), getSourceSection(), writeNode.getName(), receiver, isGlobal);
    }
}
