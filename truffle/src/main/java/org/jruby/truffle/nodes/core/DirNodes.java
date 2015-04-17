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

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.source.SourceSection;

import org.jruby.truffle.runtime.RubyContext;
import org.jruby.truffle.runtime.control.RaiseException;
import org.jruby.truffle.runtime.core.RubyArray;
import org.jruby.truffle.runtime.core.RubyFile;
import org.jruby.truffle.runtime.core.RubyProc;
import org.jruby.truffle.runtime.core.RubyString;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

@CoreClass(name = "Dir")
public abstract class DirNodes {

    @CoreMethod(names = {"exist?", "exists?"}, onSingleton = true, optional = 1)
    public abstract static class ExistsNode extends CoreMethodNode {

        public ExistsNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public ExistsNode(ExistsNode prev) {
            super(prev);
        }

        @Specialization
        public boolean exists(RubyString path) {
            notDesignedForCompilation();

            return new File(path.toString()).isDirectory();
        }

    }

    @CoreMethod(names = {"pwd", "getwd"}, onSingleton = true)
    public abstract static class PwdNode extends CoreMethodNode {

        public PwdNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public PwdNode(PwdNode prev) {
            super(prev);
        }

        @Specialization
        public RubyString pwd() {
            notDesignedForCompilation();

            return getContext().makeString(getContext().getRuntime().getCurrentDirectory());
        }

    }

}
