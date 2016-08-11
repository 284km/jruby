/*
 * Copyright (c) 2015, 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.core.format.format;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import org.jruby.truffle.RubyContext;
import org.jruby.truffle.core.format.FormatNode;
import org.jruby.truffle.core.format.printf.PrintfSimpleTreeBuilder;
import org.jruby.truffle.util.StringUtils;

import java.nio.charset.StandardCharsets;

@NodeChildren({
    @NodeChild(value = "width", type = FormatNode.class),
    @NodeChild(value = "precision", type = FormatNode.class),
    @NodeChild(value = "value", type = FormatNode.class),
})
public abstract class FormatFloatNode extends FormatNode {

    private final char format;
    private final boolean hasSpaceFlag;
    private final boolean hasZeroFlag;
    private final boolean hasPlusFlag;
    private final boolean hasMinusFlag;

    public FormatFloatNode(RubyContext context, char format, boolean hasSpaceFlag, boolean hasZeroFlag, boolean hasPlusFlag, boolean hasMinusFlag) {
        super(context);
        this.format = format;
        this.hasSpaceFlag = hasSpaceFlag;
        this.hasZeroFlag = hasZeroFlag;
        this.hasPlusFlag = hasPlusFlag;
        this.hasMinusFlag = hasMinusFlag;
    }


    @TruffleBoundary
    @Specialization(guards = "isInfinite(value)")
    public byte[] formatInfinite(int width, int precision, double value) {
        final String infinityString = StringUtils.format(getInfiniteFormatString(width), value);
        return mapInfiniteResult(infinityString).getBytes(StandardCharsets.US_ASCII);
    }

    @TruffleBoundary
    @Specialization(guards = "!isInfinite(value)")
    public byte[] formatFinite(int width, int precision,double value) {
        return mapFiniteResult(StringUtils.format(getFiniteFormatString(width, precision), value)).getBytes(StandardCharsets.US_ASCII);
    }

    protected boolean isInfinite(double value) {
        return Double.isInfinite(value);
    }

    private static String mapFiniteResult(String input){
        // Map java finite strings to the Ruby style NaN
        if(input.contains("NAN")){
            return input.replaceFirst("NAN", "NaN");
        }
        return input;
    }

    private static String mapInfiniteResult(String input){
        // Map java infinite strings to the Ruby style Inf
        if(input.contains("-Infinity")) {
            return input.replaceFirst("-Infinity", "-Inf");
        } else if(input.contains("-INFINITY")){
            return input.replaceFirst("-INFINITY", "-Inf");
        }
        else if(input.contains("INFINITY")){
            return input.replaceFirst("INFINITY", "Inf");
        }  else if(input.contains("Infinity")) {
            return input.replaceFirst("Infinity", "Inf");
        }
       return input;
    }

    private String getFiniteFormatString(final int width, final int precision){
        final StringBuilder finiteFormatBuilder = new StringBuilder();
        finiteFormatBuilder.append("%");
        if(hasMinusFlag){
            finiteFormatBuilder.append("-");
        }
        if(hasPlusFlag){
            finiteFormatBuilder.append("+");
        }

        if (hasSpaceFlag) {
            finiteFormatBuilder.append(" ");
            finiteFormatBuilder.append(width);

            if (hasZeroFlag) {
                finiteFormatBuilder.append(".");
                finiteFormatBuilder.append(width);
            }
        } else if (hasZeroFlag) {
            finiteFormatBuilder.append("0");
            if(width > 0){
                finiteFormatBuilder.append(width);
            } else {
                finiteFormatBuilder.append(1);
            }
        } else if (!hasSpaceFlag && !hasSpaceFlag && width > 0) {
            finiteFormatBuilder.append(width);
        }

        if (precision != PrintfSimpleTreeBuilder.DEFAULT) {
            finiteFormatBuilder.append(".");
            finiteFormatBuilder.append(precision);
        }

        finiteFormatBuilder.append(format);

        return finiteFormatBuilder.toString();
    }

    private String getInfiniteFormatString(final int width){
        final StringBuilder infiniteFormatBuilder = new StringBuilder();
        infiniteFormatBuilder.append("%");
        if(hasMinusFlag){
            infiniteFormatBuilder.append("-");
        }
        if(hasPlusFlag){
            infiniteFormatBuilder.append("+");
        }

        if (hasSpaceFlag) {
            infiniteFormatBuilder.append(" ");
            infiniteFormatBuilder.append(width + 5);
        }
        if (hasZeroFlag && width != 0) {
            infiniteFormatBuilder.append("0");
            infiniteFormatBuilder.append(width + 5);
        }
        if(!hasSpaceFlag && !hasZeroFlag){
            infiniteFormatBuilder.append(width + 5);
        }

        infiniteFormatBuilder.append(format);

        return infiniteFormatBuilder.toString();
    }

}
