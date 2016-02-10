/*
 * Copyright (c) 2013, 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.language.objects;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.source.SourceSection;
import org.jcodings.specific.UTF8Encoding;
import org.jruby.truffle.RubyContext;
import org.jruby.truffle.core.Layouts;
import org.jruby.truffle.language.LexicalScope;
import org.jruby.truffle.language.ModuleOperations;
import org.jruby.truffle.language.RubyGuards;
import org.jruby.truffle.language.RubyNode;
import org.jruby.truffle.language.control.RaiseException;

public class ReadClassVariableNode extends RubyNode {

    private final String name;
    private final LexicalScope lexicalScope;

    public ReadClassVariableNode(RubyContext context, SourceSection sourceSection, String name, LexicalScope lexicalScope) {
        super(context, sourceSection);
        this.name = name;
        this.lexicalScope = lexicalScope;
    }

    public static DynamicObject resolveTargetModule(LexicalScope lexicalScope) {
        // MRI logic: ignore lexical scopes (cref) referring to singleton classes
        while (RubyGuards.isRubyClass(lexicalScope.getLiveModule()) && Layouts.CLASS.getIsSingleton((lexicalScope.getLiveModule()))) {
            lexicalScope = lexicalScope.getParent();
        }
        return lexicalScope.getLiveModule();
    }

    @Override
    public Object execute(VirtualFrame frame) {
        CompilerDirectives.transferToInterpreter();

        final DynamicObject module = resolveTargetModule(lexicalScope);

        assert RubyGuards.isRubyModule(module);

        final Object value = ModuleOperations.lookupClassVariable(module, name);

        if (value == null) {
            CompilerDirectives.transferToInterpreter();
            throw new RaiseException(getContext().getCoreLibrary().nameErrorUninitializedClassVariable(module, name, this));
        }

        return value;
    }

    @Override
    public Object isDefined(VirtualFrame frame) {
        final DynamicObject module = resolveTargetModule(lexicalScope);

        final Object value = ModuleOperations.lookupClassVariable(module, name);

        if (value == null) {
            return nil();
        } else {
            return create7BitString("class variable", UTF8Encoding.INSTANCE);
        }
    }

}
