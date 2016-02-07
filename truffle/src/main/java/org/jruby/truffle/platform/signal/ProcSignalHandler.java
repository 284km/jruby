/*
 * Copyright (c) 2014, 2015 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.platform.signal;

import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.object.DynamicObject;
import org.jruby.truffle.language.RubyGuards;
import org.jruby.truffle.core.proc.ProcNodes;
import org.jruby.truffle.RubyContext;
import org.jruby.truffle.core.Layouts;
import org.jruby.util.func.Function2;

public class ProcSignalHandler implements SignalHandler {

    private final RubyContext context;
    private final DynamicObject proc;

    public ProcSignalHandler(RubyContext context, DynamicObject proc) {
        assert RubyGuards.isRubyProc(proc);

        this.context = context;
        this.proc = proc;
    }

    @Override
    public void handle(Signal signal) {
        Thread mainThread = Layouts.FIBER.getThread((Layouts.THREAD.getFiberManager(context.getThreadManager().getRootThread()).getCurrentFiber()));
        context.getSafepointManager().pauseThreadAndExecuteLaterFromNonRubyThread(mainThread, new Function2<Void, DynamicObject, Node>() {
            @Override
            public Void apply(DynamicObject thread, Node currentNode) {
                ProcNodes.rootCall(proc);
                return null;
            }
        });
    }

}
