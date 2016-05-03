/*
 * Copyright (c) 2015, 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 *
 * This code is modified from the Psych JRuby extension module
 * implementation with the following header:
 *
 * Version: EPL 1.0/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Eclipse Public
 * License Version 1.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.eclipse.org/legal/epl-v10.html
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * Copyright (C) 2010 Charles O Nutter <headius@headius.com>
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either of the GNU General Public License Version 2 or later (the "GPL"),
 * or the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the EPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the EPL, the GPL or the LGPL.
 */
package org.jruby.truffle.stdlib.psych;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.source.SourceSection;
import org.jcodings.Encoding;
import org.jcodings.specific.UTF8Encoding;
import org.jcodings.unicode.UnicodeEncoding;
import org.jruby.RubyEncoding;
import org.jruby.runtime.Helpers;
import org.jruby.truffle.RubyContext;
import org.jruby.truffle.core.CoreClass;
import org.jruby.truffle.core.CoreMethod;
import org.jruby.truffle.core.CoreMethodArrayArgumentsNode;
import org.jruby.truffle.core.Layouts;
import org.jruby.truffle.core.adapaters.InputStreamAdapter;
import org.jruby.truffle.core.cast.ToStrNode;
import org.jruby.truffle.core.cast.ToStrNodeGen;
import org.jruby.truffle.core.string.StringOperations;
import org.jruby.truffle.debug.DebugHelpers;
import org.jruby.truffle.language.NotProvided;
import org.jruby.truffle.language.RubyGuards;
import org.jruby.truffle.language.SnippetNode;
import org.jruby.truffle.language.dispatch.CallDispatchHeadNode;
import org.jruby.truffle.language.dispatch.DoesRespondDispatchHeadNode;
import org.jruby.truffle.language.objects.AllocateObjectNode;
import org.jruby.truffle.language.objects.AllocateObjectNodeGen;
import org.jruby.truffle.language.objects.ReadObjectFieldNode;
import org.jruby.truffle.language.objects.ReadObjectFieldNodeGen;
import org.jruby.util.ByteList;
import org.jruby.util.io.EncodingUtils;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.error.Mark;
import org.yaml.snakeyaml.events.AliasEvent;
import org.yaml.snakeyaml.events.DocumentEndEvent;
import org.yaml.snakeyaml.events.DocumentStartEvent;
import org.yaml.snakeyaml.events.Event;
import org.yaml.snakeyaml.events.MappingStartEvent;
import org.yaml.snakeyaml.events.ScalarEvent;
import org.yaml.snakeyaml.events.SequenceStartEvent;
import org.yaml.snakeyaml.parser.Parser;
import org.yaml.snakeyaml.parser.ParserException;
import org.yaml.snakeyaml.parser.ParserImpl;
import org.yaml.snakeyaml.reader.ReaderException;
import org.yaml.snakeyaml.reader.StreamReader;
import org.yaml.snakeyaml.scanner.ScannerException;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@CoreClass(name = "Psych::Parser")
public abstract class PsychParserNodes {

    @CoreMethod(names = "allocate", constructor = true)
    public abstract static class AllocateNode extends CoreMethodArrayArgumentsNode {

        @Child private AllocateObjectNode allocateNode;

        public AllocateNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
            allocateNode = AllocateObjectNodeGen.create(context, sourceSection, null, null);
        }

        @Specialization
        public DynamicObject allocate(DynamicObject rubyClass) {
            return allocateNode.allocate(rubyClass, null, null);
        }

    }

    @CoreMethod(names = "parse", required = 1, optional = 1)
    public abstract static class ParseNode extends CoreMethodArrayArgumentsNode {

        @Node.Child private ToStrNode toStrNode;

        public ParseNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
            toStrNode = ToStrNodeGen.create(getContext(), getSourceSection(), null);
        }

        public abstract Object executeParse(VirtualFrame frame, DynamicObject parserObject, DynamicObject yaml, Object path);

        @Specialization
        public Object parse(VirtualFrame frame, DynamicObject parserObject, DynamicObject yaml, NotProvided path) {
            return executeParse(frame, parserObject, yaml, nil());
        }

        @Specialization
        public Object parse(
                VirtualFrame frame,
                DynamicObject parserObject,
                DynamicObject yaml,
                DynamicObject path,
                @Cached("new()") SnippetNode taintedNode,
                @Cached("create()") DoesRespondDispatchHeadNode respondToPathNode,
                @Cached("createMethodCall()") CallDispatchHeadNode callPathNode,
                @Cached("createReadHandlerNode()") ReadObjectFieldNode readHandlerNode,
                @Cached("createMethodCall()") CallDispatchHeadNode callStartStreamNode,
                @Cached("createMethodCall()") CallDispatchHeadNode callStartDocumentNode,
                @Cached("createMethodCall()") CallDispatchHeadNode callEndDocumentNode,
                @Cached("createMethodCall()") CallDispatchHeadNode callAliasNode,
                @Cached("createMethodCall()") CallDispatchHeadNode callScalarNode,
                @Cached("createMethodCall()") CallDispatchHeadNode callStartSequenceNode,
                @Cached("createMethodCall()") CallDispatchHeadNode callEndSequenceNode,
                @Cached("createMethodCall()") CallDispatchHeadNode callStartMappingNode,
                @Cached("createMethodCall()") CallDispatchHeadNode callEndMappingNode,
                @Cached("createMethodCall()") CallDispatchHeadNode callEndStreamNode,
                @Cached("new()") SnippetNode raiseSyntaxErrorSnippetNode) {
            CompilerDirectives.bailout("Psych parsing cannot be compiled");

            final boolean tainted = (boolean) taintedNode.execute(frame, "yaml.tainted? || yaml.is_a?(IO)", "yaml", yaml);

            final StreamReader reader;

            // fall back on IOInputStream, using default charset
            if (!RubyGuards.isRubyString(yaml) && (boolean) DebugHelpers.eval(getContext(), "yaml.respond_to? :read", "yaml", yaml)) {
                //final boolean isIO = (boolean) ruby("yaml.is_a? IO", "yaml", yaml);
                //Encoding enc = isIO
                //        ? UTF8Encoding.INSTANCE // ((RubyIO)yaml).getReadEncoding()
                //        : UTF8Encoding.INSTANCE;
                final Encoding enc = UTF8Encoding.INSTANCE;
                Charset charset = enc.getCharset();
                reader = new StreamReader(new InputStreamReader(new InputStreamAdapter(getContext(), yaml), charset));
            } else {
                ByteList byteList = StringOperations.getByteListReadOnly(toStrNode.coerceObject(frame, yaml));
                Encoding enc = byteList.getEncoding();

                // if not unicode, transcode to UTF8
                if (!(enc instanceof UnicodeEncoding)) {
                    byteList = EncodingUtils.strConvEnc(getContext().getJRubyRuntime().getCurrentContext(), byteList, enc, UTF8Encoding.INSTANCE);
                    enc = UTF8Encoding.INSTANCE;
                }

                ByteArrayInputStream bais = new ByteArrayInputStream(byteList.getUnsafeBytes(), byteList.getBegin(), byteList.getRealSize());

                Charset charset = enc.getCharset();

                assert charset != null : "charset for encoding " + enc + " should not be null";

                InputStreamReader isr = new InputStreamReader(bais, charset);

                reader = new StreamReader(isr);
            }

            final Parser parser = new ParserImpl(reader);

            try {
                if (isNil(path) && respondToPathNode.doesRespondTo(frame, "path", yaml)) {
                    path = (DynamicObject) callPathNode.call(frame, yaml, "path", null);
                }

                final Object handler = readHandlerNode.execute(parserObject);

                while (true) {
                    Event event = parser.getEvent();

                    // FIXME: Event should expose a getID, so it can be switched
                    if (event.is(Event.ID.StreamStart)) {
                        callStartStreamNode.call(frame, handler, "start_stream", null, YAMLEncoding.YAML_ANY_ENCODING.ordinal());
                    } else if (event.is(Event.ID.DocumentStart)) {
                        DumperOptions.Version _version = ((DocumentStartEvent) event).getVersion();
                        Integer[] versionInts = _version == null ? null : _version.getArray();
                        Object version = versionInts == null ?
                                Layouts.ARRAY.createArray(coreLibrary().getArrayFactory(), null, 0) :
                                Layouts.ARRAY.createArray(coreLibrary().getArrayFactory(), new Object[]{ versionInts[0], versionInts[1] }, 2);

                        Map<String, String> tagsMap = ((DocumentStartEvent) event).getTags();
                        DynamicObject tags = Layouts.ARRAY.createArray(coreLibrary().getArrayFactory(), null, 0);
                        if (tagsMap != null && tagsMap.size() > 0) {
                            for (Map.Entry<String, String> tag : tagsMap.entrySet()) {
                                Object key = stringFor(tag.getKey(), tainted);
                                Object value = stringFor(tag.getValue(), tainted);
                                DebugHelpers.eval(getContext(), "tags.push [key, value]", "tags", tags, "key", key, "value", value);
                            }
                        }
                        Object notExplicit = !((DocumentStartEvent) event).getExplicit();

                        callStartDocumentNode.call(frame, handler, "start_document", null, version, tags, notExplicit);
                    } else if (event.is(Event.ID.DocumentEnd)) {
                        Object notExplicit = !((DocumentEndEvent) event).getExplicit();
                        callEndDocumentNode.call(frame, handler, "end_document", null, notExplicit);
                    } else if (event.is(Event.ID.Alias)) {
                        Object alias = stringOrNilFor(((AliasEvent) event).getAnchor(), tainted);
                        callAliasNode.call(frame, handler, "alias", null, alias);
                    } else if (event.is(Event.ID.Scalar)) {
                        Object anchor = stringOrNilFor(((ScalarEvent) event).getAnchor(), tainted);
                        Object tag = stringOrNilFor(((ScalarEvent) event).getTag(), tainted);
                        Object plain_implicit = ((ScalarEvent) event).getImplicit().canOmitTagInPlainScalar();
                        Object quoted_implicit = ((ScalarEvent) event).getImplicit().canOmitTagInNonPlainScalar();
                        Object style = translateStyle(((ScalarEvent) event).getStyle());
                        Object val = stringFor(((ScalarEvent) event).getValue(), tainted);

                        callScalarNode.call(frame, handler, "scalar", null, val, anchor, tag, plain_implicit,
                                quoted_implicit, style);
                    } else if (event.is(Event.ID.SequenceStart)) {
                        Object anchor = stringOrNilFor(((SequenceStartEvent) event).getAnchor(), tainted);
                        Object tag = stringOrNilFor(((SequenceStartEvent) event).getTag(), tainted);
                        Object implicit = ((SequenceStartEvent) event).getImplicit();
                        Object style = translateFlowStyle(((SequenceStartEvent) event).getFlowStyle());

                        callStartSequenceNode.call(frame, handler, "start_sequence", null, anchor, tag, implicit, style);
                    } else if (event.is(Event.ID.SequenceEnd)) {
                        callEndSequenceNode.call(frame, handler, "end_sequence", null);
                    } else if (event.is(Event.ID.MappingStart)) {
                        Object anchor = stringOrNilFor(((MappingStartEvent) event).getAnchor(), tainted);
                        Object tag = stringOrNilFor(((MappingStartEvent) event).getTag(), tainted);
                        Object implicit = ((MappingStartEvent) event).getImplicit();
                        Object style = translateFlowStyle(((MappingStartEvent) event).getFlowStyle());

                        callStartMappingNode.call(frame, handler, "start_mapping", null, anchor, tag, implicit, style);
                    } else if (event.is(Event.ID.MappingEnd)) {
                        callEndMappingNode.call(frame, handler, "end_mapping", null);
                    } else if (event.is(Event.ID.StreamEnd)) {
                        callEndStreamNode.call(frame, handler, "end_stream", null);
                        break;
                    }
                }
            } catch (ParserException pe) {
                final Mark mark = pe.getProblemMark();

                Object[] arguments = new Object[]{"file", path, "line", mark.getLine(), "col", mark.getColumn(), "offset", mark.getIndex(), "problem", pe.getProblem() == null ? nil() : createString(new ByteList(pe.getProblem().getBytes(StandardCharsets.UTF_8))), "context", pe.getContext() == null ? nil() : createString(new ByteList(pe.getContext().getBytes(StandardCharsets.UTF_8)))};
                raiseSyntaxErrorSnippetNode.execute(frame, "raise Psych::SyntaxError.new(file, line, col, offset, problem, context)", arguments);
            } catch (ScannerException se) {
                StringBuilder message = new StringBuilder("syntax error");
                if (se.getProblemMark() != null) {
                    message.append(se.getProblemMark().toString());
                }
                final Mark mark = se.getProblemMark();

                Object[] arguments = new Object[]{"file", path, "line", mark.getLine(), "col", mark.getColumn(), "offset", mark.getIndex(), "problem", se.getProblem() == null ? nil() : createString(new ByteList(se.getProblem().getBytes(StandardCharsets.UTF_8))), "context", se.getContext() == null ? nil() : createString(new ByteList(se.getContext().getBytes(StandardCharsets.UTF_8)))};
                raiseSyntaxErrorSnippetNode.execute(frame, "raise Psych::SyntaxError.new(file, line, col, offset, problem, context)", arguments);
            } catch (ReaderException re) {
                Object[] arguments = new Object[]{"file", path, "line", 0, "col", 0, "offset", re.getPosition(), "problem", re.getName() == null ? nil() : createString(new ByteList(re.getName().getBytes(StandardCharsets.UTF_8))), "context", re.toString() == null ? nil() : createString(new ByteList(re.toString().getBytes(StandardCharsets.UTF_8)))};
                raiseSyntaxErrorSnippetNode.execute(frame, "raise Psych::SyntaxError.new(file, line, col, offset, problem, context)", arguments);
            } catch (Throwable t) {
                Helpers.throwException(t);
                return parserObject;
            }

            return parserObject;
        }

        protected ReadObjectFieldNode createReadHandlerNode() {
            return ReadObjectFieldNodeGen.create("@handler", nil());
        }

        private static int translateStyle(Character style) {
            if (style == null) return 0; // any

            switch (style) {
                case 0:
                    return 1; // plain
                case '\'':
                    return 2; // single-quoted
                case '"':
                    return 3; // double-quoted
                case '|':
                    return 4; // literal
                case '>':
                    return 5; // folded
                default:
                    return 0; // any
            }
        }

        private static int translateFlowStyle(Boolean flowStyle) {
            if (flowStyle == null) return 0; // any

            if (flowStyle) return 2;
            return 1;
        }

        private Object stringOrNilFor(String value, boolean tainted) {
            if (value == null) return nil(); // No need to taint nil

            return stringFor(value, tainted);
        }

        private Object stringFor(String value, boolean tainted) {
            // TODO CS 23-Sep-15 this is JRuby's internal encoding, not ours
            Encoding encoding = getContext().getJRubyRuntime().getDefaultInternalEncoding();

            if (encoding == null) {
                encoding = UTF8Encoding.INSTANCE;
            }

            Charset charset = RubyEncoding.UTF8;
            if (encoding.getCharset() != null) {
                charset = encoding.getCharset();
            }

            ByteList bytes = new ByteList(value.getBytes(charset), encoding);
            Object string = createString(bytes);

            if (tainted) {
                DebugHelpers.eval(getContext(), "string.taint", "string", string);
            }

            return string;
        }

    }

}
