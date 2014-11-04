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

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.nodes.NodeCost;
import com.oracle.truffle.api.source.SourceSection;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.NodeInfo;
import org.jruby.truffle.nodes.RubyNode;
import org.jruby.truffle.runtime.RubyContext;

@NodeInfo(cost = NodeCost.NONE)
public class BehaveAsBlockNode extends RubyNode {

    private @CompilationFinal boolean behaveAsBlock;

    public BehaveAsBlockNode(RubyContext context, SourceSection sourceSection, boolean behaveAsBlock) {
        super(context, sourceSection);
        this.behaveAsBlock = behaveAsBlock;
    }

    @Override
    public boolean executeBoolean(VirtualFrame frame) {
        return behaveAsBlock;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        return executeBoolean(frame);
    }

    public void setBehaveAsBlock(boolean behaveAsBlock) {
        CompilerAsserts.neverPartOfCompilation();

        this.behaveAsBlock = behaveAsBlock;

        // No need to deoptimize, as we're only doing this during clone
    }

}
