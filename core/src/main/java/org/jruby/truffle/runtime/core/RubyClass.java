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

import java.util.*;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives.*;
import com.oracle.truffle.api.nodes.Node;
import org.jruby.truffle.nodes.RubyNode;
import org.jruby.truffle.runtime.*;
import org.jruby.truffle.runtime.objectstorage.*;

/**
 * Represents the Ruby {@code Class} class. Note that most of the functionality you might associate
 * with {@code Class} is actually in {@code Module}, implemented by {@link RubyModule}.
 */
public class RubyClass extends RubyModule {

    private boolean isSingleton;
    private final Set<RubyClass> subClasses = Collections.newSetFromMap(new WeakHashMap<RubyClass, Boolean>());
    private ObjectLayout objectLayoutForInstances = null;

    /**
     * The class from which we create the object that is {@code Class}. A subclass of
     * {@link RubyClass} so that we can override {@link #newInstance} and allocate a
     * {@link RubyClass} rather than a normal {@link RubyBasicObject}.
     */
    public static class RubyClassClass extends RubyClass {

        public RubyClassClass(RubyContext context) {
            super(context, null, null, "Class", false);
        }

        @Override
        public RubyBasicObject newInstance(RubyNode currentNode) {
            return new RubyClass(getContext(), null, null, "(unnamed class)", false);
        }

    }

    /**
     * This constructor supports initialization and solves boot-order problems and should not
     * normally be used from outside this class.
     */
    public static RubyClass createBootClass(RubyContext context, String name) {
        return new RubyClass(context, null, null, name, false);
    }

    public RubyClass(RubyContext context, RubyModule lexicalParent, RubyClass superclass, String name) {
        this(context, lexicalParent, superclass, name, false);
        // Always create a class singleton class for normal classes for consistency.
        ensureSingletonConsistency();
    }

    protected static RubyClass createSingletonClassOfObject(RubyContext context, RubyClass superclass, String name) {
        // We also need to create the singleton class of a singleton class for proper lookup and consistency.
        // See rb_singleton_class() documentation in MRI.
        return new RubyClass(context, null, superclass, name, true).ensureSingletonConsistency();
    }

    protected RubyClass(RubyContext context, RubyModule lexicalParent, RubyClass superclass, String name, boolean isSingleton) {
        super(context, context.getCoreLibrary().getClassClass(), lexicalParent, name, null);
        this.isSingleton = isSingleton;

        if (superclass == null) {
            objectLayoutForInstances = ObjectLayout.EMPTY;
        } else {
            unsafeSetSuperclass(superclass);
        }
    }

    public void initialize(RubyClass superclass) {
        unsafeSetSuperclass(superclass);
        ensureSingletonConsistency();
    }

    @Override
    public void initCopy(RubyModule other) {
        super.initCopy(other);
        assert other instanceof RubyClass;
        final RubyClass otherClass = (RubyClass) other;
        this.objectLayoutForInstances = otherClass.objectLayoutForInstances;
    }

    private RubyClass ensureSingletonConsistency() {
        createOneSingletonClass();
        return this;
    }

    @Override
    public RubyClass getSingletonClass(Node currentNode) {
        // We also need to create the singleton class of a singleton class for proper lookup and consistency.
        // See rb_singleton_class() documentation in MRI.
        return createOneSingletonClass().ensureSingletonConsistency();
    }

    private RubyClass createOneSingletonClass() {
        CompilerAsserts.neverPartOfCompilation();

        if (metaClass.isSingleton()) {
            return metaClass;
        }

        RubyClass singletonSuperclass;

        if (getSuperClass() == null) {
            singletonSuperclass = getLogicalClass();
        } else {
            singletonSuperclass = getSuperClass().createOneSingletonClass();
        }

        metaClass = new RubyClass(getContext(),
                null, singletonSuperclass, String.format("#<Class:%s>", getName()), true);

        return metaClass;
    }

    /**
     * This method supports initialization and solves boot-order problems and should not normally be
     * used.
     */
    public void unsafeSetSuperclass(RubyClass newSuperclass) {
        RubyNode.notDesignedForCompilation();

        assert parentModule == null;

        unsafeSetParent(newSuperclass);
        newSuperclass.subClasses.add(this);

        objectLayoutForInstances = new ObjectLayout(newSuperclass.objectLayoutForInstances);
    }

    @SlowPath
    public RubyBasicObject newInstance(RubyNode currentNode) {
        return new RubyBasicObject(this);
    }

    public boolean isSingleton() {
        return isSingleton;
    }

    /**
     * Returns the object layout that objects of this class should use. Do not confuse with
     * {@link #getObjectLayout}, which for {@link RubyClass} will return the layout of the class
     * object itself.
     */
    public ObjectLayout getObjectLayoutForInstances() {
        return objectLayoutForInstances;
    }

    /**
     * Change the layout to be used for instances of this object.
     */
    public void setObjectLayoutForInstances(ObjectLayout newObjectLayoutForInstances) {
        RubyNode.notDesignedForCompilation();

        assert newObjectLayoutForInstances != objectLayoutForInstances;

        objectLayoutForInstances = newObjectLayoutForInstances;

        for (RubyClass subClass : subClasses) {
            subClass.renewObjectLayoutForInstances();
        }
    }

    private void renewObjectLayoutForInstances() {
        RubyNode.notDesignedForCompilation();

        objectLayoutForInstances = objectLayoutForInstances.withNewParent(getSuperClass().objectLayoutForInstances);

        for (RubyClass subClass : subClasses) {
            subClass.renewObjectLayoutForInstances();
        }
    }

    public RubyClass getSuperClass() {
        CompilerAsserts.neverPartOfCompilation();

        for (RubyModule ancestor : parentAncestors()) {
            if (ancestor instanceof RubyClass) {
                return (RubyClass) ancestor;
            }
        }

        return null;
    }

}
