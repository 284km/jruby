/*
 * Copyright (c) 2015 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.nodes.rubinius;

import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.object.*;
import jnr.ffi.Pointer;
import org.jruby.truffle.nodes.core.BasicObjectNodes;
import org.jruby.truffle.nodes.core.ModuleNodes;
import org.jruby.truffle.nodes.objects.Allocator;
import org.jruby.truffle.om.dsl.api.Layout;
import org.jruby.truffle.runtime.RubyContext;
import org.jruby.truffle.runtime.core.RubyBasicObject;

public abstract class PointerNodes {

    public static final Pointer NULL_POINTER = jnr.ffi.Runtime.getSystemRuntime().getMemoryManager().newOpaquePointer(0);

    @Layout
    public interface PointerLayout extends BasicObjectNodes.BasicObjectLayout {

        DynamicObjectFactory createPointerShape(RubyBasicObject logicalClass, RubyBasicObject metaClass);

        DynamicObject createPointer(DynamicObjectFactory factory, Pointer pointer);

        boolean isPointer(DynamicObject object);

        Pointer getPointer(DynamicObject object);
        void setPointer(DynamicObject object, Pointer pointer);

    }

    public static final PointerLayout POINTER_LAYOUT = PointerLayoutImpl.INSTANCE;

    public static class PointerAllocator implements Allocator {
        @Override
        public RubyBasicObject allocate(RubyContext context, RubyBasicObject rubyClass, Node currentNode) {
            return createPointer(rubyClass, NULL_POINTER);
        }
    }

    public static RubyBasicObject createPointer(RubyBasicObject rubyClass, Pointer pointer) {
        if (pointer == null) {
            pointer = NULL_POINTER;
        }

        return BasicObjectNodes.createRubyBasicObject(rubyClass, POINTER_LAYOUT.createPointer(ModuleNodes.getModel(rubyClass).factory, pointer));
    }

    public static void setPointer(RubyBasicObject pointer, Pointer newPointer) {
        POINTER_LAYOUT.setPointer(BasicObjectNodes.getDynamicObject(pointer), newPointer);
    }

    public static Pointer getPointer(RubyBasicObject pointer) {
        return POINTER_LAYOUT.getPointer(BasicObjectNodes.getDynamicObject(pointer));
    }

}
