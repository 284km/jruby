/*
 * Copyright (c) 2015 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.pack.parser;

public class PackTokenizer {

    private final String format;
    private final boolean extended;
    private int position;
    private Object peek;

    public PackTokenizer(String format, boolean extended) {
        this.format = format;
        this.extended = extended;
    }

    public Object peek() {
        if (peek == null) {
            peek = next();
        }

        return peek;
    }

    public Object next() {
        if (peek != null) {
            final Object token = peek;
            peek = null;
            return token;
        }

        consumeWhitespace();

        if (position == format.length()) {
            return null;
        }

        final char c = format.charAt(position);

        final String chars;

        if (extended) {
            chars = "NLXx*()";
        } else {
            chars = "NLXx*";
        }

        if (chars.indexOf(c) > -1) {
            position++;
            return c;
        }

        if (Character.isDigit(c)) {
            final int start = position;
            position++;
            while (position < format.length() && Character.isDigit(format.charAt(position))) {
                position++;
            }
            return Integer.parseInt(format.substring(start, position));
        }


        throw new UnsupportedOperationException(String.format("unexpected token %c", c));
    }

    private void consumeWhitespace() {
        while (position < format.length() && format.charAt(position) == ' ') {
            position++;
        }
    }

}
