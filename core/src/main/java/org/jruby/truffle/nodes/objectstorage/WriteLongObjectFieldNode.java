/*
 * Copyright (c) 2013, 2014 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.nodes.objectstorage;

import com.oracle.truffle.api.nodes.NodeCost;
import com.oracle.truffle.api.nodes.NodeInfo;
import org.jruby.truffle.runtime.objectstorage.LongStorageLocation;
import org.jruby.truffle.runtime.objectstorage.ObjectLayout;
import org.jruby.truffle.runtime.objectstorage.ObjectStorage;

@NodeInfo(cost = NodeCost.POLYMORPHIC)
public class WriteLongObjectFieldNode extends WriteObjectFieldChainNode {

    private final ObjectLayout objectLayout;
    private final LongStorageLocation storageLocation;

    public WriteLongObjectFieldNode(ObjectLayout objectLayout, LongStorageLocation storageLocation, WriteObjectFieldNode next) {
        super(next);
        this.objectLayout = objectLayout;
        this.storageLocation = storageLocation;
    }

    @Override
    public void execute(ObjectStorage object, long value) {
        if (object.getObjectLayout() == objectLayout) {
            storageLocation.writeLong(object, value);
        } else {
            next.execute(object, value);
        }
    }

    @Override
    public void execute(ObjectStorage object, Object value) {
        if (value instanceof Long) {
            execute(object, (long) value);
        } else {
            next.execute(object, value);
        }
    }

}
