/*
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.parser;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import org.jruby.truffle.Log;
import org.jruby.truffle.language.RubyNode;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

public class LazyTranslationNode extends RubyNode {

    private final Supplier<RubyNode> resolver;

    public LazyTranslationNode(Supplier<RubyNode> resolver) {
        this.resolver = resolver;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        CompilerDirectives.transferToInterpreterAndInvalidate();
        final RubyNode resolved = resolver.get();
        replace(resolved, "lazy translation node resolved");
        return resolved.execute(frame);
    }

}
