/*
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.cext;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.interop.CanResolve;
import com.oracle.truffle.api.interop.MessageResolution;
import com.oracle.truffle.api.interop.Resolve;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.nodes.Node;
import org.jruby.truffle.RubyLanguage;
import org.jruby.truffle.language.objects.ReadObjectFieldNode;
import org.jruby.truffle.language.objects.ReadObjectFieldNodeGen;
import org.jruby.truffle.language.objects.WriteObjectFieldNode;
import org.jruby.truffle.language.objects.WriteObjectFieldNodeGen;

@MessageResolution(
        receiverType = TypedDataAdapter.class,
        language = RubyLanguage.class
)
public class TypedDataMessageResolution {

    @CanResolve
    public abstract static class TypedDataCheckNode extends Node {

        protected static boolean test(TruffleObject receiver) {
            return receiver instanceof TypedDataAdapter;
        }

    }

    @Resolve(message = "READ")
    public static abstract class TypedDataReadNode extends Node {

        @Child private ReadObjectFieldNode readDataNode;

        protected Object access(TypedDataAdapter typedDataAdapter, long index) {
            if (index == 0) {
                if (readDataNode == null) {
                    CompilerDirectives.transferToInterpreterAndInvalidate();
                    readDataNode = insert(ReadObjectFieldNodeGen.create("@data", 0));
                }

                return readDataNode.execute(typedDataAdapter.getObject());
            } else {
                CompilerDirectives.transferToInterpreter();
                throw new UnsupportedOperationException();
            }
        }

    }

    @Resolve(message = "WRITE")
    public static abstract class TypedDataWriteNode extends Node {

        @Child private WriteObjectFieldNode writeDataNode;

        protected Object access(TypedDataAdapter typedDataAdapter, int index, Object value) {
            if (index == 0) {
                if (writeDataNode == null) {
                    CompilerDirectives.transferToInterpreterAndInvalidate();
                    writeDataNode = insert(WriteObjectFieldNodeGen.create("@data"));
                }

                writeDataNode.execute(typedDataAdapter.getObject(), value);
            } else {
                CompilerDirectives.transferToInterpreter();
                throw new UnsupportedOperationException();
            }

            return value;
        }

    }

}
