/*
 * Copyright (c) 2013, 2014 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.runtime.core;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.Layout;
import com.oracle.truffle.api.object.Shape;
import org.jruby.truffle.nodes.RubyNode;
import org.jruby.truffle.runtime.ModuleOperations;
import org.jruby.truffle.runtime.RubyContext;
import org.jruby.truffle.runtime.RubyOperations;
import org.jruby.truffle.runtime.control.RaiseException;
import org.jruby.truffle.runtime.subsystems.ObjectSpaceManager;

import java.util.Map;

/**
 * Represents the Ruby {@code BasicObject} class - the root of the Ruby class hierarchy.
 */
public class RubyBasicObject {

    public static Layout LAYOUT = Layout.createLayout(Layout.INT_TO_LONG);

    private final DynamicObject dynamicObject;

    /** The class of the object, not a singleton class. */
    @CompilationFinal protected RubyClass logicalClass;
    /** Either the singleton class if it exists or the logicalClass. */
    @CompilationFinal protected RubyClass metaClass;

    protected long objectID = -1;

    private boolean frozen = false;

    public RubyBasicObject(RubyClass rubyClass) {
        this(rubyClass, rubyClass.getContext());
    }

    public RubyBasicObject(RubyClass rubyClass, RubyContext context) {
        dynamicObject = LAYOUT.newInstance(context.getEmptyShape());

        if (rubyClass != null) {
            unsafeSetLogicalClass(rubyClass);
        }
    }

    public boolean hasNoSingleton() {
        return false;
    }

    public boolean hasClassAsSingleton() {
        return false;
    }

    public boolean isFrozen() {
        return frozen;
    }

    public void freeze() {
        frozen = true;
    }

    public void checkFrozen(Node currentNode) {
        if (frozen) {
            CompilerDirectives.transferToInterpreter();
            throw new RaiseException(getContext().getCoreLibrary().frozenError(getLogicalClass().getName(), currentNode));
        }
    }

    public RubyClass getMetaClass() {
        return metaClass;
    }

    public RubyClass getSingletonClass(Node currentNode) {
        CompilerAsserts.neverPartOfCompilation();

        if (hasNoSingleton()) {
            throw new RaiseException(getContext().getCoreLibrary().typeErrorCantDefineSingleton(currentNode));
        }

        if (hasClassAsSingleton() || metaClass.isSingleton()) {
            return metaClass;
        }

        final RubyClass logicalClass = metaClass;

        metaClass = RubyClass.createSingletonClassOfObject(getContext(), logicalClass,
                String.format("#<Class:#<%s:0x%x>>", logicalClass.getName(), getObjectID()));

        if (isFrozen()) {
            metaClass.freeze();
        }

        return metaClass;
    }

    public void setInstanceVariable(String name, Object value) {
        RubyNode.notDesignedForCompilation();
        getOperations().setInstanceVariable(this, name, value);
    }

    @CompilerDirectives.TruffleBoundary
    public long getObjectID() {
        if (objectID == -1) {
            objectID = getContext().getNextObjectID();
        }

        return objectID;
    }

    @CompilerDirectives.TruffleBoundary
    public void setInstanceVariables(Map<String, Object> instanceVariables) {
        getOperations().setInstanceVariables(this, instanceVariables);
    }


    @CompilerDirectives.TruffleBoundary
    public Map<String, Object>  getInstanceVariables() {
        return getOperations().getInstanceVariables(this);
    }

    public String[] getFieldNames() {
        return getOperations().getFieldNames(this);
    }

    public void extend(RubyModule module, RubyNode currentNode) {
        RubyNode.notDesignedForCompilation();

        getSingletonClass(currentNode).include(currentNode, module);
    }

    public void unsafeSetLogicalClass(RubyClass newLogicalClass) {
        assert logicalClass == null;
        logicalClass = newLogicalClass;
        metaClass = newLogicalClass;
    }

    //public void unsafeSetMetaClass(RubyClass newMetaClass) {
    //    assert metaClass == null;
    //    metaClass = newMetaClass;
    //}

    public Object getInstanceVariable(String name) {
        RubyNode.notDesignedForCompilation();

        final Object value = getOperations().getInstanceVariable(this, name);

        if (value == null) {
            return getContext().getCoreLibrary().getNilObject();
        } else {
            return value;
        }
    }

    public boolean isFieldDefined(String name) {
        return getOperations().isFieldDefined(this, name);
    }

    public void visitObjectGraph(ObjectSpaceManager.ObjectGraphVisitor visitor) {
        if (visitor.visit(this)) {
            metaClass.visitObjectGraph(visitor);

            for (Object instanceVariable : getOperations().getInstanceVariables(this).values()) {
                if (instanceVariable instanceof RubyBasicObject) {
                    ((RubyBasicObject) instanceVariable).visitObjectGraph(visitor);
                }
            }

            visitObjectGraphChildren(visitor);
        }
    }

    public void visitObjectGraphChildren(ObjectSpaceManager.ObjectGraphVisitor visitor) {
    }

    public boolean isNumeric() {
        return ModuleOperations.assignableTo(this.getMetaClass(), getContext().getCoreLibrary().getNumericClass());
    }

    public RubyContext getContext() {
        return logicalClass.getContext();
    }

    public Shape getObjectLayout() {
        return dynamicObject.getShape();
    }

    public RubyOperations getOperations() {
        return (RubyOperations) dynamicObject.getShape().getObjectType();
    }

    public RubyClass getLogicalClass() {
        return logicalClass;
    }

    public DynamicObject getDynamicObject() {
        return dynamicObject;
    }
}
