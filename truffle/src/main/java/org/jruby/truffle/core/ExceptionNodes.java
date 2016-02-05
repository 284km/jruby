/*
 * Copyright (c) 2013, 2015 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.core;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.source.SourceSection;
import org.jcodings.specific.UTF8Encoding;
import org.jruby.truffle.language.objects.AllocateObjectNode;
import org.jruby.truffle.language.objects.AllocateObjectNodeGen;
import org.jruby.truffle.language.objects.ReadHeadObjectFieldNode;
import org.jruby.truffle.language.objects.ReadHeadObjectFieldNodeGen;
import org.jruby.truffle.runtime.NotProvided;
import org.jruby.truffle.runtime.RubyCallStack;
import org.jruby.truffle.runtime.RubyContext;
import org.jruby.truffle.language.backtrace.Backtrace;
import org.jruby.truffle.language.backtrace.BacktraceFormatter;
import org.jruby.truffle.runtime.layouts.Layouts;

import java.util.EnumSet;
import java.util.List;

@CoreClass(name = "Exception")
public abstract class ExceptionNodes {

    @TruffleBoundary
    public static DynamicObject backtraceAsRubyStringArray(RubyContext context, DynamicObject exception, Backtrace backtrace) {
        final List<String> lines = new BacktraceFormatter(context, EnumSet.of(BacktraceFormatter.FormattingFlags.OMIT_FROM_PREFIX, BacktraceFormatter.FormattingFlags.OMIT_EXCEPTION))
                .formatBacktrace(context, exception, backtrace);

        final Object[] array = new Object[lines.size()];

        for (int n = 0;n < lines.size(); n++) {
            array[n] = StringOperations.createString(context, StringOperations.encodeRope(lines.get(n), UTF8Encoding.INSTANCE));
        }

        return Layouts.ARRAY.createArray(context.getCoreLibrary().getArrayFactory(), array, array.length);
    }

    public static void setMessage(DynamicObject exception, Object message) {
        Layouts.EXCEPTION.setMessage(exception, message);
    }

    public static DynamicObject createRubyException(DynamicObject rubyClass) {
        return Layouts.EXCEPTION.createException(Layouts.CLASS.getInstanceFactory(rubyClass), null, null);
    }

    public static DynamicObject createRubyException(DynamicObject rubyClass, Object message, Backtrace backtrace) {
        return Layouts.EXCEPTION.createException(Layouts.CLASS.getInstanceFactory(rubyClass), message, backtrace);
    }

    @CoreMethod(names = "initialize", optional = 1)
    public abstract static class InitializeNode extends CoreMethodArrayArgumentsNode {

        public InitializeNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        @Specialization
        public DynamicObject initialize(DynamicObject exception, NotProvided message) {
            setMessage(exception, nil());
            return exception;
        }

        @Specialization(guards = "wasProvided(message)")
        public DynamicObject initialize(DynamicObject exception, Object message) {
            setMessage(exception, message);
            return exception;
        }

    }

    @CoreMethod(names = "backtrace")
    public abstract static class BacktraceNode extends CoreMethodArrayArgumentsNode {

        @Child ReadHeadObjectFieldNode readCustomBacktrace;

        public BacktraceNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
            readCustomBacktrace = ReadHeadObjectFieldNodeGen.create(getContext(), "@custom_backtrace", null);
        }

        @Specialization
        public Object backtrace(DynamicObject exception) {
            final Object customBacktrace = readCustomBacktrace.execute(exception);
            if (customBacktrace != null) {
                return customBacktrace;
            } else if (Layouts.EXCEPTION.getBacktrace(exception) != null) {
                return backtraceAsRubyStringArray(getContext(), exception, Layouts.EXCEPTION.getBacktrace(exception));
            } else {
                return nil();
            }
        }

    }

    @RubiniusOnly
    @CoreMethod(names = "capture_backtrace!", optional = 1)
    public abstract static class CaptureBacktraceNode extends CoreMethodArrayArgumentsNode {

        public CaptureBacktraceNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        @Specialization
        public DynamicObject captureBacktrace(DynamicObject exception, NotProvided offset) {
            return captureBacktrace(exception, 1);
        }

        @TruffleBoundary
        @Specialization
        public DynamicObject captureBacktrace(DynamicObject exception, int offset) {
            Backtrace backtrace = RubyCallStack.getBacktrace(getContext(), this, offset, exception);
            Layouts.EXCEPTION.setBacktrace(exception, backtrace);
            return nil();
        }

    }

    @CoreMethod(names = "message")
    public abstract static class MessageNode extends CoreMethodArrayArgumentsNode {

        public MessageNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        @Specialization
        public Object message(DynamicObject exception) {
            final Object message = Layouts.EXCEPTION.getMessage(exception);
            if (message == null) {
                final String className = Layouts.MODULE.getFields(Layouts.BASIC_OBJECT.getLogicalClass(exception)).getName();
                return createString(StringOperations.encodeRope(className, UTF8Encoding.INSTANCE));
            } else {
                return message;
            }
        }

    }

    @CoreMethod(names = "allocate", constructor = true)
    public abstract static class AllocateNode extends CoreMethodArrayArgumentsNode {

        @Child private AllocateObjectNode allocateObjectNode;

        public AllocateNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
            allocateObjectNode = AllocateObjectNodeGen.create(context, sourceSection, null, null);
        }

        @Specialization
        public DynamicObject allocate(DynamicObject rubyClass) {
            return allocateObjectNode.allocate(rubyClass, null, null);
        }

    }
}
