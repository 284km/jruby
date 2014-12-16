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

import java.util.*;

public class ReadKeywordRestArgumentNode extends RubyNode {

    private final int minimum;
    private final String[] excludedKeywords;

    public ReadKeywordRestArgumentNode(RubyContext context, SourceSection sourceSection, int minimum, String[] excludedKeywords) {
        super(context, sourceSection);
        this.minimum = minimum;
        this.excludedKeywords = excludedKeywords;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        notDesignedForCompilation();

        final RubyHash hash = getKeywordsHash(frame);

        if (hash == null) {
            return new RubyHash(getContext().getCoreLibrary().getHashClass(), null, null, null, 0, null);
        }

        final List<Entry> entries = new ArrayList<>();

        outer: for (Entry entry : hash.verySlowToEntries()) {
            for (String excludedKeyword : excludedKeywords) {
                if (excludedKeyword.toString().equals(entry.getKey().toString())) {
                    continue outer;
                }
            }

            entries.add(new Entry(entry.getKey(), entry.getValue()));
        }

        return RubyHash.verySlowFromEntries(getContext(), entries);
    }

    private RubyHash getKeywordsHash(VirtualFrame frame) {
        // TODO(CS): duplicated in ReadKeywordArgumentNode

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
