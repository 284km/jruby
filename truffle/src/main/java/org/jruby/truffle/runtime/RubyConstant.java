/*
 * Copyright (c) 2014, 2015 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.runtime;

import com.oracle.truffle.api.CompilerAsserts;
import org.jruby.truffle.nodes.RubyGuards;
import org.jruby.truffle.nodes.core.ModuleNodes;
import org.jruby.truffle.runtime.core.RubyBasicObject;

public class RubyConstant {

    private final RubyBasicObject declaringModule;
    private final Object value;
    private boolean isPrivate;
    private final boolean autoload;

    public RubyConstant(RubyBasicObject declaringModule, Object value, boolean isPrivate, boolean autoload) {
        assert RubyGuards.isRubyModule(declaringModule);
        this.declaringModule = declaringModule;
        this.value = value;
        this.isPrivate = isPrivate;
        this.autoload = autoload;
    }

    public RubyBasicObject getDeclaringModule() {
        return declaringModule;
    }

    public Object getValue() {
        return value;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    public boolean isVisibleTo(RubyContext context, LexicalScope lexicalScope, RubyBasicObject module) {
        CompilerAsserts.neverPartOfCompilation();

        assert RubyGuards.isRubyModule(module);
        assert lexicalScope == null || lexicalScope.getLiveModule() == module;

        if (!isPrivate) {
            return true;
        }

        // Look in lexical scope
        if (lexicalScope != null) {
            while (lexicalScope != context.getRootLexicalScope()) {
                if (lexicalScope.getLiveModule() == declaringModule) {
                    return true;
                }
                lexicalScope = lexicalScope.getParent();
            }
        }

        // Look in ancestors
        if (RubyGuards.isRubyClass(module)) {
            for (RubyBasicObject included : ModuleNodes.getModel(module).parentAncestors()) {
                if (included == declaringModule) {
                    return true;
                }
            }
        }

        // Allow Object constants if looking with lexical scope.
        if (lexicalScope != null && context.getCoreLibrary().getObjectClass() == declaringModule) {
            return true;
        }

        return false;
    }

    public boolean isAutoload() {
        return autoload;
    }

}
