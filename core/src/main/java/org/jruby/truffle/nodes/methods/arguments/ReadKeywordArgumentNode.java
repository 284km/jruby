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
import org.jruby.truffle.nodes.RubyNode;
import org.jruby.truffle.runtime.RubyArguments;
import org.jruby.truffle.runtime.RubyContext;
import org.jruby.truffle.runtime.core.RubyHash;
import org.jruby.truffle.runtime.hash.Entry;
import org.jruby.truffle.runtime.hash.HashOperations;

public class ReadKeywordArgumentNode extends RubyNode {

    private final int minimum;
    private final String name;
    @Child protected RubyNode defaultValue;

    public ReadKeywordArgumentNode(RubyContext context, SourceSection sourceSection, int minimum, String name, RubyNode defaultValue) {
        super(context, sourceSection);
        this.minimum = minimum;
        this.name = name;
        this.defaultValue = defaultValue;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        notDesignedForCompilation();

        final RubyHash hash = getKeywordsHash(frame);

        if (hash == null) {
            return defaultValue.execute(frame);
        }

        Object value = null;

        for (Entry entry : HashOperations.verySlowToEntries(hash)) {
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

    private RubyHash getKeywordsHash(VirtualFrame frame) {
        if (RubyArguments.getUserArgumentsCount(frame.getArguments()) <= minimum) {
            return null;
        }

        final Object lastArgument = RubyArguments.getUserArgument(frame.getArguments(), RubyArguments.getUserArgumentsCount(frame.getArguments()) - 1);

        if (lastArgument instanceof RubyHash) {
            return (RubyHash) lastArgument;
        }

        return null;
    }

}
