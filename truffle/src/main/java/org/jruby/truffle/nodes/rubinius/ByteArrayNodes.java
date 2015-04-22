/*
 * Copyright (c) 2015 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.nodes.rubinius;

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.source.SourceSection;
import org.jruby.truffle.nodes.core.CoreClass;
import org.jruby.truffle.nodes.core.CoreMethod;
import org.jruby.truffle.nodes.core.CoreMethodNode;
import org.jruby.truffle.runtime.RubyContext;
import org.jruby.truffle.runtime.core.RubyString;
import org.jruby.truffle.runtime.rubinius.RubiniusByteArray;
import org.jruby.util.ByteList;

@CoreClass(name = "Rubinius::ByteArray")
public abstract class ByteArrayNodes {

    @CoreMethod(names = "get_byte", required = 1)
    public abstract static class GetByteNode extends CoreMethodNode {

        public GetByteNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public GetByteNode(GetByteNode prev) {
            super(prev);
        }

        @Specialization
        public int getByte(RubiniusByteArray bytes, int index) {
            return bytes.getBytes().get(index);
        }

    }

    @CoreMethod(names = "locate", required = 3)
    public abstract static class LocateNode extends CoreMethodNode {

        public LocateNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        public LocateNode(LocateNode prev) {
            super(prev);
        }

        @Specialization
        public Object getByte(RubiniusByteArray bytes, RubyString pattern, int start, int length) {
            final int index = new ByteList(bytes.getBytes().unsafeBytes(), start, length)
                    .indexOf(pattern.getBytes());

            if (index == -1) {
                return nil();
            } else {
                return start + index + pattern.length();
            }
        }

    }

}
