/*
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.runtime.methods;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.nodes.NodeUtil;
import org.jruby.runtime.Visibility;
import org.jruby.truffle.nodes.RubyRootNode;
import org.jruby.truffle.nodes.methods.arguments.BehaveAsBlockNode;
import org.jruby.truffle.runtime.*;
import org.jruby.truffle.runtime.core.*;

/**
 * Any kind of Ruby method - so normal methods in classes and modules, but also blocks, procs,
 * lambdas and native methods written in Java.
 */
public class RubyMethod {

    private final SourceSection sourceSection;

    private final UniqueMethodIdentifier uniqueIdentifier;
    private final String name;

    private final RubyModule declaringModule;
    private final Visibility visibility;
    private final boolean undefined;

    private final CallTarget callTarget;
    private final MaterializedFrame declarationFrame;

    public RubyMethod(SourceSection sourceSection, UniqueMethodIdentifier uniqueIdentifier, String name,
                      RubyModule declaringModule, Visibility visibility, boolean undefined,
                      CallTarget callTarget, MaterializedFrame declarationFrame) {
        this.sourceSection = sourceSection;
        this.uniqueIdentifier = uniqueIdentifier;
        this.declaringModule = declaringModule;
        this.name = name;
        this.visibility = visibility;
        this.undefined = undefined;
        this.callTarget = callTarget;
        this.declarationFrame = declarationFrame;
    }

    @Deprecated
    public Object call(Object self, RubyProc block, Object... args) {
        assert self != null;
        assert args != null;

        assert RubyContext.shouldObjectBeVisible(self) : self.getClass();
        assert RubyContext.shouldObjectsBeVisible(args);

        final Object result = callTarget.call(RubyArguments.create(declarationFrame, self, block, args));

        assert RubyContext.shouldObjectBeVisible(result);

        return result;
    }

    public RubyMethod withNewName(String newName) {
        if (newName.equals(name)) {
            return this;
        }

        return new RubyMethod(sourceSection, uniqueIdentifier, newName, declaringModule, visibility, undefined, callTarget, declarationFrame);
    }

    public RubyMethod withNewVisibility(Visibility newVisibility) {
        if (newVisibility == visibility) {
            return this;
        }

        return new RubyMethod(sourceSection, uniqueIdentifier, name, declaringModule, newVisibility, undefined, callTarget, declarationFrame);
    }

    public RubyMethod withDeclaringModule(RubyModule newDeclaringModule) {
        if (newDeclaringModule == declaringModule) {
            return this;
        }

        return new RubyMethod(sourceSection, uniqueIdentifier, name, newDeclaringModule, visibility, undefined, callTarget, declarationFrame);
    }

    public RubyMethod withoutBlockDestructureSemantics() {
        final RubyRootNode modifiedRootNode = (RubyRootNode) ((RootCallTarget) callTarget).getRootNode().split();

        for (BehaveAsBlockNode behaveAsBlockNode : NodeUtil.findAllNodeInstances(modifiedRootNode, BehaveAsBlockNode.class)) {
            behaveAsBlockNode.setBehaveAsBlock(false);
        }

        return new RubyMethod(sourceSection, uniqueIdentifier, name, declaringModule, visibility, undefined,
                Truffle.getRuntime().createCallTarget(modifiedRootNode), declarationFrame);
    }

    public RubyMethod undefined() {
        if (undefined) {
            return this;
        }

        return new RubyMethod(sourceSection, uniqueIdentifier, name, declaringModule, visibility, true, callTarget, declarationFrame);
    }

    public boolean isVisibleTo(RubyBasicObject caller, RubyBasicObject receiver) {
        if (caller == receiver.getRubyClass()){
            return true;
        }

        if (caller == receiver){
            return true;
        }
        return isVisibleTo(caller);
    }

    public boolean isVisibleTo(RubyBasicObject caller) {
        if (caller instanceof RubyModule) {
            if (isVisibleTo((RubyModule) caller)) {
                return true;
            }
        }

        if (isVisibleTo(caller.getRubyClass())) {
            return true;
        }

        if (isVisibleTo(caller.getSingletonClass())) {
            return true;
        }

        return false;
    }

    private boolean isVisibleTo(RubyModule module) {
        switch (visibility) {
            case PUBLIC:
                return true;

            case PROTECTED:
                if (module == declaringModule) {
                    return true;
                }

                if (module.getSingletonClass() == declaringModule) {
                    return true;
                }

                if (module.getParentModule() != null && isVisibleTo(module.getParentModule())) {
                    return true;
                }

                return false;

            case PRIVATE:
                if (module == declaringModule) {
                    return true;
                }

                if (module.getSingletonClass() == declaringModule) {
                    return true;
                }

                if (module.getParentModule() != null && isVisibleTo(module.getParentModule())) {
                    return true;
                }

                return false;

            default:
                return false;
        }
    }

    public SourceSection getSourceSection() {
        return sourceSection;
    }

    public String getName() {
        return name;
    }

    public UniqueMethodIdentifier getUniqueIdentifier() {
        return uniqueIdentifier;
    }

    public RubyModule getDeclaringModule() { return declaringModule; }

    public Visibility getVisibility() {
        return visibility;
    }

    public boolean isUndefined() {
        return undefined;
    }

    public MaterializedFrame getDeclarationFrame() {
        return declarationFrame;
    }

    public CallTarget getCallTarget(){
        return callTarget;
    }

}
