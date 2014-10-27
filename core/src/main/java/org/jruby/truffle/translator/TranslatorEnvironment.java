/*
 * Copyright (c) 2013, 2014 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.translator;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.impl.DefaultFrameTypeConversion;
import com.oracle.truffle.api.source.*;
import com.oracle.truffle.api.frame.*;
import org.jruby.truffle.nodes.*;
import org.jruby.truffle.nodes.methods.locals.*;
import org.jruby.truffle.runtime.*;
import org.jruby.truffle.runtime.core.*;
import org.jruby.truffle.runtime.methods.*;

public class TranslatorEnvironment {

    private final RubyContext context;

    private final FrameDescriptor frameDescriptor;

    private final List<FrameSlot> flipFlopStates = new ArrayList<>();

    private TranslatorDriver parser;
    private final long returnID;
    private final boolean isBlock;

    private final boolean ownScopeForAssignments;
    private final boolean neverAssignInParentScope;

    protected final TranslatorEnvironment parent;
    private boolean needsDeclarationFrame = true;
    private final SharedMethodInfo sharedMethodInfo;

    private final String namedMethodName;

    // TODO(CS): overflow?
    private static AtomicInteger tempIndex = new AtomicInteger();

    public boolean hasRestParameter = false;

    public TranslatorEnvironment(RubyContext context, TranslatorEnvironment parent, FrameDescriptor frameDescriptor, TranslatorDriver parser, long returnID, boolean ownScopeForAssignments,
                    boolean neverAssignInParentScope, SharedMethodInfo sharedMethodInfo, String namedMethodName, boolean isBlock) {
        this.context = context;
        this.parent = parent;
        this.frameDescriptor = frameDescriptor;
        this.parser = parser;
        this.returnID = returnID;
        this.ownScopeForAssignments = ownScopeForAssignments;
        this.neverAssignInParentScope = neverAssignInParentScope;
        this.sharedMethodInfo = sharedMethodInfo;
        this.namedMethodName = namedMethodName;
        this.isBlock = isBlock;
    }

    public TranslatorEnvironment(RubyContext context, TranslatorEnvironment parent, TranslatorDriver parser, long returnID, boolean ownScopeForAssignments, boolean neverAssignInParentScope,
                    SharedMethodInfo methodIdentifier, String namedMethodName, boolean isBlock) {
        this(context, parent, new FrameDescriptor(new RubyFrameTypeConversion(context.getCoreLibrary().getNilObject())), parser, returnID, ownScopeForAssignments, neverAssignInParentScope, methodIdentifier, namedMethodName, isBlock);
    }

    public TranslatorEnvironment getParent() {
        return parent;
    }

    public TranslatorEnvironment getParent(int level) {
        assert level >= 0;
        if (level == 0) {
            return this;
        } else {
            return parent.getParent(level - 1);
        }
    }

    public FrameSlot declareVar(String name) {
        return getFrameDescriptor().findOrAddFrameSlot(name);
    }

    public SharedMethodInfo findMethodForLocalVar(String name) {
        TranslatorEnvironment current = this;
        do {
            FrameSlot slot = current.getFrameDescriptor().findFrameSlot(name);
            if (slot != null) {
                return current.sharedMethodInfo;
            }

            current = current.parent;
        } while (current != null);

        return null;
    }

    public RubyNode findLocalVarNode(String name, SourceSection sourceSection) {
        TranslatorEnvironment current = this;
        int level = -1;
        try {
            do {
                level++;
                FrameSlot slot = current.getFrameDescriptor().findFrameSlot(name);
                if (slot != null) {
                    if (level == 0) {
                        return ReadLocalVariableNodeFactory.create(context, sourceSection, slot);
                    } else {
                        return ReadLevelVariableNodeFactory.create(context, sourceSection, slot, level);
                    }
                }

                current = current.parent;
            } while (current != null);
        } finally {
            if (current != null) {
                current = this;
                while (level-- > 0) {
                    current.needsDeclarationFrame = true;
                    current = current.parent;
                }
            }
        }

        return null;
    }

    public void setNeedsDeclarationFrame() {
        needsDeclarationFrame = true;
    }

    public boolean needsDeclarationFrame() {
        return needsDeclarationFrame;
    }

    public FrameDescriptor getFrameDescriptor() {
        return frameDescriptor;
    }

    public String allocateLocalTemp(String indicator) {
        final String name = "rubytruffle_temp_" + indicator + "_" + tempIndex.getAndIncrement();
        declareVar(name);
        return name;
    }

    public long getReturnID() {
        return returnID;
    }

    public TranslatorDriver getParser() {
        return parser;
    }

    public boolean hasOwnScopeForAssignments() {
        return ownScopeForAssignments;
    }

    public boolean getNeverAssignInParentScope() {
        return neverAssignInParentScope;
    }

    public void addMethodDeclarationSlots() {
        frameDescriptor.addFrameSlot(RubyModule.VISIBILITY_FRAME_SLOT_ID);
        frameDescriptor.addFrameSlot(RubyModule.MODULE_FUNCTION_FLAG_FRAME_SLOT_ID);
    }

    public SharedMethodInfo getSharedMethodInfo() {
        return sharedMethodInfo;
    }

    public List<FrameSlot> getFlipFlopStates() {
        return flipFlopStates;
    }

    public String getNamedMethodName() {
        return namedMethodName;
    }

    public boolean isBlock() {
        return isBlock;
    }
}
