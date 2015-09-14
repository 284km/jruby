/*
 * Copyright (c) 2013, 2015 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.nodes.core;

import org.jruby.truffle.nodes.RubyGuards;
import org.jruby.truffle.nodes.dispatch.RespondToNode;
import org.jruby.truffle.nodes.objectstorage.ReadHeadObjectFieldNode;
import org.jruby.truffle.nodes.objectstorage.ReadHeadObjectFieldNodeGen;
import org.jruby.truffle.runtime.ModuleOperations;
import org.jruby.truffle.runtime.NotProvided;
import org.jruby.truffle.runtime.RubyContext;
import org.jruby.truffle.runtime.control.RaiseException;
import org.jruby.truffle.runtime.layouts.Layouts;
import org.jruby.truffle.runtime.object.ObjectGraph;
import org.jruby.truffle.runtime.object.ObjectIDOperations;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.UnexpectedResultException;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.source.SourceSection;

@CoreClass(name = "ObjectSpace")
public abstract class ObjectSpaceNodes {

    @CoreMethod(names = "_id2ref", isModuleFunction = true, required = 1)
    @ImportStatic(ObjectIDOperations.class)
    public abstract static class ID2RefNode extends CoreMethodArrayArgumentsNode {

        public ID2RefNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        @Specialization(guards = "id == NIL")
        public Object id2RefNil(long id) {
            return nil();
        }

        @Specialization(guards = "id == TRUE")
        public boolean id2RefTrue(long id) {
            return true;
        }

        @Specialization(guards = "id == FALSE")
        public boolean id2RefFalse(long id) {
            return false;
        }

        @Specialization(guards = "isSmallFixnumID(id)")
        public long id2RefSmallInt(long id) {
            return ObjectIDOperations.toFixnum(id);
        }

        @TruffleBoundary
        @Specialization(guards = "isBasicObjectID(id)")
        public DynamicObject id2Ref(
                final long id,
                @Cached("createReadObjectIDNode()") ReadHeadObjectFieldNode readObjectIdNode) {
            for (DynamicObject object : ObjectGraph.stopAndGetAllObjects(this, getContext())) {
                final long objectID;

                try {
                    objectID = readObjectIdNode.executeLong(object);
                } catch (UnexpectedResultException e) {
                    throw new UnsupportedOperationException(e);
                }

                if (objectID == id) {
                    return object;
                }
            }

            throw new RaiseException(getContext().getCoreLibrary().rangeError(String.format("0x%016x is not id value", id), this));
        }

        @Specialization(guards = { "isRubyBignum(id)", "isLargeFixnumID(id)" })
        public Object id2RefLargeFixnum(DynamicObject id) {
            return Layouts.BIGNUM.getValue(id).longValue();
        }

        @Specialization(guards = { "isRubyBignum(id)", "isFloatID(id)" })
        public double id2RefFloat(DynamicObject id) {
            return Double.longBitsToDouble(Layouts.BIGNUM.getValue(id).longValue());
        }

        protected ReadHeadObjectFieldNode createReadObjectIDNode() {
            return ReadHeadObjectFieldNodeGen.create(getContext(), getSourceSection(), Layouts.OBJECT_ID_IDENTIFIER, 0L, null);
        }

        protected boolean isLargeFixnumID(DynamicObject id) {
            return ObjectIDOperations.isLargeFixnumID(Layouts.BIGNUM.getValue(id));
        }

        protected boolean isFloatID(DynamicObject id) {
            return ObjectIDOperations.isFloatID(Layouts.BIGNUM.getValue(id));
        }

    }

    @CoreMethod(names = "each_object", isModuleFunction = true, needsBlock = true, optional = 1, returnsEnumeratorIfNoBlock = true)
    public abstract static class EachObjectNode extends YieldingCoreMethodNode {

        public EachObjectNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        @Specialization(guards = "isRubyProc(block)")
        public int eachObject(VirtualFrame frame, NotProvided ofClass, DynamicObject block) {
            CompilerDirectives.transferToInterpreter();

            int count = 0;

            for (DynamicObject object : ObjectGraph.stopAndGetAllObjects(this, getContext())) {
                if (!isHidden(object)) {
                    yield(frame, block, object);
                    count++;
                }
            }

            return count;
        }

        @Specialization(guards = {"isRubyClass(ofClass)", "isRubyProc(block)"})
        public int eachObject(VirtualFrame frame, DynamicObject ofClass, DynamicObject block) {
            CompilerDirectives.transferToInterpreter();

            int count = 0;

            for (DynamicObject object : ObjectGraph.stopAndGetAllObjects(this, getContext())) {
                if (!isHidden(object) && ModuleOperations.assignableTo(Layouts.BASIC_OBJECT.getLogicalClass(object), ofClass)) {
                    yield(frame, block, object);
                    count++;
                }
            }

            return count;
        }

        private boolean isHidden(DynamicObject object) {
            return RubyGuards.isRubyClass(object) && Layouts.CLASS.getIsSingleton(object);
        }

    }

    @CoreMethod(names = "define_finalizer", isModuleFunction = true, required = 2)
    public abstract static class DefineFinalizerNode extends CoreMethodArrayArgumentsNode {

        @Child private RespondToNode respondToNode;

        public DefineFinalizerNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
            respondToNode = new RespondToNode(getContext(), getSourceSection(), null, "call");
        }

        @Specialization
        public DynamicObject defineFinalizer(VirtualFrame frame, DynamicObject object, Object finalizer) {
            if (respondToNode.executeBoolean(frame, finalizer)) {
                getContext().getObjectSpaceManager().defineFinalizer(object, finalizer);
                Object[] objects = new Object[]{0, finalizer};
                return Layouts.ARRAY.createArray(getContext().getCoreLibrary().getArrayFactory(), objects, objects.length);
            } else {
                CompilerDirectives.transferToInterpreter();
                throw new RaiseException(getContext().getCoreLibrary().argumentErrorWrongArgumentType(finalizer, "callable", this));
            }
        }

    }

    @CoreMethod(names = "undefine_finalizer", isModuleFunction = true, required = 1)
    public abstract static class UndefineFinalizerNode extends CoreMethodArrayArgumentsNode {

        public UndefineFinalizerNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        @TruffleBoundary
        @Specialization
        public Object undefineFinalizer(Object object) {
            getContext().getObjectSpaceManager().undefineFinalizer((DynamicObject) object);
            return object;
        }
    }

}
