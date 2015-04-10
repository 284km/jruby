/*
 * Copyright (c) 2015 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.pack.nodes;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.TypeSystemReference;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.Node;
import org.jruby.truffle.pack.runtime.PackFrame;
import org.jruby.truffle.pack.runtime.exceptions.TooFewArgumentsException;
import org.jruby.util.ByteList;

import java.util.Arrays;

/**
 * The root of the pack nodes.
 * <p>
 * Contains methods to change the state of the parser which is stored in the
 * frame.
 */
@ImportStatic(PackGuards.class)
@TypeSystemReference(PackTypes.class)
public abstract class PackNode extends Node {

    public abstract Object execute(VirtualFrame frame);

    /**
     * Get the length of the source array.
     */
    public int getSourceLength(VirtualFrame frame) {
        try {
            return frame.getInt(PackFrame.INSTANCE.getSourceLengthSlot());
        } catch (FrameSlotTypeException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Set the current position we are reading from in the source array.
     */
    protected int getSourcePosition(VirtualFrame frame) {
        try {
            return frame.getInt(PackFrame.INSTANCE.getSourcePositionSlot());
        } catch (FrameSlotTypeException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Set the current position we will read from next in the source array.
     */
    protected void setSourcePosition(VirtualFrame frame, int position) {
        frame.setInt(PackFrame.INSTANCE.getSourcePositionSlot(), position);
    }

    /**
     * Advanced the position we are reading from in the source array by one
     * element.
     */
    protected int advanceSourcePosition(VirtualFrame frame) {
        final int sourcePosition = getSourcePosition(frame);

        if (sourcePosition == getSourceLength(frame)) {
            CompilerDirectives.transferToInterpreter();
            throw new TooFewArgumentsException();
        }

        setSourcePosition(frame, sourcePosition + 1);

        return sourcePosition;
    }

    /**
     * Get the output array we are writing to.
     */
    protected byte[] getOutput(VirtualFrame frame) {
        try {
            return (byte[]) frame.getObject(PackFrame.INSTANCE.getOutputSlot());
        } catch (FrameSlotTypeException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Set the output array we are writing to. This should never be used in the
     * compiled code - having to change the output array to resize is is a
     * deoptimizing action.
     */
    protected void setOutput(VirtualFrame frame, byte[] output) {
        CompilerAsserts.neverPartOfCompilation();
        frame.setObject(PackFrame.INSTANCE.getOutputSlot(), output);
    }

    /**
     * Get the current position we are writing to the in the output array.
     */
    protected int getOutputPosition(VirtualFrame frame) {
        try {
            return frame.getInt(PackFrame.INSTANCE.getOutputPositionSlot());
        } catch (FrameSlotTypeException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Set the current position we are writing to in the output array.
     */
    protected void setOutputPosition(VirtualFrame frame, int position) {
        frame.setInt(PackFrame.INSTANCE.getOutputPositionSlot(), position);
    }

    /**
     * Set the output to be tainted.
     */
    protected void setTainted(VirtualFrame frame) {
        frame.setBoolean(PackFrame.INSTANCE.getTaintSlot(), true);
    }

    /**
     * Write an array of bytes to the output.
     */
    protected void writeBytes(VirtualFrame frame, byte... values) {
        writeBytes(frame, values, 0, values.length);
    }

    /**
     * Write a {@link ByteList} to the output.
     */
    protected void writeBytes(VirtualFrame frame, ByteList values) {
        writeBytes(frame, values.getUnsafeBytes(), values.begin(), values.length());
    }

    /**
     * Write a range of an array of bytes to the output.
     */
    protected void writeBytes(VirtualFrame frame, byte[] values, int valuesStart, int valuesLength) {
        byte[] output = getOutput(frame);
        final int outputPosition = getOutputPosition(frame);

        if (outputPosition + valuesLength > output.length) {
            // If we ran out of output byte[], deoptimize and next time we'll allocate more

            CompilerDirectives.transferToInterpreterAndInvalidate();
            output = Arrays.copyOf(output, (output.length + valuesLength) * 2);
            setOutput(frame, output);
        }

        System.arraycopy(values, valuesStart, output, outputPosition, valuesLength);
        setOutputPosition(frame, outputPosition + valuesLength);
    }

}
