/*
 * Copyright (c) 2013, 2014 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.nodes.core;

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.source.SourceSection;
import org.jruby.truffle.runtime.RubyContext;
import org.jruby.truffle.runtime.core.RubyArray;
import org.jruby.truffle.runtime.core.RubyMatchData;
import org.jruby.truffle.runtime.core.RubyString;
import org.jruby.truffle.runtime.util.ArrayUtils;

@CoreClass(name = "MatchData")
public abstract class MatchDataNodes {

    @CoreMethod(names = "[]", required = 1, lowerFixnumParameters = 0)
    public abstract static class GetIndexNode extends CoreMethodNode {

        public GetIndexNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public GetIndexNode(GetIndexNode prev) {
            super(prev);
        }

        @Specialization
        public Object getIndex(RubyMatchData matchData, int index) {
            notDesignedForCompilation();

            if (index >= matchData.getValues().length) {
                return getContext().getCoreLibrary().getNilObject();
            } else {
                return matchData.getValues()[index];
            }
        }

    }

    @CoreMethod(names = "captures")
    public abstract static class CapturesNode extends CoreMethodNode {

        public CapturesNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public CapturesNode(CapturesNode prev) {
            super(prev);
        }

        @Specialization
        public RubyArray toA(RubyMatchData matchData) {
            notDesignedForCompilation();

            return RubyArray.fromObjects(getContext().getCoreLibrary().getArrayClass(), matchData.getCaptures());
        }
    }

    @CoreMethod(names = "pre_match")
    public abstract static class PreMatchNode extends CoreMethodNode {

        public PreMatchNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public PreMatchNode(PreMatchNode prev) {
            super(prev);
        }

        @Specialization
        public RubyString preMatch(RubyMatchData matchData) {
            return matchData.getPre();
        }

    }

    @CoreMethod(names = "post_match")
    public abstract static class PostMatchNode extends CoreMethodNode {

        public PostMatchNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public PostMatchNode(PostMatchNode prev) {
            super(prev);
        }

        @Specialization
        public RubyString postMatch(RubyMatchData matchData) {
            return matchData.getPost();
        }

    }

    @CoreMethod(names = "to_a")
    public abstract static class ToANode extends CoreMethodNode {

        public ToANode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public ToANode(ToANode prev) {
            super(prev);
        }

        @Specialization
        public RubyArray toA(RubyMatchData matchData) {
            notDesignedForCompilation();

            return RubyArray.fromObjects(getContext().getCoreLibrary().getArrayClass(), matchData.getValues());
        }
    }

    @CoreMethod(names = "values_at", argumentsAsArray = true)
    public abstract static class ValuesAtNode extends CoreMethodNode {

        public ValuesAtNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public ValuesAtNode(ValuesAtNode prev) {
            super(prev);
        }

        @Specialization
        public RubyArray valuesAt(RubyMatchData matchData, Object[] args) {
            notDesignedForCompilation();

            final int[] indicies = new int[args.length];

            for (int n = 0; n < args.length; n++) {
                indicies[n] = (int) args[n];
            }

            return RubyArray.fromObjects(getContext().getCoreLibrary().getArrayClass(), matchData.valuesAt(indicies));
        }

    }

}
