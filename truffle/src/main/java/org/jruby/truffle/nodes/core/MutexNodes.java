/*
 * Copyright (c) 2015 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.nodes.core;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.object.*;
import com.oracle.truffle.api.source.SourceSection;
import org.jruby.truffle.nodes.RubyGuards;
import org.jruby.truffle.nodes.RubyNode;
import org.jruby.truffle.nodes.objects.Allocator;
import org.jruby.truffle.runtime.NotProvided;
import org.jruby.truffle.runtime.RubyContext;
import org.jruby.truffle.runtime.control.RaiseException;
import com.oracle.truffle.api.object.DynamicObject;
import org.jruby.truffle.runtime.subsystems.ThreadManager.BlockingAction;

import java.util.concurrent.locks.ReentrantLock;

@CoreClass(name = "Mutex")
public abstract class MutexNodes {

    @org.jruby.truffle.om.dsl.api.Layout
    public interface MutexLayout extends BasicObjectNodes.BasicObjectLayout {

        DynamicObjectFactory createMutexShape(DynamicObject logicalClass, DynamicObject metaClass);

        DynamicObject createMutex(DynamicObjectFactory factory, ReentrantLock lock);

        boolean isMutex(DynamicObject object);

        ReentrantLock getLock(DynamicObject object);

    }

    public static final MutexLayout MUTEX_LAYOUT = MutexLayoutImpl.INSTANCE;

    public static class MutexAllocator implements Allocator {
        @Override
        public DynamicObject allocate(RubyContext context, DynamicObject rubyClass, Node currentNode) {
            return BasicObjectNodes.createDynamicObject(rubyClass, MUTEX_LAYOUT.createMutex(ModuleNodes.getModel(rubyClass).getFactory(), new ReentrantLock()));
        }
    }

    protected static ReentrantLock getLock(DynamicObject mutex) {
        return MUTEX_LAYOUT.getLock(BasicObjectNodes.getDynamicObject(mutex));
    }

    @CoreMethod(names = "lock")
    public abstract static class LockNode extends UnaryCoreMethodNode {

        public LockNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        @Specialization
        public DynamicObject lock(DynamicObject mutex) {
            final ReentrantLock lock = getLock(mutex);
            final DynamicObject thread = getContext().getThreadManager().getCurrentThread();

            lock(lock, thread, this);

            return mutex;
        }

        protected static void lock(final ReentrantLock lock, final DynamicObject thread, RubyNode currentNode) {
            assert RubyGuards.isRubyThread(thread);

            final RubyContext context = currentNode.getContext();

            if (lock.isHeldByCurrentThread()) {
                CompilerDirectives.transferToInterpreter();
                throw new RaiseException(context.getCoreLibrary().threadError("deadlock; recursive locking", currentNode));
            }

            context.getThreadManager().runUntilResult(new BlockingAction<Boolean>() {
                @Override
                public Boolean block() throws InterruptedException {
                    lock.lockInterruptibly();
                    ThreadNodes.acquiredLock(thread, lock);
                    return SUCCESS;
                }
            });
        }

    }

    @CoreMethod(names = "locked?")
    public abstract static class IsLockedNode extends UnaryCoreMethodNode {

        public IsLockedNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        @Specialization
        public boolean isLocked(DynamicObject mutex) {
            return getLock(mutex).isLocked();
        }

    }

    @CoreMethod(names = "owned?")
    public abstract static class IsOwnedNode extends UnaryCoreMethodNode {

        public IsOwnedNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        @Specialization
        public boolean isOwned(DynamicObject mutex) {
            return getLock(mutex).isHeldByCurrentThread();
        }

    }

    @CoreMethod(names = "try_lock")
    public abstract static class TryLockNode extends UnaryCoreMethodNode {

        public TryLockNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        @Specialization
        public boolean tryLock(DynamicObject mutex) {
            final ReentrantLock lock = getLock(mutex);

            if (lock.isHeldByCurrentThread()) {
                return false;
            }

            if (lock.tryLock()) {
                final DynamicObject thread = getContext().getThreadManager().getCurrentThread();
                ThreadNodes.acquiredLock(thread, lock);
                return true;
            } else {
                return false;
            }
        }

    }

    @CoreMethod(names = "unlock")
    public abstract static class UnlockNode extends UnaryCoreMethodNode {

        public UnlockNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        @Specialization
        public DynamicObject unlock(DynamicObject mutex) {
            final ReentrantLock lock = getLock(mutex);
            final DynamicObject thread = getContext().getThreadManager().getCurrentThread();

            unlock(lock, thread, this);

            return mutex;
        }

        protected static void unlock(ReentrantLock lock, DynamicObject thread, RubyNode currentNode) {
            assert RubyGuards.isRubyThread(thread);

            final RubyContext context = currentNode.getContext();

            try {
                lock.unlock();
            } catch (IllegalMonitorStateException e) {
                if (!lock.isLocked()) {
                    throw new RaiseException(context.getCoreLibrary().threadError("Attempt to unlock a mutex which is not locked", currentNode));
                } else {
                    throw new RaiseException(context.getCoreLibrary().threadError("Attempt to unlock a mutex which is locked by another thread", currentNode));
                }
            }

            ThreadNodes.releasedLock(thread, lock);
        }

    }

    @CoreMethod(names = "sleep", optional = 1)
    public abstract static class SleepNode extends CoreMethodArrayArgumentsNode {

        public SleepNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        @Specialization
        public long sleep(DynamicObject mutex, NotProvided duration) {
            return doSleepMillis(mutex, Long.MAX_VALUE);
        }

        @Specialization(guards = "isNil(duration)")
        public long sleep(DynamicObject mutex, DynamicObject duration) {
            return sleep(mutex, NotProvided.INSTANCE);
        }

        @Specialization
        public long sleep(DynamicObject mutex, long duration) {
            return doSleepMillis(mutex, duration * 1000);
        }

        @Specialization
        public long sleep(DynamicObject mutex, double duration) {
            return doSleepMillis(mutex, (long) (duration * 1000.0));
        }

        public long doSleepMillis(DynamicObject mutex, long durationInMillis) {
            if (durationInMillis < 0) {
                throw new RaiseException(getContext().getCoreLibrary().argumentError("time interval must be positive", this));
            }

            final ReentrantLock lock = getLock(mutex);
            final DynamicObject thread = getContext().getThreadManager().getCurrentThread();

            // Clear the wakeUp flag, following Ruby semantics:
            // it should only be considered if we are inside the sleep when Thread#{run,wakeup} is called.
            // Here we do it before unlocking for providing nice semantics for
            // thread1: mutex.sleep
            // thread2: mutex.synchronize { <ensured that thread1 is sleeping and thread1.wakeup will wake it up> }
            ThreadNodes.shouldWakeUp(thread);

            UnlockNode.unlock(lock, thread, this);
            try {
                return KernelNodes.SleepNode.sleepFor(getContext(), durationInMillis);
            } finally {
                LockNode.lock(lock, thread, this);
            }
        }

    }

}
