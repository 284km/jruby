/*
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.parser;

import org.jcodings.Encoding;
import org.jcodings.specific.ASCIIEncoding;
import org.jruby.truffle.core.string.ByteList;

import java.util.Arrays;

public class ParserByteListBuilder {

    private byte[] bytes;
    private int length;
    private Encoding encoding;

    public ParserByteListBuilder() {
        bytes = new byte[16];
        length = 0;
        encoding = ASCIIEncoding.INSTANCE;
    }

    public ParserByteListBuilder(byte[] bytes, Encoding encoding) {
        this.bytes = bytes;
        length = bytes.length;
        this.encoding = encoding;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public Encoding getEncoding() {
        return encoding;
    }

    public void setEncoding(Encoding encoding) {
        this.encoding = encoding;
    }

    public void append(int b) {
        append((byte) b);
    }

    public void append(byte b) {
        grow(1);
        bytes[length] = b;
        length++;
    }

    public void append(byte[] bytes) {
        append(bytes, 0, bytes.length);
    }

    public void append(ParserByteList other) {
        append(other, 0, other.getLength());
    }

    public void append(ParserByteList other, int start, int length) {
        append(other.getBytes(), other.getStart() + start, length);
    }

    public void append(byte[] appendBytes, int appendStart, int appendLength) {
        grow(appendLength);
        System.arraycopy(appendBytes, appendStart, bytes, length, appendLength);
        length += appendLength;
    }

    public void grow(int extra) {
        if (length + extra > bytes.length) {
            bytes = Arrays.copyOf(bytes, (length + extra) * 2);
        }
    }

    public byte[] getUnsafeBytes() {
        return bytes;
    }

    public String toString() {
        return toByteList().toString();
    }

    public ParserByteList toParserByteList() {
        return new ParserByteList(Arrays.copyOfRange(bytes, 0, length), 0, length, encoding);
    }

    private ByteList toByteList() {
        return new ByteList(bytes, 0, length, encoding, true);
    }

}
