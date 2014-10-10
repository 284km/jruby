/*
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.nodes.core;

import java.math.*;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.source.*;
import com.oracle.truffle.api.dsl.*;
import com.oracle.truffle.api.frame.*;
import org.jruby.truffle.runtime.*;
import org.jruby.truffle.runtime.core.*;

@CoreClass(name = "ObjectSpace")
public abstract class ObjectSpaceNodes {

    @CoreMethod(names = "_id2ref", isModuleFunction = true, needsSelf = false, minArgs = 1, maxArgs = 1)
    public abstract static class ID2RefNode extends CoreMethodNode {

        public ID2RefNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public ID2RefNode(ID2RefNode prev) {
            super(prev);
        }

        @Specialization
        public Object id2Ref(int id) {
            notDesignedForCompilation();

            final Object object = getContext().getObjectSpaceManager().lookupId(id);

            if (object == null) {
                return NilPlaceholder.INSTANCE;
            } else {
                return object;
            }
        }

        @Specialization
        public Object id2Ref(BigInteger id) {
            notDesignedForCompilation();

            final Object object = getContext().getObjectSpaceManager().lookupId(id.longValue());

            if (object == null) {
                return NilPlaceholder.INSTANCE;
            } else {
                return object;
            }
        }

    }

    @CoreMethod(names = "each_object", isModuleFunction = true, needsSelf = false, needsBlock = true, minArgs = 0, maxArgs = 1)
    public abstract static class EachObjectNode extends YieldingCoreMethodNode {

        public EachObjectNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public EachObjectNode(EachObjectNode prev) {
            super(prev);
        }

        @Specialization
        public NilPlaceholder eachObject(VirtualFrame frame, @SuppressWarnings("unused") UndefinedPlaceholder ofClass, RubyProc block) {
            notDesignedForCompilation();

            for (RubyBasicObject object : getContext().getObjectSpaceManager().collectLiveObjects().values()) {
                yield(frame, block, object);
            }
            return NilPlaceholder.INSTANCE;
        }

        @Specialization
        public NilPlaceholder eachObject(VirtualFrame frame, RubyClass ofClass, RubyProc block) {
            notDesignedForCompilation();

            for (RubyBasicObject object : getContext().getObjectSpaceManager().collectLiveObjects().values()) {
                if (ModuleOperations.assignableTo(object.getLogicalClass(), ofClass)) {
                    yield(frame, block, object);
                }
            }
            return NilPlaceholder.INSTANCE;
        }

    }

    @CoreMethod(names = "define_finalizer", isModuleFunction = true, needsSelf = false, minArgs = 2, maxArgs = 2)
    public abstract static class DefineFinalizerNode extends CoreMethodNode {

        public DefineFinalizerNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public DefineFinalizerNode(DefineFinalizerNode prev) {
            super(prev);
        }

        @Specialization
        public RubyProc defineFinalizer(Object object, RubyProc finalizer) {
            notDesignedForCompilation();

            getContext().getObjectSpaceManager().defineFinalizer((RubyBasicObject) object, finalizer);
            return finalizer;
        }
    }

    @CoreMethod(names = {"garbage_collect", "start"}, isModuleFunction = true, needsSelf = false, maxArgs = 0)
    public abstract static class GarbageCollectNode extends CoreMethodNode {

        public GarbageCollectNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public GarbageCollectNode(GarbageCollectNode prev) {
            super(prev);
        }

        @Specialization
        public NilPlaceholder garbageCollect() {
            return doGC();
        }

        @CompilerDirectives.SlowPath
        private NilPlaceholder doGC() {
            notDesignedForCompilation();

            getContext().outsideGlobalLock(new Runnable() {

                @Override
                public void run() {
                    System.gc();
                }

            });

            return NilPlaceholder.INSTANCE;
        }
    }

    @CoreMethod(names = "undefine_finalizer", isModuleFunction = true, needsSelf = false, minArgs = 1, maxArgs = 1)
    public abstract static class UndefineFinalizerNode extends CoreMethodNode {

        public UndefineFinalizerNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public UndefineFinalizerNode(UndefineFinalizerNode prev) {
            super(prev);
        }

        @Specialization
        public Object undefineFinalizer(Object object) {
            notDesignedForCompilation();

            getContext().getObjectSpaceManager().undefineFinalizer((RubyBasicObject) object);
            return object;
        }
    }

}
