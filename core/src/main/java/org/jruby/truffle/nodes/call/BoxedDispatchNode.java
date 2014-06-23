/*
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.nodes.call;

import com.oracle.truffle.api.frame.*;
import org.jruby.truffle.runtime.*;
import org.jruby.truffle.runtime.core.*;

/**
 * A node in the dispatch chain that expects the receiver to be an object boxed into a full
 * {@link RubyBasicObject}.
 */
public abstract class BoxedDispatchNode extends DispatchNode {

    public BoxedDispatchNode(RubyContext context) {
        super(context);
    }

    public abstract Object dispatch(VirtualFrame frame, RubyBasicObject receiverObject, RubyProc blockObject, Object[] argumentsObjects);

    public abstract boolean doesRespondTo(VirtualFrame frame, RubyBasicObject receiverObject);

}
