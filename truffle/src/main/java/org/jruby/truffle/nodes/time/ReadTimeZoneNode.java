/*
 * Copyright (c) 2015 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.nodes.time;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.source.SourceSection;
import com.oracle.truffle.api.utilities.ConditionProfile;
import org.jcodings.specific.UTF8Encoding;
import org.joda.time.DateTimeZone;
import org.jruby.RubyString;
import org.jruby.truffle.nodes.RubyGuards;
import org.jruby.truffle.nodes.RubyNode;
import org.jruby.truffle.nodes.constants.ReadLiteralConstantNode;
import org.jruby.truffle.nodes.dispatch.CallDispatchHeadNode;
import org.jruby.truffle.nodes.dispatch.DispatchHeadNodeFactory;
import org.jruby.truffle.nodes.literal.LiteralNode;
import org.jruby.truffle.runtime.RubyContext;
import org.jruby.truffle.runtime.layouts.Layouts;
import org.jruby.util.ByteList;
import org.jruby.util.StringSupport;
import org.jruby.util.io.BlockingIO;

public class ReadTimeZoneNode extends RubyNode {
    
    @Child private CallDispatchHeadNode hashNode;
    @Child private ReadLiteralConstantNode envNode;

    private final ConditionProfile tzNilProfile = ConditionProfile.createBinaryProfile();
    private final ConditionProfile tzStringProfile = ConditionProfile.createBinaryProfile();

    private static final ByteList defaultZone = RubyString.encodeBytelist(DateTimeZone.getDefault().toString(), UTF8Encoding.INSTANCE);
    private final DynamicObject TZ;
    
    public ReadTimeZoneNode(RubyContext context, SourceSection sourceSection) {
        super(context, sourceSection);
        hashNode = DispatchHeadNodeFactory.createMethodCall(context);
        envNode = new ReadLiteralConstantNode(context, sourceSection,
                new LiteralNode(context, sourceSection, getContext().getCoreLibrary().getObjectClass()), "ENV");
        TZ = Layouts.STRING.createString(getContext().getCoreLibrary().getStringFactory(), RubyString.encodeBytelist("TZ", UTF8Encoding.INSTANCE), StringSupport.CR_7BIT, null);
    }

    @Override
    public Object execute(VirtualFrame frame) {
        final Object tz = hashNode.call(frame, envNode.execute(frame), "[]", null, TZ);

        // TODO CS 4-May-15 not sure how TZ ends up being nil

        if (tzNilProfile.profile(tz == nil())) {
            return Layouts.STRING.createString(getContext().getCoreLibrary().getStringFactory(), defaultZone.dup(), StringSupport.CR_UNKNOWN, null);
        } else if (tzStringProfile.profile(RubyGuards.isRubyString(tz))) {
            return tz;
        } else {
            throw new UnsupportedOperationException();
        }
    }

}
