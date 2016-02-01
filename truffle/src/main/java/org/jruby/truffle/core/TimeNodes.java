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
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.source.SourceSection;
import org.jcodings.specific.UTF8Encoding;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.jruby.truffle.nodes.RubyNode;
import org.jruby.truffle.nodes.objects.AllocateObjectNode;
import org.jruby.truffle.nodes.objects.AllocateObjectNodeGen;
import org.jruby.truffle.nodes.rubinius.TimePrimitiveNodes;
import org.jruby.truffle.nodes.time.ReadTimeZoneNode;
import org.jruby.truffle.runtime.NotProvided;
import org.jruby.truffle.runtime.RubyContext;
import org.jruby.truffle.runtime.layouts.Layouts;

@CoreClass(name = "Time")
public abstract class TimeNodes {

    private static final DateTime ZERO = new DateTime(0);

    // We need it to copy the internal data for a call to Kernel#clone.
    @CoreMethod(names = "initialize_copy", required = 1)
    public abstract static class InitializeCopyNode extends CoreMethodArrayArgumentsNode {

        public InitializeCopyNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        @Specialization(guards = "isRubyTime(from)")
        public Object initializeCopy(DynamicObject self, DynamicObject from) {
            Layouts.TIME.setDateTime(self, Layouts.TIME.getDateTime(from));
            Layouts.TIME.setNSec(self, Layouts.TIME.getNSec(from));
            Layouts.TIME.setOffset(self, Layouts.TIME.getOffset(from));
            Layouts.TIME.setRelativeOffset(self, Layouts.TIME.getRelativeOffset(from));
            return self;
        }

    }

    // Not a core method, used to simulate Rubinius @is_gmt.
    @NodeChild(type = RubyNode.class, value = "self")
    public abstract static class InternalGMTNode extends CoreMethodNode {

        public InternalGMTNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        @Specialization
        public boolean internalGMT(DynamicObject time) {
            return Layouts.TIME.getIsUtc(time);
        }
    }

    // Not a core method, used to simulate Rubinius @offset.
    @NodeChild(type = RubyNode.class, value = "self")
    public abstract static class InternalOffsetNode extends CoreMethodNode {

        public InternalOffsetNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        @TruffleBoundary
        @Specialization
        public Object internalOffset(DynamicObject time) {
            final Object offset = Layouts.TIME.getOffset(time);
            if (offset == nil()) {
                return Layouts.TIME.getDateTime(time).getZone().getOffset(Layouts.TIME.getDateTime(time).getMillis()) / 1_000;
            } else {
                return offset;
            }
        }
    }

    @CoreMethod(names = "localtime_internal", optional = 1)
    public abstract static class LocalTimeNode extends CoreMethodArrayArgumentsNode {
        @Child private ReadTimeZoneNode readTimeZoneNode;

        public LocalTimeNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
            readTimeZoneNode = new ReadTimeZoneNode(context, sourceSection);
        }

        @Specialization
        public DynamicObject localtime(VirtualFrame frame, DynamicObject time, NotProvided offset) {
            final DynamicObject zoneName = (DynamicObject) readTimeZoneNode.execute(frame);
            final DateTimeZone dateTimeZone = TimePrimitiveNodes.TimeZoneParser.parse(this, StringOperations.getString(getContext(), zoneName));
            final String shortZoneName = TimePrimitiveNodes.TimeZoneParser.getShortZoneName(time, dateTimeZone);
            final DynamicObject zone = createString(StringOperations.encodeRope(shortZoneName, UTF8Encoding.INSTANCE));
            final DateTime dateTime = Layouts.TIME.getDateTime(time);

            Layouts.TIME.setIsUtc(time, false);
            Layouts.TIME.setRelativeOffset(time, false);
            Layouts.TIME.setZone(time, zone);
            Layouts.TIME.setDateTime(time, dateTime.withZone(dateTimeZone));

            return time;
        }

        @Specialization
        public DynamicObject localtime(DynamicObject time, long offset) {
            final DateTime dateTime = Layouts.TIME.getDateTime(time);
            final DateTimeZone zone = getDateTimeZone((int) offset);

            Layouts.TIME.setIsUtc(time, false);
            Layouts.TIME.setRelativeOffset(time, true);
            Layouts.TIME.setZone(time, nil());
            Layouts.TIME.setDateTime(time, dateTime.withZone(zone));

            return time;
        }

        @TruffleBoundary
        public DateTimeZone getDateTimeZone(int offset) {
            return DateTimeZone.forOffsetMillis(offset * 1000);
        }

    }

    @CoreMethod(names = "gmtime")
    public abstract static class GmTimeNode extends CoreMethodArrayArgumentsNode {

        public GmTimeNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        @Specialization
        public DynamicObject localtime(DynamicObject time) {
            final DateTime dateTime = Layouts.TIME.getDateTime(time);

            Layouts.TIME.setIsUtc(time, true);
            Layouts.TIME.setRelativeOffset(time, false);
            Layouts.TIME.setZone(time, nil());
            Layouts.TIME.setDateTime(time, dateTime.withZone(DateTimeZone.UTC));

            return time;
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
            return allocateObjectNode.allocate(rubyClass, ZERO, 0, getContext().getCoreLibrary().getNilObject(),
                    0, false, false);
        }

    }
}
