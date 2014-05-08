/*
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.runtime.core.array;

import java.util.*;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.nodes.*;
import org.jruby.truffle.runtime.*;

/**
 * A store for an array of {@code int}.
 */
public final class IntegerArrayStore extends BaseArrayStore {

    private int[] values;

    public IntegerArrayStore() {
        this(new int[]{});
    }

    public IntegerArrayStore(int[] values) {
        this.values = values;
        size = values.length;
        capacity = values.length;
    }

    @Override
    public Object get(int normalisedIndex) {
        try {
            return getFixnum(normalisedIndex);
        } catch (UnexpectedResultException e) {
            return e.getResult();
        }
    }

    public int getFixnum(int normalisedIndex) throws UnexpectedResultException {
        if (normalisedIndex >= size) {
            throw new UnexpectedResultException(NilPlaceholder.INSTANCE);
        }

        return values[normalisedIndex];
    }

    @Override
    public ArrayStore getRange(int normalisedBegin, int truncatedNormalisedExclusiveEnd) {
        if (normalisedBegin >= size) {
            return null; // Represents Nil
        }

        return new IntegerArrayStore(Arrays.copyOfRange(values, normalisedBegin, truncatedNormalisedExclusiveEnd));
    }

    @Override
    public void set(int normalisedIndex, Object value) throws GeneraliseArrayStoreException {
        if (value instanceof Integer) {
            setFixnum(normalisedIndex, (int) value);
        } else {
            throw new GeneraliseArrayStoreException();
        }
    }

    public void setFixnum(int normalisedIndex, int value) throws GeneraliseArrayStoreException {
        if (normalisedIndex > size) {
            throw new GeneraliseArrayStoreException();
        }

        if (normalisedIndex == size) {
            push(value);
        } else {
            values[normalisedIndex] = value;
        }
    }

    @Override
    public void setRangeSingle(int normalisedBegin, int truncatedNormalisedExclusiveEnd, Object value) throws GeneraliseArrayStoreException {
        if (value instanceof Integer) {
            setRangeSingleFixnum(normalisedBegin, truncatedNormalisedExclusiveEnd, (int) value);
        } else {
            throw new GeneraliseArrayStoreException();
        }
    }

    public void setRangeSingleFixnum(int normalisedBegin, int truncatedNormalisedExclusiveEnd, int value) {
        // Is the range the whole array?

        if (normalisedBegin == 0 && truncatedNormalisedExclusiveEnd == size) {
            // Reset length and set the value.
            size = 1;
            values[0] = value;
        } else {
            // Delete the range, except for the first value.
            deleteSpace(normalisedBegin + 1, truncatedNormalisedExclusiveEnd - normalisedBegin - 1);

            // Set the value we left in.
            values[normalisedBegin] = value;
        }
    }

    @Override
    public void setRangeArray(int normalisedBegin, int normalisedExclusiveEnd, ArrayStore other) throws GeneraliseArrayStoreException {
        if (other instanceof IntegerArrayStore) {
            setRangeArrayFixnum(normalisedBegin, normalisedExclusiveEnd, (IntegerArrayStore) other);
        } else {
            throw new GeneraliseArrayStoreException();
        }
    }

    public void setRangeArrayFixnum(int normalisedBegin, int normalisedExclusiveEnd, IntegerArrayStore other) {
        setRangeArrayMatchingTypes(normalisedBegin, normalisedExclusiveEnd, other.values, other.size);
    }

    @Override
    public void insert(int normalisedIndex, Object value) throws GeneraliseArrayStoreException {
        if (value instanceof Integer) {
            insertFixnum(normalisedIndex, (int) value);
        } else {
            throw new GeneraliseArrayStoreException();
        }
    }

    public void insertFixnum(int normalisedIndex, int value) throws GeneraliseArrayStoreException {
        if (normalisedIndex > size) {
            throw new GeneraliseArrayStoreException();
        }

        createSpace(normalisedIndex, 1);
        values[normalisedIndex] = value;
    }

    @Override
    public void push(Object value) throws GeneraliseArrayStoreException {
        if (value instanceof Integer) {
            pushFixnum((int) value);
        } else {
            throw new GeneraliseArrayStoreException();
        }
    }

    public void pushFixnum(int value) {
        createSpaceAtEnd(1);
        values[size - 1] = value;
    }

    @Override
    public Object deleteAt(int normalisedIndex) {
        try {
            return deleteAtFixnum(normalisedIndex);
        } catch (UnexpectedResultException e) {
            return e.getResult();
        }
    }

    public int deleteAtFixnum(int normalisedIndex) throws UnexpectedResultException {
        if (normalisedIndex >= size) {
            CompilerDirectives.transferToInterpreter();
            throw new UnexpectedResultException(NilPlaceholder.INSTANCE);
        }

        final int value = values[normalisedIndex];

        deleteSpace(normalisedIndex, 1);

        return value;
    }

    @Override
    public ArrayStore dup() {
        return new IntegerArrayStore(Arrays.copyOf(values, size));
    }

    @Override
    public boolean contains(Object value) {
        final long longValue;

        if (value instanceof Integer) {
            longValue = (int) value;
        } else if (value instanceof Long) {
            longValue = (long) value;
        } else {
            return false;
        }

        for (int n = 0; n < size; n++) {
            if (values[n] == longValue) {
                return true;
            }
        }

        return false;
    }

    @Override
    public ArrayStore generalizeFor(Object type) {
        return new ObjectArrayStore(toObjectArray());
    }

    @Override
    public Object getIndicativeValue() {
        return 0;
    }

    @Override
    protected void setCapacityByCopying(int newCapacity) {
        values = Arrays.copyOf(values, newCapacity);
        capacity = values.length;
    }

    @Override
    protected void setCapacityWithNewArray(int newCapacity) {
        values = new int[newCapacity];
        capacity = values.length;
    }

    public int[] getValues() {
        return values;
    }

    @Override
    protected Object getValuesArrayObject() {
        return values;
    }

    @Override
    public Object[] toObjectArray() {
        final Object[] objectValues = new Object[size];

        // System.arraycopy will not box.

        for (int n = 0; n < size; n++) {
            objectValues[n] = values[n];
        }

        return objectValues;
    }

    @Override
    public boolean equals(ArrayStore other) {
        if (other instanceof IntegerArrayStore) {
            return equals((IntegerArrayStore) other);
        } else {
            return super.equals(other);
        }
    }

    public boolean equals(IntegerArrayStore other) {
        if (other == null) {
            return false;
        } else if (other == this) {
            return true;
        } else if (other.size != size) {
            return false;
        } else if (other.capacity == capacity) {
            return Arrays.equals(other.values, values);
        } else {
            for (int n = 0; n < size; n++) {
                if (other.values[n] != values[n]) {
                    return false;
                }
            }

            return true;
        }
    }
}
