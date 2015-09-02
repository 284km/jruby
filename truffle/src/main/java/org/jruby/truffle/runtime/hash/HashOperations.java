/*
 * Copyright (c) 2013, 2015 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.runtime.hash;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.object.DynamicObject;
import org.jruby.truffle.nodes.RubyGuards;
import org.jruby.truffle.nodes.core.hash.HashGuards;
import org.jruby.truffle.runtime.Options;
import org.jruby.truffle.runtime.layouts.Layouts;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

public abstract class HashOperations {

    public static boolean verifyStore(DynamicObject hash) {
        return verifyStore(Layouts.HASH.getStore(hash), Layouts.HASH.getSize(hash), Layouts.HASH.getFirstInSequence(hash), Layouts.HASH.getLastInSequence(hash));
    }

    public static boolean verifyStore(Object store, int size, Entry firstInSequence, Entry lastInSequence) {
        assert store == null || store instanceof Object[] || store instanceof Entry[];

        if (store == null) {
            assert size == 0;
            assert firstInSequence == null;
            assert lastInSequence == null;
        }

        if (store instanceof Entry[]) {
            assert lastInSequence == null || lastInSequence.getNextInSequence() == null;

            final Entry[] entryStore = (Entry[]) store;

            Entry foundFirst = null;
            Entry foundLast = null;
            int foundSizeBuckets = 0;

            for (int n = 0; n < entryStore.length; n++) {
                Entry entry = entryStore[n];

                while (entry != null) {
                    foundSizeBuckets++;

                    if (entry == firstInSequence) {
                        assert foundFirst == null;
                        foundFirst = entry;
                    }

                    if (entry == lastInSequence) {
                        assert foundLast == null;
                        foundLast = entry;
                    }

                    entry = entry.getNextInLookup();
                }
            }

            assert foundSizeBuckets == size;
            assert firstInSequence == foundFirst;
            assert lastInSequence == foundLast;

            int foundSizeSequence = 0;
            Entry entry = firstInSequence;

            while (entry != null) {
                foundSizeSequence++;

                if (entry.getNextInSequence() == null) {
                    assert entry == lastInSequence;
                } else {
                    assert entry.getNextInSequence().getPreviousInSequence() == entry;
                }

                entry = entry.getNextInSequence();

                assert entry != firstInSequence;
            }

            assert foundSizeSequence == size : String.format("%d %d", foundSizeSequence, size);
        } else if (store instanceof Object[]) {
            assert ((Object[]) store).length == Options.TRUFFLE_HASH_PACKED_ARRAY_MAX * PackedArrayStrategy.ELEMENTS_PER_ENTRY : ((Object[]) store).length;

            final Object[] packedStore = (Object[]) store;

            for (int n = 0; n < Options.TRUFFLE_HASH_PACKED_ARRAY_MAX; n++) {
                if (n < size) {
                    assert packedStore[n * 2] != null;
                    assert packedStore[n * 2 + 1] != null;
                }
            }

            assert firstInSequence == null;
            assert lastInSequence == null;
        }

        return true;
    }

    @CompilerDirectives.TruffleBoundary
    public static Iterator<Map.Entry<Object, Object>> iterateKeyValues(DynamicObject hash) {
        assert RubyGuards.isRubyHash(hash);

        if (HashGuards.isNullHash(hash)) {
            return Collections.emptyIterator();
        } if (HashGuards.isPackedHash(hash)) {
            return PackedArrayStrategy.iterateKeyValues((Object[]) Layouts.HASH.getStore(hash), Layouts.HASH.getSize(hash));
        } else if (HashGuards.isBucketHash(hash)) {
            return BucketsStrategy.iterateKeyValues(Layouts.HASH.getFirstInSequence(hash));
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @CompilerDirectives.TruffleBoundary
    public static Iterable<Map.Entry<Object, Object>> iterableKeyValues(final DynamicObject hash) {
        assert RubyGuards.isRubyHash(hash);

        return new Iterable<Map.Entry<Object, Object>>() {

            @Override
            public Iterator<Map.Entry<Object, Object>> iterator() {
                return iterateKeyValues(hash);
            }

        };
    }

}
