/*
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.runtime.core;

import org.jruby.truffle.nodes.RubyNode;

import java.util.*;

/**
 * Represents the Ruby {@code Time} class. This is a very rough implementation and is only really
 * enough to run benchmark harnesses.
 */
public class RubyTime extends RubyObject {

    /**
     * The class from which we create the object that is {@code Time}. A subclass of
     * {@link RubyClass} so that we can override {@link RubyClass#newInstance} and allocate a
     * {@link RubyTime} rather than a normal {@link RubyBasicObject}.
     */
    public static class RubyTimeClass extends RubyClass {

        public RubyTimeClass(RubyClass objectClass) {
            super(null, null, objectClass, "Time");
        }

        @Override
        public RubyBasicObject newInstance(RubyNode currentNode) {
            return new RubyTime(this, milisecondsToNanoseconds(System.currentTimeMillis()));
        }

    }

    public final long nanoseconds;

    public RubyTime(RubyClass timeClass, long nanoseconds) {
        super(timeClass);
        this.nanoseconds = nanoseconds;
    }


    public Date toDate() {
        return new Date(nanosecondsToMiliseconds(nanoseconds));
    }

    public static RubyTime fromDate(RubyClass timeClass, long timeMiliseconds) {
        return new RubyTime(timeClass, milisecondsToNanoseconds(timeMiliseconds));
    }

    private static long milisecondsToNanoseconds(long miliseconds) {
        return miliseconds * 1000000;
    }

    private static long nanosecondsToMiliseconds(long nanoseconds) {
        return nanoseconds / 1000000;
    }

    public static double nanosecondsToSecond(long nanoseconds) {
        return nanoseconds / 1e9;
    }

}
