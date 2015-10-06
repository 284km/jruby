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
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.FrameInstance;
import com.oracle.truffle.api.frame.FrameInstanceVisitor;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.Property;
import org.jruby.truffle.runtime.RubyArguments;
import org.jruby.truffle.runtime.RubyContext;
import org.jruby.truffle.runtime.hash.Entry;
import org.jruby.truffle.runtime.layouts.Layouts;
import org.jruby.truffle.runtime.subsystems.SafepointAction;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

public abstract class ObjectGraph {

    public static Set<DynamicObject> stopAndGetAllObjects(
            Node currentNode, final RubyContext context) {
        final Set<DynamicObject> visited = new HashSet<>();

        final Thread stoppingThread = Thread.currentThread();

        context.getSafepointManager().pauseAllThreadsAndExecute(currentNode, false, new SafepointAction() {

            @Override
            public void run(DynamicObject thread, Node currentNode) {
                synchronized (visited) {
                    final Deque<DynamicObject> stack = new ArrayDeque<>();

                    stack.add(thread);

                    if (Thread.currentThread() == stoppingThread) {
                        stack.addAll(ObjectGraph.getAdjacentObjects(context.getCoreLibrary().getGlobalVariablesObject()));
                        stack.addAll(context.getAtExitManager().getHandlers());
                        stack.addAll(context.getObjectSpaceManager().getFinalizerHandlers());
                    }

                    final FrameInstance currentFrame = Truffle.getRuntime().getCurrentFrame();

                    if (currentFrame != null) {
                        stack.addAll(getObjectsInFrame(currentFrame.getFrame(FrameInstance.FrameAccess.READ_ONLY, true)));
                    }

                    Truffle.getRuntime().iterateFrames(new FrameInstanceVisitor<Object>() {

                        @Override
                        public Object visitFrame(FrameInstance frameInstance) {
                            stack.addAll(getObjectsInFrame(frameInstance
                                    .getFrame(FrameInstance.FrameAccess.READ_ONLY, true)));

                            return null;
                        }

                    });

                    while (!stack.isEmpty()) {
                        final DynamicObject object = stack.pop();

                        if (visited.add(object)) {
                            stack.addAll(ObjectGraph.getAdjacentObjects(object));
                        }
                    }
                }
            }

        });

        return visited;
    }

    public static Set<DynamicObject> stopAndGetRootObjects(Node currentNode, final RubyContext context) {
        final Set<DynamicObject> objects = new HashSet<>();

        final Thread stoppingThread = Thread.currentThread();

        context.getSafepointManager().pauseAllThreadsAndExecute(currentNode, false, new SafepointAction() {

            @Override
            public void run(DynamicObject thread, Node currentNode) {
                objects.add(thread);

                if (Thread.currentThread() == stoppingThread) {
                    objects.addAll(ObjectGraph.getAdjacentObjects(context.getCoreLibrary().getGlobalVariablesObject()));
                    objects.addAll(context.getAtExitManager().getHandlers());
                    objects.addAll(context.getObjectSpaceManager().getFinalizerHandlers());
                }
            }

        });

        return objects;
    }

    public static Set<DynamicObject> getAdjacentObjects(DynamicObject object) {
        final Set<DynamicObject> reachable = new HashSet<>();

        reachable.add(Layouts.BASIC_OBJECT.getLogicalClass(object));
        reachable.add(Layouts.BASIC_OBJECT.getMetaClass(object));

        for (Property property : object.getShape().getPropertyListInternal(false)) {
            final Object propertyValue = property.get(object, object.getShape());

            if (propertyValue instanceof DynamicObject) {
                reachable.add((DynamicObject) propertyValue);
            } else if (propertyValue instanceof Entry[]) {
                for (Entry bucket : (Entry[]) propertyValue) {
                    while (bucket != null) {
                        if (bucket.getKey() instanceof DynamicObject) {
                            reachable.add((DynamicObject) bucket.getKey());
                        }

                        if (bucket.getValue() instanceof DynamicObject) {
                            reachable.add((DynamicObject) bucket.getValue());
                        }

                        bucket = bucket.getNextInLookup();
                    }
                }
            } else if (propertyValue instanceof Object[]) {
                for (Object element : (Object[]) propertyValue) {
                    if (element instanceof DynamicObject) {
                        reachable.add((DynamicObject) element);
                    }
                }
            } else if (propertyValue instanceof Frame) {
                reachable.addAll(getObjectsInFrame((Frame) propertyValue));
            } else if (propertyValue instanceof ObjectGraphNode) {
                reachable.addAll(((ObjectGraphNode) propertyValue).getAdjacentObjects());
            }
        }

        return reachable;
    }

    public static Set<DynamicObject> getObjectsInFrame(Frame frame) {
        final Set<DynamicObject> objects = new HashSet<>();

        final Frame lexicalParentFrame = RubyArguments.tryGetDeclarationFrame(frame.getArguments());

        if (lexicalParentFrame != null) {
            objects.addAll(getObjectsInFrame(lexicalParentFrame));
        }

        for (FrameSlot slot : frame.getFrameDescriptor().getSlots()) {
            final Object slotValue = frame.getValue(slot);

            if (slotValue instanceof DynamicObject) {
                objects.add((DynamicObject) slotValue);
            }
        }

        return objects;
    }

}
