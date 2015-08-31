/*
 * Copyright (c) 2013, 2015 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.runtime.object;

import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.Property;
import org.jruby.truffle.nodes.RubyGuards;
import org.jruby.truffle.runtime.RubyArguments;
import org.jruby.truffle.runtime.RubyConstant;
import org.jruby.truffle.runtime.RubyContext;
import org.jruby.truffle.runtime.core.ModuleFields;
import org.jruby.truffle.runtime.hash.Entry;
import org.jruby.truffle.runtime.layouts.Layouts;
import org.jruby.truffle.runtime.methods.InternalMethod;
import org.jruby.truffle.runtime.subsystems.SafepointAction;

import java.util.HashSet;
import java.util.Set;

public class ObjectGraph {

    private final RubyContext context;

    public ObjectGraph(RubyContext context) {
        this.context = context;
    }

    public Set<DynamicObject> getObjects() {
        return visitObjects(new ObjectGraphVisitor() {

            @Override
            public boolean visit(DynamicObject object) throws StopVisitingObjectsException {
                return true;
            }

        });
    }

    public Set<DynamicObject> visitObjects(final ObjectGraphVisitor visitor) {
        final Set<DynamicObject> objects = new HashSet<>();

        visitRoots(new ObjectGraphVisitor() {

            @Override
            public boolean visit(DynamicObject object) throws StopVisitingObjectsException {
                if (objects.add(object)) {
                    visitor.visit(object);
                    return true;
                } else {
                    return false;
                }
            }

        });

        return objects;
    }

    private void visitRoots(final ObjectGraphVisitor visitor) {
        context.getSafepointManager().pauseAllThreadsAndExecute(null, false, new SafepointAction() {

            boolean keepVisiting = true;

            @Override
            public void run(DynamicObject thread, Node currentNode) {
                synchronized (this) {
                    if (!keepVisiting) {
                        return;
                    }

                    try {
                        // We only visit the global variables and other global state from the root thread

                        if (thread == context.getThreadManager().getRootThread()) {
                            visitObject(context.getCoreLibrary().getGlobalVariablesObject(), visitor);

                            for (DynamicObject handler : context.getAtExitManager().getHandlers()) {
                                visitObject(handler, visitor);
                            }
                        }

                        // All threads visit their thread object

                        visitObject(thread, visitor);

                        // All threads visit their call stack

                        if (Truffle.getRuntime().getCurrentFrame() != null) {
                            visitFrameInstance(Truffle.getRuntime().getCurrentFrame(), visitor);
                        }

                        Truffle.getRuntime().iterateFrames(new FrameInstanceVisitor<Object>() {

                            @Override
                            public Object visitFrame(FrameInstance frameInstance) {
                                try {
                                    visitFrameInstance(frameInstance, visitor);
                                } catch (StopVisitingObjectsException e) {
                                    return new Object();
                                }
                                return null;
                            }

                        });
                    } catch (StopVisitingObjectsException e) {
                        keepVisiting = false;
                    }
                }
            }

        });
    }

    private void visitObject(DynamicObject object, ObjectGraphVisitor visitor) throws StopVisitingObjectsException {
        if (visitor.visit(object)) {
            // Visiting the meta class will also visit the logical class eventually

            visitObject(Layouts.BASIC_OBJECT.getMetaClass(object), visitor);

            // Visit all properties

            for (Property property : object.getShape().getPropertyListInternal(false)) {
                visitObject(property.get(object, object.getShape()), visitor);
            }

            // Visit specific objects that we're managing

            if (RubyGuards.isRubyModule(object)) {
                final ModuleFields module = Layouts.MODULE.getFields(object);

                for (DynamicObject ancestor : module.ancestors()) {
                    visitObject(ancestor, visitor);
                }

                for (RubyConstant constant : module.getConstants().values()) {
                    visitObject(constant.getValue(), visitor);
                }
            }
        }
    }

    private void visitObject(Object object, ObjectGraphVisitor visitor) throws StopVisitingObjectsException {
        if (object instanceof DynamicObject) {
            visitObject((DynamicObject) object, visitor);
        } else if (object instanceof Object[]) {
            for (Object child : (Object[]) object) {
                visitObject(child, visitor);
            }
        } else if (object instanceof Entry) {
            final Entry entry = (Entry) object;
            visitObject(entry.getKey(), visitor);
            visitObject(entry.getValue(), visitor);
            visitObject(entry.getNextInLookup(), visitor);
        } else if (object instanceof Frame) {
            visitFrame((Frame) object, visitor);
        } else if (object instanceof InternalMethod) {
            final InternalMethod method = (InternalMethod) object;
            visitObject(method.getDeclarationFrame(), visitor);
        }
    }

    private void visitFrameInstance(FrameInstance frameInstance, ObjectGraphVisitor visitor) throws StopVisitingObjectsException {
        visitFrame(frameInstance.getFrame(FrameInstance.FrameAccess.READ_ONLY, true), visitor);
    }

    private void visitFrame(Frame frame, ObjectGraphVisitor visitor) throws StopVisitingObjectsException {
        for (FrameSlot slot : frame.getFrameDescriptor().getSlots()) {
            visitObject(frame.getValue(slot), visitor);
        }

        Frame declarationFrame;

        try {
            declarationFrame = RubyArguments.getDeclarationFrame(frame.getArguments());
        } catch (Exception e) {
            declarationFrame = null;
        }

        if (declarationFrame != null) {
            visitFrame(declarationFrame, visitor);
        }
    }
}
