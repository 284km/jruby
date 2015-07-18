/*
 * Copyright (c) 2013, 2015 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.runtime.core;

import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.nodes.Node;
import org.jruby.truffle.nodes.objects.Allocator;
import org.jruby.truffle.runtime.RubyContext;

/**
 * Represents the Ruby {@code Class} class. Note that most of the functionality you might associate
 * with {@code Class} is actually in {@code Module}, implemented by {@link RubyModule}.
 */
public class RubyClass extends RubyModule {

    // TODO(CS): is this compilation final needed? Is it a problem for correctness?
    @CompilationFinal
    private Allocator allocator;

    public RubyClass(RubyContext context, RubyModule lexicalParent, RubyClass superclass, String name, Allocator allocator) {
        this(context, superclass.getLogicalClass(), lexicalParent, superclass, name, false, null, allocator);
        // Always create a class singleton class for normal classes for consistency.
        model.ensureSingletonConsistency();
    }

    public RubyClass(RubyContext context, RubyClass classClass, RubyModule lexicalParent, RubyClass superclass, String name, boolean isSingleton, RubyModule attached, Allocator allocator) {
        super(context, classClass, lexicalParent, name, null, isSingleton, attached);

        assert isSingleton || attached == null;

        this.unsafeSetAllocator(allocator);

        if (superclass != null) {
            model.unsafeSetSuperclass(superclass);
        }
    }

    public RubyBasicObject allocate(Node currentNode) {
        return getAllocator().allocate(getContext(), this, currentNode);
    }

    public Allocator getAllocator() {
        return allocator;
    }

    public void unsafeSetAllocator(Allocator allocator) {
        this.allocator = allocator;
    }

}
