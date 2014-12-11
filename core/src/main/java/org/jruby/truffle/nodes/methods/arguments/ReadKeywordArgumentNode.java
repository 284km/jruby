/*
 * Copyright (c) 2014 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.nodes.methods.arguments;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.source.SourceSection;
import com.oracle.truffle.api.utilities.BranchProfile;
import org.jruby.truffle.nodes.RubyNode;
import org.jruby.truffle.nodes.RubyValueProfile;
import org.jruby.truffle.runtime.RubyArguments;
import org.jruby.truffle.runtime.RubyContext;
import org.jruby.truffle.runtime.UndefinedPlaceholder;
import org.jruby.truffle.runtime.core.RubyHash;
import org.jruby.truffle.runtime.core.RubyString;

import java.util.Map;

public class ReadKeywordArgumentNode extends RubyNode {

    private final String name;
    @Child protected RubyNode defaultValue;

    public ReadKeywordArgumentNode(RubyContext context, SourceSection sourceSection, String name, RubyNode defaultValue) {
        super(context, sourceSection);
        this.name = name;
        this.defaultValue = defaultValue;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        notDesignedForCompilation();

        final int last = RubyArguments.getUserArgumentsCount(frame.getArguments()) - 1;

        if (last == -1) {
            return defaultValue.execute(frame);
        }

        final Object hashValue = RubyArguments.getUserArgument(frame.getArguments(), last);

        if (!(hashValue instanceof RubyHash)) {
            return defaultValue.execute(frame);
        }

        final RubyHash hash = (RubyHash) hashValue;

        Object value = null;

        for (Map.Entry<Object, Object> entry : hash.slowToMap().entrySet()) {
            if (entry.getKey().toString().equals(name)) {
                value = entry.getValue();
                break;
            }
        }

        if (value == null) {
            return defaultValue.execute(frame);
        }

        return value;
    }

}
