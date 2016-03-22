/*
 * Copyright (c) 2013, 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.interop;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.ForeignAccess;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.source.SourceSection;
import org.jruby.truffle.RubyContext;
import org.jruby.truffle.core.module.ModuleOperations;
import org.jruby.truffle.language.RubyGuards;
import org.jruby.truffle.language.RubyNode;
import org.jruby.truffle.language.methods.InternalMethod;

public class UnresolvedInteropReadNode extends RubyNode {

    private final int labelIndex;

    public UnresolvedInteropReadNode(RubyContext context, SourceSection sourceSection) {
        super(context, sourceSection);
        this.labelIndex = 0;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        Object label = ForeignAccess.getArguments(frame).get(labelIndex);
        if (label instanceof  String || RubyGuards.isRubySymbol(label) || label instanceof Integer) {
            if (label instanceof  String) {
                String name = (String) label;
                if (name.startsWith("@")) {
                    return this.replace(new InteropInstanceVariableReadNode(getContext(), getSourceSection(), name, labelIndex)).execute(frame);
                }
            }
            DynamicObject receiver = (DynamicObject) ForeignAccess.getReceiver(frame);

            if (RubyGuards.isRubyString(receiver)) {
                // TODO CS 22-Mar-16 monomorphic, what happens if it fails for other objects?
                return this.replace(new UnresolvedInteropStringReadNode(getContext(), getSourceSection())).execute(frame);
            }

            InternalMethod labelMethod = ModuleOperations.lookupMethod(coreLibrary().getMetaClass(receiver), label.toString());
            InternalMethod indexedSetter = ModuleOperations.lookupMethod(coreLibrary().getMetaClass(receiver), "[]=");
            if (labelMethod == null && indexedSetter != null) {
                return this.replace(new ResolvedInteropIndexedReadNode(getContext(), getSourceSection(), labelIndex)).execute(frame);
            } else if (label instanceof  String) {
                return this.replace(new ResolvedInteropReadNode(getContext(), getSourceSection(), (String) label, labelIndex)).execute(frame);
            } else if (RubyGuards.isRubySymbol(label)) {
                return this.replace(new ResolvedInteropReadFromSymbolNode(getContext(), getSourceSection(), (DynamicObject) label, labelIndex)).execute(frame);
            } else {
                CompilerDirectives.transferToInterpreter();
                throw new IllegalStateException(label + " not allowed as name");
            }
        } else {
            CompilerDirectives.transferToInterpreter();
            throw new IllegalStateException(label + " not allowed as name");
        }
    }
}
