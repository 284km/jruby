/*
 * Copyright (c) 2015 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.format.parser;

/*
 * A single format directive from a printf-style format string.
 *
 * %[space padding][zero padding][.precision]type
 */
public class FormatDirective {

    public static final int PADDING_FROM_ARGUMENT = -2;
    public static final int DEFAULT = -1;

    private final int spacePadding;
    private final int zeroPadding;
    private final boolean leftJustified;
    private final int precision;
    private final char type;
    private final Object key;

    public FormatDirective(int spacePadding, int zeroPadding, boolean leftJustified, int precision, char type, Object key) {
        this.spacePadding = spacePadding;
        this.zeroPadding = zeroPadding;
        this.leftJustified = leftJustified;
        this.precision = precision;
        this.type = type;
        this.key = key;
    }

    public int getSpacePadding() {
        return spacePadding;
    }

    public int getZeroPadding() {
        return zeroPadding;
    }

    public boolean getLeftJustified() {
        return leftJustified;
    }

    public int getPrecision() {
        return precision;
    }

    public char getType() {
        return type;
    }

    public Object getKey() {
        return key;
    }
}
