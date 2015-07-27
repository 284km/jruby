/*
 * Copyright (c) 2015 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.translator;

import java.util.ArrayList;
import java.util.List;

import org.jruby.ast.RestArgNode;
import org.jruby.truffle.nodes.RubyNode;
import org.jruby.truffle.nodes.arguments.MissingArgumentBehaviour;
import org.jruby.truffle.nodes.arguments.ReadPreArgumentNode;
import org.jruby.truffle.nodes.control.SequenceNode;
import org.jruby.truffle.nodes.literal.LiteralNode;
import org.jruby.truffle.runtime.RubyContext;

import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.source.SourceSection;

/**
 * Produces code to reload arguments from local variables back into the
 * arguments array. Only works for simple cases. Used for zsuper calls which
 * pass the same arguments, but will pick up modifications made to them in the
 * method so far.
 */
public class ReloadArgumentsTranslator extends Translator {

    private final BodyTranslator methodBodyTranslator;

    private int index = 0;
    private boolean hasRestParameter = false;

    public ReloadArgumentsTranslator(Node currentNode, RubyContext context, Source source, BodyTranslator methodBodyTranslator) {
        super(currentNode, context, source);
        this.methodBodyTranslator = methodBodyTranslator;
    }

    @Override
    public RubyNode visitArgsNode(org.jruby.ast.ArgsNode node) {
        final SourceSection sourceSection = translate(node.getPosition());

        final List<RubyNode> sequence = new ArrayList<>();

        if (node.getPre() != null) {
            for (org.jruby.ast.Node arg : node.getPre().children()) {
                sequence.add(arg.accept(this));
                index++;
            }
        }

        if (node.getOptArgs() != null) {
            for (org.jruby.ast.Node arg : node.getOptArgs().children()) {
                sequence.add(arg.accept(this));
                index++;
            }
        }

        if (node.hasRestArg()) {
            hasRestParameter = true;
            sequence.add(node.getRestArgNode().accept(this));
        }

        return SequenceNode.sequenceNoFlatten(context, sourceSection, sequence);
    }

    @Override
    public RubyNode visitArgumentNode(org.jruby.ast.ArgumentNode node) {
        final SourceSection sourceSection = translate(node.getPosition());
        return methodBodyTranslator.getEnvironment().findLocalVarNode(node.getName(), sourceSection);
    }

    @Override
    public RubyNode visitOptArgNode(org.jruby.ast.OptArgNode node) {
        final SourceSection sourceSection = translate(node.getPosition());
        return methodBodyTranslator.getEnvironment().findLocalVarNode(node.getName(), sourceSection);
    }

    @Override
    public RubyNode visitMultipleAsgnNode(org.jruby.ast.MultipleAsgnNode node) {
        final SourceSection sourceSection = translate(node.getPosition());
        return new ReadPreArgumentNode(context, sourceSection, index, MissingArgumentBehaviour.NIL);
    }

    @Override
    public RubyNode visitRestArgNode(RestArgNode node) {
        final SourceSection sourceSection = translate(node.getPosition());
        return methodBodyTranslator.getEnvironment().findLocalVarNode(node.getName(), sourceSection);
    }

    @Override
    protected RubyNode defaultVisit(org.jruby.ast.Node node) {
        final SourceSection sourceSection = translate(node.getPosition());
        return new LiteralNode(context, sourceSection, context.getCoreLibrary().getNilObject());
    }

    @Override
    protected String getIdentifier() {
        return methodBodyTranslator.getIdentifier();
    }

    public boolean isSplatted() {
        return hasRestParameter;
    }

}
