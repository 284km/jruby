/*
 * Copyright (c) 2015 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.runtime.layouts;

import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.DynamicObjectFactory;
import org.jruby.truffle.nodes.core.InterruptMode;
import org.jruby.truffle.nodes.core.ThreadNodes;
import org.jruby.truffle.om.dsl.api.Layout;
import org.jruby.truffle.om.dsl.api.Nullable;
import org.jruby.truffle.om.dsl.api.Volatile;
import org.jruby.truffle.runtime.subsystems.FiberManager;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;

@Layout
public interface ThreadLayout extends BasicObjectLayout {

    DynamicObjectFactory createThreadShape(DynamicObject logicalClass,
                                           DynamicObject metaClass);

    DynamicObject createThread(DynamicObjectFactory factory,
                               @Nullable FiberManager fiberManager,
                               @Nullable String name,
                               CountDownLatch finishedLatch,
                               DynamicObject threadLocals,
                               List<Lock> ownedLocks,
                               boolean abortOnException,
                               InterruptMode interruptMode,
                               @Nullable @Volatile Thread thread,
                               ThreadNodes.ThreadFields fields);

    boolean isThread(DynamicObject object);

    FiberManager getFiberManager(DynamicObject object);
    void setFiberManagerUnsafe(DynamicObject object, FiberManager fiberManager);

    String getName(DynamicObject object);
    void setName(DynamicObject object, String name);

    CountDownLatch getFinishedLatch(DynamicObject object);

    DynamicObject getThreadLocals(DynamicObject object);

    List<Lock> getOwnedLocks(DynamicObject object);

    boolean getAbortOnException(DynamicObject object);
    void setAbortOnException(DynamicObject object, boolean abortOnException);

    InterruptMode getInterruptMode(DynamicObject object);
    void setInterruptMode(DynamicObject object, InterruptMode interruptMode);

    Thread getThread(DynamicObject object);
    void setThread(DynamicObject object, Thread thread);

    ThreadNodes.ThreadFields getFields(DynamicObject object);

}
