/*
 * Copyright (c) 2015, 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.core.format.parser;

import com.oracle.truffle.api.nodes.Node;
import org.jruby.truffle.RubyContext;
import org.jruby.truffle.core.format.nodes.PackNode;
import org.jruby.truffle.core.format.nodes.SourceNode;
import org.jruby.truffle.core.format.nodes.control.AtUnpackNode;
import org.jruby.truffle.core.format.nodes.control.BackUnpackNode;
import org.jruby.truffle.core.format.nodes.control.ForwardUnpackNode;
import org.jruby.truffle.core.format.nodes.control.NNode;
import org.jruby.truffle.core.format.nodes.control.SequenceNode;
import org.jruby.truffle.core.format.nodes.control.StarNode;
import org.jruby.truffle.core.format.nodes.decode.DecodeByteNodeGen;
import org.jruby.truffle.core.format.nodes.decode.DecodeFloat32NodeGen;
import org.jruby.truffle.core.format.nodes.decode.DecodeFloat64NodeGen;
import org.jruby.truffle.core.format.nodes.decode.DecodeInteger16BigNodeGen;
import org.jruby.truffle.core.format.nodes.decode.DecodeInteger16LittleNodeGen;
import org.jruby.truffle.core.format.nodes.decode.DecodeInteger32BigNodeGen;
import org.jruby.truffle.core.format.nodes.decode.DecodeInteger32LittleNodeGen;
import org.jruby.truffle.core.format.nodes.decode.DecodeInteger64BigNodeGen;
import org.jruby.truffle.core.format.nodes.decode.DecodeInteger64LittleNodeGen;
import org.jruby.truffle.core.format.nodes.read.ReadBERNodeGen;
import org.jruby.truffle.core.format.nodes.read.ReadBase64StringNodeGen;
import org.jruby.truffle.core.format.nodes.read.ReadBinaryStringNodeGen;
import org.jruby.truffle.core.format.nodes.read.ReadBitStringNodeGen;
import org.jruby.truffle.core.format.nodes.read.ReadByteNodeGen;
import org.jruby.truffle.core.format.nodes.read.ReadBytesNodeGen;
import org.jruby.truffle.core.format.nodes.read.ReadHexStringNodeGen;
import org.jruby.truffle.core.format.nodes.read.ReadMIMEStringNodeGen;
import org.jruby.truffle.core.format.nodes.read.ReadUTF8CharacterNodeGen;
import org.jruby.truffle.core.format.nodes.read.ReadUUStringNodeGen;
import org.jruby.truffle.core.format.nodes.type.AsUnsignedNodeGen;
import org.jruby.truffle.core.format.nodes.write.WriteValueNodeGen;
import org.jruby.truffle.core.format.runtime.PackEncoding;
import org.jruby.truffle.language.control.RaiseException;

import java.nio.ByteOrder;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class UnpackTreeBuilder extends PackBaseListener {

    private final RubyContext context;
    private final Node currentNode;

    private PackEncoding encoding = PackEncoding.DEFAULT;
    private final Deque<List<PackNode>> sequenceStack = new ArrayDeque<>();

    public UnpackTreeBuilder(RubyContext context, Node currentNode) {
        this.context = context;
        this.currentNode = currentNode;
        pushSequence();
    }

    @Override
    public void enterSequence(PackParser.SequenceContext ctx) {
        pushSequence();
    }

    @Override
    public void exitSequence(PackParser.SequenceContext ctx) {
        final List<PackNode> sequence = sequenceStack.pop();
        appendNode(new SequenceNode(context, sequence.toArray(new PackNode[sequence.size()])));
    }

    @Override
    public void exitInt8(PackParser.Int8Context ctx) {
        appendNode(applyCount(ctx.count(),
                WriteValueNodeGen.create(context,
                        DecodeByteNodeGen.create(context, true,
                                ReadByteNodeGen.create(context,
                                        new SourceNode())))));
    }

    @Override
    public void exitUint8(PackParser.Uint8Context ctx) {
        appendNode(applyCount(ctx.count(),
                WriteValueNodeGen.create(context,
                        DecodeByteNodeGen.create(context, false,
                                ReadByteNodeGen.create(context,
                                        new SourceNode())))));
    }

    @Override
    public void exitInt16Little(PackParser.Int16LittleContext ctx) {
        integer(16, ByteOrder.LITTLE_ENDIAN, ctx.count(), true);
    }

    @Override
    public void exitInt16Big(PackParser.Int16BigContext ctx) {
        integer(16, ByteOrder.BIG_ENDIAN, ctx.count(), true);
    }

    @Override
    public void exitInt16Native(PackParser.Int16NativeContext ctx) {
        integer(16, ByteOrder.nativeOrder(), ctx.count(), true);
    }

    @Override
    public void exitUint16Little(PackParser.Uint16LittleContext ctx) {
        integer(16, ByteOrder.LITTLE_ENDIAN, ctx.count(), false);
    }

    @Override
    public void exitUint16Big(PackParser.Uint16BigContext ctx) {
        integer(16, ByteOrder.BIG_ENDIAN, ctx.count(), false);
    }

    @Override
    public void exitUint16Native(PackParser.Uint16NativeContext ctx) {
        integer(16, ByteOrder.nativeOrder(), ctx.count(), false);
    }

    @Override
    public void exitInt32Little(PackParser.Int32LittleContext ctx) {
        integer(32, ByteOrder.LITTLE_ENDIAN, ctx.count(), true);
    }

    @Override
    public void exitInt32Big(PackParser.Int32BigContext ctx) {
        integer(32, ByteOrder.BIG_ENDIAN, ctx.count(), true);
    }

    @Override
    public void exitInt32Native(PackParser.Int32NativeContext ctx) {
        integer(32, ByteOrder.nativeOrder(), ctx.count(), true);
    }

    @Override
    public void exitUint32Little(PackParser.Uint32LittleContext ctx) {
        integer(32, ByteOrder.LITTLE_ENDIAN, ctx.count(), false);
    }

    @Override
    public void exitUint32Big(PackParser.Uint32BigContext ctx) {
        integer(32, ByteOrder.BIG_ENDIAN, ctx.count(), false);
    }

    @Override
    public void exitUint32Native(PackParser.Uint32NativeContext ctx) {
        integer(32, ByteOrder.nativeOrder(), ctx.count(), false);
    }

    @Override
    public void exitInt64Little(PackParser.Int64LittleContext ctx) {
        integer(64, ByteOrder.LITTLE_ENDIAN, ctx.count(), true);
    }

    @Override
    public void exitInt64Big(PackParser.Int64BigContext ctx) {
        integer(64, ByteOrder.BIG_ENDIAN, ctx.count(), true);
    }

    @Override
    public void exitInt64Native(PackParser.Int64NativeContext ctx) {
        integer(64, ByteOrder.nativeOrder(), ctx.count(), true);
    }

    @Override
    public void exitUint64Little(PackParser.Uint64LittleContext ctx) {
        integer(64, ByteOrder.LITTLE_ENDIAN, ctx.count(), false);
    }

    @Override
    public void exitUint64Big(PackParser.Uint64BigContext ctx) {
        integer(64, ByteOrder.BIG_ENDIAN, ctx.count(), false);
    }

    @Override
    public void exitUint64Native(PackParser.Uint64NativeContext ctx) {
        integer(64, ByteOrder.nativeOrder(), ctx.count(), false);
    }

    public void integer(int size, ByteOrder byteOrder, PackParser.CountContext count, boolean signed) {
        appendNode(applyCount(count, readInteger(size, byteOrder, consumePartial(count), signed)));
    }

    @Override
    public void exitUtf8Character(PackParser.Utf8CharacterContext ctx) {
        //unify(PackEncoding.UTF_8);

        appendNode(applyCount(ctx.count(),
                WriteValueNodeGen.create(context,
                        ReadUTF8CharacterNodeGen.create(context,
                                new SourceNode()))));
    }

    @Override
    public void exitBerInteger(PackParser.BerIntegerContext ctx) {
        appendNode(applyCount(ctx.count(),
                WriteValueNodeGen.create(context,
                    ReadBERNodeGen.create(context, new SourceNode()))));
    }

    @Override
    public void exitF64Native(PackParser.F64NativeContext ctx) {
        appendNode(applyCount(ctx.count(), WriteValueNodeGen.create(context, DecodeFloat64NodeGen.create(context, readIntegerX(64, ByteOrder.nativeOrder(), consumePartial(ctx.count()), true)))));
    }

    @Override
    public void exitF32Native(PackParser.F32NativeContext ctx) {
        appendNode(applyCount(ctx.count(), WriteValueNodeGen.create(context, DecodeFloat32NodeGen.create(context, readIntegerX(32, ByteOrder.nativeOrder(), consumePartial(ctx.count()), true)))));
    }

    @Override
    public void exitF64Little(PackParser.F64LittleContext ctx) {
        appendNode(applyCount(ctx.count(), WriteValueNodeGen.create(context, DecodeFloat64NodeGen.create(context, readIntegerX(64, ByteOrder.LITTLE_ENDIAN, consumePartial(ctx.count()), true)))));
    }

    @Override
    public void exitF32Little(PackParser.F32LittleContext ctx) {
        appendNode(applyCount(ctx.count(), WriteValueNodeGen.create(context, DecodeFloat32NodeGen.create(context, readIntegerX(32, ByteOrder.LITTLE_ENDIAN, consumePartial(ctx.count()), true)))));
    }

    @Override
    public void exitF64Big(PackParser.F64BigContext ctx) {
        appendNode(applyCount(ctx.count(), WriteValueNodeGen.create(context, DecodeFloat64NodeGen.create(context, readIntegerX(64, ByteOrder.BIG_ENDIAN, consumePartial(ctx.count()), true)))));
    }

    @Override
    public void exitF32Big(PackParser.F32BigContext ctx) {
        appendNode(applyCount(ctx.count(), WriteValueNodeGen.create(context, DecodeFloat32NodeGen.create(context, readIntegerX(32, ByteOrder.BIG_ENDIAN, consumePartial(ctx.count()), true)))));
    }

    @Override
    public void exitBinaryStringSpacePadded(PackParser.BinaryStringSpacePaddedContext ctx) {
        if (ctx.count() == null) {
            appendNode(WriteValueNodeGen.create(context, ReadBinaryStringNodeGen.create(context, false, false, 1, true, true, false, new SourceNode())));
        } else if (ctx.count().INT() == null) {
            appendNode(WriteValueNodeGen.create(context, ReadBinaryStringNodeGen.create(context, true, false, -1, true, true, false, new SourceNode())));
        } else {
            final int count = Integer.parseInt(ctx.count().INT().getSymbol().getText());
            appendNode(WriteValueNodeGen.create(context, ReadBinaryStringNodeGen.create(context, false, false, count, true, true, false, new SourceNode())));
        }
    }

    @Override
    public void exitBinaryStringNullPadded(PackParser.BinaryStringNullPaddedContext ctx) {
        if (ctx.count() == null) {
            appendNode(WriteValueNodeGen.create(context, ReadBinaryStringNodeGen.create(context, false, false, 1, false, false, false, new SourceNode())));
        } else if (ctx.count().INT() == null) {
            appendNode(WriteValueNodeGen.create(context, ReadBinaryStringNodeGen.create(context, true, false, -1, false, false, false, new SourceNode())));
        } else {
            final int count = Integer.parseInt(ctx.count().INT().getSymbol().getText());
            appendNode(WriteValueNodeGen.create(context, ReadBinaryStringNodeGen.create(context, false, false, count, false, false, false, new SourceNode())));
        }
    }

    @Override
    public void exitBinaryStringNullStar(PackParser.BinaryStringNullStarContext ctx) {
        if (ctx.count() == null) {
            appendNode(WriteValueNodeGen.create(context, ReadBinaryStringNodeGen.create(context, false, true, 1, false, true, true, new SourceNode())));
        } else if (ctx.count().INT() == null) {
            appendNode(WriteValueNodeGen.create(context, ReadBinaryStringNodeGen.create(context, true, true, -1, false, true, true, new SourceNode())));
        } else {
            final int count = Integer.parseInt(ctx.count().INT().getSymbol().getText());
            appendNode(WriteValueNodeGen.create(context, ReadBinaryStringNodeGen.create(context, false, false, count, false, true, true, new SourceNode())));
        }
    }

    @Override
    public void exitBitStringMSBFirst(PackParser.BitStringMSBFirstContext ctx) {
        bitString(ByteOrder.BIG_ENDIAN, ctx.count());
    }

    @Override
    public void exitBitStringMSBLast(PackParser.BitStringMSBLastContext ctx) {
        bitString(ByteOrder.LITTLE_ENDIAN, ctx.count());
    }

    @Override
    public void exitHexStringHighFirst(PackParser.HexStringHighFirstContext ctx) {
        hexString(ByteOrder.BIG_ENDIAN, ctx.count());
    }

    @Override
    public void exitHexStringLowFirst(PackParser.HexStringLowFirstContext ctx) {
        hexString(ByteOrder.LITTLE_ENDIAN, ctx.count());
    }

    @Override
    public void exitUuString(PackParser.UuStringContext ctx) {
        //unify(PackEncoding.US_ASCII);

        appendNode(
                WriteValueNodeGen.create(context,
                        ReadUUStringNodeGen.create(context,
                                new SourceNode())));
    }

    @Override
    public void exitMimeString(PackParser.MimeStringContext ctx) {
        //unify(PackEncoding.US_ASCII);

        appendNode(WriteValueNodeGen.create(context,
                ReadMIMEStringNodeGen.create(context, new SourceNode())));
    }

    @Override
    public void exitBase64String(PackParser.Base64StringContext ctx) {
        //unify(PackEncoding.US_ASCII);

        appendNode(WriteValueNodeGen.create(context,
                ReadBase64StringNodeGen.create(context, new SourceNode())));
    }

    @Override
    public void exitPointer(PackParser.PointerContext ctx) {
        // Just do nothing
    }

    @Override
    public void exitAt(PackParser.AtContext ctx) {
        final int position;

        if (ctx.count() == null) {
            position = 0;
        } else if (ctx.count() != null && ctx.count().INT() == null) {
            return;
        } else {
            position = Integer.parseInt(ctx.count().INT().getText());
        }

        appendNode(new AtUnpackNode(context, position));
    }

    @Override
    public void exitBack(PackParser.BackContext ctx) {
        if (ctx.count() != null && ctx.count().INT() == null) {
            appendNode(new BackUnpackNode(context, true));
        } else if (ctx.count() == null || ctx.count().INT() != null) {
            appendNode(applyCount(ctx.count(), new BackUnpackNode(context, false)));
        }
    }

    @Override
    public void exitNullByte(PackParser.NullByteContext ctx) {
        if (ctx.count() != null && ctx.count().INT() == null) {
            appendNode(new ForwardUnpackNode(context, true));
        } else if (ctx.count() == null || ctx.count().INT() != null) {
            appendNode(applyCount(ctx.count(), new ForwardUnpackNode(context, false)));
        }
    }

    @Override
    public void enterSubSequence(PackParser.SubSequenceContext ctx) {
        pushSequence();
    }

    @Override
    public void exitSubSequence(PackParser.SubSequenceContext ctx) {
        final List<PackNode> sequence = sequenceStack.pop();
        final SequenceNode sequenceNode = new SequenceNode(context, sequence.toArray(new PackNode[sequence.size()]));

        final PackNode resultingNode;

        if (ctx.INT() == null) {
            resultingNode = sequenceNode;
        } else {
            resultingNode = new NNode(context, Integer.parseInt(ctx.INT().getText()), sequenceNode);
        }

        appendNode(resultingNode);
    }

    @Override
    public void exitErrorDisallowedNative(PackParser.ErrorDisallowedNativeContext ctx) {
        throw new RaiseException(context.getCoreLibrary().argumentError("'" + ctx.NATIVE().getText() + "' allowed only after types sSiIlLqQ", currentNode));
    }

    public PackNode getNode() {
        return sequenceStack.peek().get(0);
    }

    public PackEncoding getEncoding() {
        return encoding;
    }

    private void pushSequence() {
        sequenceStack.push(new ArrayList<PackNode>());
    }

    private void appendNode(PackNode node) {
        sequenceStack.peek().add(node);
    }

    private boolean consumePartial(PackParser.CountContext ctx) {
        return ctx != null && ctx.INT() == null;
    }

    private PackNode readInteger(int size, ByteOrder byteOrder, boolean consumePartial, boolean signed) {
        final PackNode readNode = ReadBytesNodeGen.create(context, size / 8, consumePartial, new SourceNode());
        return readInteger(size, byteOrder, readNode, signed);
    }

    private PackNode readIntegerX(int size, ByteOrder byteOrder, boolean consumePartial, boolean signed) {
        final PackNode readNode = ReadBytesNodeGen.create(context, size / 8, consumePartial, new SourceNode());
        return readIntegerX(size, byteOrder, readNode, signed);
    }

    private PackNode readInteger(int size, ByteOrder byteOrder, PackNode readNode, boolean signed) {
        PackNode convert;

        switch (size) {
            case 16:
                if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
                    convert = DecodeInteger16LittleNodeGen.create(context, readNode);
                } else {
                    convert = DecodeInteger16BigNodeGen.create(context, readNode);
                }
                break;
            case 32:
                if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
                    convert = DecodeInteger32LittleNodeGen.create(context, readNode);
                } else {
                    convert = DecodeInteger32BigNodeGen.create(context, readNode);
                }
                break;
            case 64:
                if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
                    convert = DecodeInteger64LittleNodeGen.create(context, readNode);
                } else {
                    convert = DecodeInteger64BigNodeGen.create(context, readNode);
                }
                break;
            default:
                throw new UnsupportedOperationException();
        }

        if (!signed) {
            convert = AsUnsignedNodeGen.create(context, convert);
        }

        return WriteValueNodeGen.create(context, convert);
    }

    private PackNode readIntegerX(int size, ByteOrder byteOrder, PackNode readNode, boolean signed) {
        PackNode convert;

        switch (size) {
            case 16:
                if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
                    convert = DecodeInteger16LittleNodeGen.create(context, readNode);
                } else {
                    convert = DecodeInteger16BigNodeGen.create(context, readNode);
                }
                break;
            case 32:
                if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
                    convert = DecodeInteger32LittleNodeGen.create(context, readNode);
                } else {
                    convert = DecodeInteger32BigNodeGen.create(context, readNode);
                }
                break;
            case 64:
                if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
                    convert = DecodeInteger64LittleNodeGen.create(context, readNode);
                } else {
                    convert = DecodeInteger64BigNodeGen.create(context, readNode);
                }
                break;
            default:
                throw new UnsupportedOperationException();
        }

        if (!signed) {
            convert = AsUnsignedNodeGen.create(context, convert);
        }

        return convert;
    }

    private void bitString(ByteOrder byteOrder, PackParser.CountContext ctx) {
        final boolean star;
        final int length;

        if (ctx == null) {
            star = false;
            length = 1;
        } else if (ctx.INT() == null) {
            star = true;
            length = 0;
        } else {
            star = false;
            length = Integer.parseInt(ctx.INT().getText());
        }

        appendNode(WriteValueNodeGen.create(context,
                ReadBitStringNodeGen.create(context, byteOrder, star, length, new SourceNode())));
    }

    private void hexString(ByteOrder byteOrder, PackParser.CountContext ctx) {
        final boolean star;
        final int length;

        if (ctx == null) {
            star = false;
            length = 1;
        } else if (ctx.INT() == null) {
            star = true;
            length = 0;
        } else {
            star = false;
            length = Integer.parseInt(ctx.INT().getText());
        }

        appendNode(WriteValueNodeGen.create(context,
                ReadHexStringNodeGen.create(context, byteOrder, star, length, new SourceNode())));

    }

    private PackNode applyCount(PackParser.CountContext count, PackNode node) {
        if (count == null) {
            return node;
        } else if (count.INT() != null) {
            return new NNode(context, Integer.parseInt(count.INT().getText()), node);
        } else {
            return new StarNode(context, node);
        }
    }

}
