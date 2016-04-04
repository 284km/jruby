/*
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
/*
 * Copyright (c) 2014, 2015, 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.interop;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.ArityException;
import com.oracle.truffle.api.interop.ForeignAccess;
import com.oracle.truffle.api.interop.Message;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.interop.UnsupportedTypeException;
import com.oracle.truffle.api.nodes.DirectCallNode;
import com.oracle.truffle.api.nodes.IndirectCallNode;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.source.SourceSection;
import org.jruby.truffle.RubyContext;
import org.jruby.truffle.core.CoreClass;
import org.jruby.truffle.core.CoreMethod;
import org.jruby.truffle.core.CoreMethodArrayArgumentsNode;
import org.jruby.truffle.core.Layouts;
import org.jruby.truffle.core.rope.Rope;
import org.jruby.truffle.core.string.StringCachingGuards;
import org.jruby.truffle.core.string.StringOperations;
import org.jruby.util.ByteList;
import org.jruby.util.StringSupport;

import java.io.IOException;

@CoreClass(name = "Truffle::Interop")
public abstract class TruffleInteropNodes {

    @CoreMethod(unsafeNeedsAudit = true, names = "executable?", isModuleFunction = true, needsSelf = false, required = 1)
    public abstract static class IsExecutableNode extends CoreMethodArrayArgumentsNode {

        public IsExecutableNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        @Specialization
        public boolean isExecutable(
                VirtualFrame frame,
                TruffleObject receiver,
                @Cached("createIsExecutableNode()") Node isExecutableNode) {
            return ForeignAccess.sendIsExecutable(isExecutableNode, frame, receiver);
        }

        protected Node createIsExecutableNode() {
            return Message.IS_EXECUTABLE.createNode();
        }

    }

    @CoreMethod(unsafeNeedsAudit = true, names = "execute", isModuleFunction = true, needsSelf = false, required = 1, rest = true)
    public abstract static class ExecuteNode extends CoreMethodArrayArgumentsNode {

        public ExecuteNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        @Specialization(
                guards = "args.length == cachedArgsLength",
                limit = "10"
        )
        public Object executeForeignCached(
                VirtualFrame frame,
                TruffleObject receiver,
                Object[] args,
                @Cached("args.length") int cachedArgsLength,
                @Cached("createIsExecuteNode(cachedArgsLength)") Node executeNode,
                @Cached("create()") BranchProfile exceptionProfile) {
            try {
                return ForeignAccess.sendExecute(executeNode, frame, receiver, args);
            } catch (UnsupportedTypeException | ArityException | UnsupportedMessageException e) {
                exceptionProfile.enter();
                throw new RuntimeException(e);
            }
        }

        @Specialization(contains = "executeForeignCached")
        public Object executeForeignUncached(
                VirtualFrame frame,
                TruffleObject receiver,
                Object[] args) {
            CompilerDirectives.bailout("can't compile megamorphic interop EXECUTE message sends");

            final Node executeNode = createIsExecuteNode(args.length);

            try {
                return ForeignAccess.sendExecute(executeNode, frame, receiver, args);
            } catch (UnsupportedTypeException | ArityException | UnsupportedMessageException e) {
                throw new RuntimeException(e);
            }
        }

        protected Node createIsExecuteNode(int argsLength) {
            return Message.createExecute(argsLength).createNode();
        }

    }

    @CoreMethod(unsafeNeedsAudit = true, names = {"size?", "has_size_property?"}, isModuleFunction = true, needsSelf = false, required = 1)
    public abstract static class HasSizeNode extends CoreMethodArrayArgumentsNode {

        public HasSizeNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        @Specialization
        public boolean hasSize(
                VirtualFrame frame,
                TruffleObject receiver,
                @Cached("createHasSizeNode()") Node hasSizeNode) {
            return ForeignAccess.sendHasSize(hasSizeNode, frame, receiver);
        }

        protected Node createHasSizeNode() {
            return Message.IS_EXECUTABLE.createNode();
        }

    }

    @CoreMethod(unsafeNeedsAudit = true, names = "size", isModuleFunction = true, needsSelf = false, required = 1)
    public abstract static class SizeNode extends CoreMethodArrayArgumentsNode {

        public SizeNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        @Specialization
        public Object size(String receiver) {
            return receiver.length();
        }

        @Specialization
        public Object size(
                VirtualFrame frame,
                TruffleObject receiver,
                @Cached("createGetSizeNode()") Node getSizeNode,
                @Cached("create()") BranchProfile exceptionProfile) {
            try {
                return ForeignAccess.sendGetSize(getSizeNode, frame, receiver);
            } catch (UnsupportedMessageException e) {
                exceptionProfile.enter();
                throw new RuntimeException(e);
            }
        }

        protected Node createGetSizeNode() {
            return Message.GET_SIZE.createNode();
        }

    }

    @CoreMethod(unsafeNeedsAudit = true, names = {"boxed?", "boxed_primitive?"}, isModuleFunction = true, needsSelf = false, required = 1)
    public abstract static class BoxedNode extends CoreMethodArrayArgumentsNode {

        public BoxedNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        @Specialization
        public boolean isBoxed(boolean receiver) {
            return true;
        }

        @Specialization
        public boolean isBoxed(byte receiver) {
            return true;
        }

        @Specialization
        public boolean isBoxed(short receiver) {
            return true;
        }

        @Specialization
        public boolean isBoxed(int receiver) {
            return true;
        }

        @Specialization
        public boolean isBoxed(long receiver) {
            return true;
        }

        @Specialization
        public boolean isBoxed(float receiver) {
            return true;
        }

        @Specialization
        public boolean isBoxed(double receiver) {
            return true;
        }

        @Specialization
        public boolean isBoxed(CharSequence receiver) {
            return true;
        }

        @Specialization
        public boolean isBoxed(
                VirtualFrame frame,
                TruffleObject receiver,
                @Cached("createIsBoxedNode()") Node isBoxedNode,
                @Cached("create()") BranchProfile exceptionProfile) {
            return ForeignAccess.sendIsBoxed(isBoxedNode, frame, receiver);
        }

        protected Node createIsBoxedNode() {
            return Message.IS_BOXED.createNode();
        }

        @Fallback
        public boolean isBoxed(Object receiver) {
            return false;
        }

    }

    @CoreMethod(unsafeNeedsAudit = true, names = {"unbox", "unbox_value"}, isModuleFunction = true, needsSelf = false, required = 1)
    public abstract static class UnboxNode extends CoreMethodArrayArgumentsNode {

        public UnboxNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        @Specialization
        public boolean unbox(boolean receiver) {
            return receiver;
        }

        @Specialization
        public byte unbox(byte receiver) {
            return receiver;
        }

        @Specialization
        public short unbox(short receiver) {
            return receiver;
        }

        @Specialization
        public int unbox(int receiver) {
            return receiver;
        }

        @Specialization
        public long unbox(long receiver) {
            return receiver;
        }

        @Specialization
        public float unbox(float receiver) {
            return receiver;
        }

        @Specialization
        public double unbox(double receiver) {
            return receiver;
        }

        @Specialization
        public DynamicObject unbox(CharSequence receiver) {
            // TODO CS-21-Dec-15 this shouldn't be needed - we need to convert j.l.String to Ruby's String automatically

            return Layouts.STRING.createString(coreLibrary().getStringFactory(),
                    StringOperations.ropeFromByteList(ByteList.create(receiver)));
        }

        @Specialization
        public Object unbox(
                VirtualFrame frame,
                TruffleObject receiver,
                @Cached("createUnboxNode()") Node unboxNode,
                @Cached("create()") BranchProfile exceptionProfile) {
            try {
                return ForeignAccess.sendUnbox(unboxNode, frame, receiver);
            } catch (UnsupportedMessageException e) {
                exceptionProfile.enter();
                throw new RuntimeException(e);
            }
        }

        protected Node createUnboxNode() {
            return Message.UNBOX.createNode();
        }

    }

    @CoreMethod(unsafeNeedsAudit = true, names = "null?", isModuleFunction = true, needsSelf = false, required = 1)
    public abstract static class NullNode extends CoreMethodArrayArgumentsNode {

        public NullNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        @Specialization
        public boolean isNull(
                VirtualFrame frame,
                TruffleObject receiver,
                @Cached("createIsNullNode()") Node isNullNode) {
            return ForeignAccess.sendIsNull(isNullNode, frame, receiver);
        }

        protected Node createIsNullNode() {
            return Message.IS_NULL.createNode();
        }

    }

    @CoreMethod(unsafeNeedsAudit = true, names = {"read", "read_property"}, isModuleFunction = true, needsSelf = false, required = 2)
    @ImportStatic(StringCachingGuards.class)
    public abstract static class ReadNode extends CoreMethodArrayArgumentsNode {

        public ReadNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        @Specialization(guards = {"!isRubySymbol(identifier)", "!isRubyString(identifier)"})
        public Object read(
                VirtualFrame frame,
                TruffleObject receiver,
                Object identifier,
                @Cached("createReadNode()") Node readNode,
                @Cached("create()") BranchProfile exceptionProfile) {
            try {
                return ForeignAccess.sendRead(readNode, frame, receiver, identifier);
            } catch (UnknownIdentifierException | UnsupportedMessageException e) {
                exceptionProfile.enter();
                throw new RuntimeException(e);
            }
        }

        @Specialization(guards = {"isRubySymbol(identifier)", "identifier == cachedIdentifier"})
        public Object read(
                VirtualFrame frame,
                TruffleObject receiver,
                DynamicObject identifier,
                @Cached("identifier") DynamicObject cachedIdentifier,
                @Cached("identifier.toString()") String identifierString,
                @Cached("createReadNode()") Node readNode,
                @Cached("create()") BranchProfile exceptionProfile) {
            try {
                return ForeignAccess.sendRead(readNode, frame, receiver, identifierString);
            } catch (UnknownIdentifierException | UnsupportedMessageException e) {
                exceptionProfile.enter();
                throw new RuntimeException(e);
            }
        }

        @Specialization(guards = {"isRubyString(identifier)", "ropesEqual(identifier, cachedIdentifier)"})
        public Object read(
                VirtualFrame frame,
                TruffleObject receiver,
                DynamicObject identifier,
                @Cached("privatizeRope(identifier)") Rope cachedIdentifier,
                @Cached("identifier.toString()") String identifierString,
                @Cached("createReadNode()") Node readNode,
                @Cached("create()") BranchProfile exceptionProfile) {
            try {
                return ForeignAccess.sendRead(readNode, frame, receiver, identifierString);
            } catch (UnknownIdentifierException | UnsupportedMessageException e) {
                exceptionProfile.enter();
                throw new RuntimeException(e);
            }
        }

        protected static Node createReadNode() {
            return Message.READ.createNode();
        }

    }

    @CoreMethod(unsafeNeedsAudit = true, names = {"write", "write_property"}, isModuleFunction = true, needsSelf = false, required = 3)
    @ImportStatic(StringCachingGuards.class)
    public abstract static class WriteNode extends CoreMethodArrayArgumentsNode {

        public WriteNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        @Specialization(guards = {"!isRubySymbol(identifier)", "!isRubyString(identifier)"})
        public Object writeProperty(
                VirtualFrame frame,
                TruffleObject receiver,
                Object identifier,
                Object value,
                @Cached("createWriteNode()") Node writeNode,
                @Cached("create()") BranchProfile exceptionProfile) {
            try {
                return ForeignAccess.sendWrite(writeNode, frame, receiver, identifier, value);
            } catch (UnknownIdentifierException | UnsupportedTypeException | UnsupportedMessageException e) {
                exceptionProfile.enter();
                throw new RuntimeException(e);
            }
        }

        @Specialization(guards = {"isRubySymbol(identifier)", "identifier == cachedIdentifier"})
        public Object writeProperty(
                VirtualFrame frame,
                TruffleObject receiver,
                DynamicObject identifier,
                Object value,
                @Cached("identifier") DynamicObject cachedIdentifier,
                @Cached("identifier.toString()") String identifierString,
                @Cached("createWriteNode()") Node writeNode,
                @Cached("create()") BranchProfile exceptionProfile) {
            try {
                return ForeignAccess.sendWrite(writeNode, frame, receiver, identifierString, value);
            } catch (UnknownIdentifierException | UnsupportedTypeException | UnsupportedMessageException e) {
                exceptionProfile.enter();
                throw new RuntimeException(e);
            }
        }

        @Specialization(guards = {"isRubyString(identifier)", "ropesEqual(identifier, cachedIdentifier)"})
        public Object writeProperty(
                VirtualFrame frame,
                TruffleObject receiver,
                DynamicObject identifier,
                Object value,
                @Cached("privatizeRope(identifier)") Rope cachedIdentifier,
                @Cached("identifier.toString()") String identifierString,
                @Cached("createWriteNode()") Node writeNode,
                @Cached("create()") BranchProfile exceptionProfile) {
            try {
                return ForeignAccess.sendWrite(writeNode, frame, receiver, identifierString, value);
            } catch (UnknownIdentifierException | UnsupportedTypeException | UnsupportedMessageException e) {
                exceptionProfile.enter();
                throw new RuntimeException(e);
            }
        }

        protected static Node createWriteNode() {
            return Message.WRITE.createNode();
        }

    }

    // TODO CS 21-Dec-15 this shouldn't be needed any more - we can handle byte, short, float etc natively

    @CoreMethod(unsafeNeedsAudit = true, names = "interop_to_ruby_primitive", isModuleFunction = true, needsSelf = false, required = 1)
    public abstract static class InteropToRubyNode extends CoreMethodArrayArgumentsNode {

        public InteropToRubyNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        @Specialization
        public int convert(byte value) {
            return value;
        }

        @Specialization
        public int convert(short value) {
            return value;
        }

        @Specialization
        public int convert(char value) {
            return value;
        }

        @Specialization
        public int convert(int value) {
            return value;
        }

        @Specialization
        public long convert(long value) {
            return value;
        }

        @Specialization
        public double convert(float value) {
            return value;
        }

        @Specialization
        public double convert(double value) {
            return value;
        }

        @Specialization
        public int convert(String value) {
            return (int) value.charAt(0);
        }

    }

    @CoreMethod(unsafeNeedsAudit = true, names = "export", isModuleFunction = true, needsSelf = false, required = 2)
    public abstract static class ExportNode extends CoreMethodArrayArgumentsNode {

        public ExportNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        @Specialization(guards = "isRubyString(name)")
        public Object export(VirtualFrame frame, DynamicObject name, TruffleObject object) {
            getContext().getInteropManager().exportObject(name, object);
            return object;
        }

        protected static String rubyStringToString(DynamicObject rubyString) {
            return rubyString.toString();
        }
    }

    @CoreMethod(unsafeNeedsAudit = true, names = "import", isModuleFunction = true, needsSelf = false, required = 1)
    public abstract static class ImportNode extends CoreMethodArrayArgumentsNode {

        public ImportNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        @TruffleBoundary
        @Specialization(guards = "isRubyString(name)")
        public Object importObject(DynamicObject name) {
            return getContext().getInteropManager().importObject(name);
        }

    }

    @CoreMethod(unsafeNeedsAudit = true, names = "mime_type_supported?", isModuleFunction = true, needsSelf = false, required =1)
    public abstract static class MimeTypeSupportedNode extends CoreMethodArrayArgumentsNode {

        public MimeTypeSupportedNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        @TruffleBoundary
        @Specialization(guards = "isRubyString(mimeType)")
        public boolean isMimeTypeSupported(DynamicObject mimeType) {
            return getContext().getEnv().isMimeTypeSupported(mimeType.toString());
        }

    }

    @CoreMethod(unsafeNeedsAudit = true, names = "eval", isModuleFunction = true, needsSelf = false, required = 2)
    @ImportStatic(StringCachingGuards.class)
    public abstract static class EvalNode extends CoreMethodArrayArgumentsNode {

        public EvalNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        @Specialization(guards = {
                "isRubyString(mimeType)",
                "isRubyString(source)",
                "ropesEqual(mimeType, cachedMimeType)",
                "ropesEqual(source, cachedSource)"
        }, limit = "getCacheLimit()")
        public Object evalCached(
                VirtualFrame frame,
                DynamicObject mimeType,
                DynamicObject source,
                @Cached("privatizeRope(mimeType)") Rope cachedMimeType,
                @Cached("privatizeRope(source)") Rope cachedSource,
                @Cached("create(parse(mimeType, source))") DirectCallNode callNode
        ) {
            return callNode.call(frame, new Object[]{});
        }

        @Specialization(guards = {"isRubyString(mimeType)", "isRubyString(source)"}, contains = "evalCached")
        public Object evalUncached(VirtualFrame frame, DynamicObject mimeType, DynamicObject source, @Cached("create()")IndirectCallNode callNode) {
            return callNode.call(frame, parse(mimeType, source), new Object[]{});
        }

        @TruffleBoundary
        protected CallTarget parse(DynamicObject mimeType, DynamicObject source) {
            final String mimeTypeString = mimeType.toString();
            final Source sourceObject = Source.fromText(source.toString(), "(eval)").withMimeType(mimeTypeString);

            try {
                return getContext().getEnv().parse(sourceObject);
            } catch (IOException e) {
                CompilerDirectives.transferToInterpreter();
                throw new RuntimeException(e);
            }
        }

        protected int getCacheLimit() {
            return getContext().getOptions().EVAL_CACHE;
        }

    }

    // TODO CS-21-Dec-15 this shouldn't be needed - we need to convert j.l.String to Ruby's String automatically

    @CoreMethod(unsafeNeedsAudit = true, names = "java_string_to_ruby", isModuleFunction = true, needsSelf = false, required = 1)
    public abstract static class JavaStringToRubyNode extends CoreMethodArrayArgumentsNode {

        public JavaStringToRubyNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        @Specialization
        @TruffleBoundary
        public DynamicObject javaStringToRuby(String string) {
            return Layouts.STRING.createString(coreLibrary().getStringFactory(), StringOperations.ropeFromByteList(ByteList.create(string), StringSupport.CR_UNKNOWN));
        }

    }

}
