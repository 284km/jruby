/*
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 *
 * This code is modified from the Readline JRuby extension module
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
 * Copyright (C) 2006 Ola Bini <ola@ologix.com>
 * Copyright (C) 2006 Damian Steer <pldms@mac.com>
 * Copyright (C) 2008 Joseph LaFata <joe@quibb.org>
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
package org.jruby.truffle.stdlib.readline;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.source.SourceSection;
import org.jcodings.specific.UTF8Encoding;
import org.jruby.truffle.RubyContext;
import org.jruby.truffle.builtins.CoreClass;
import org.jruby.truffle.builtins.CoreMethod;
import org.jruby.truffle.builtins.CoreMethodArrayArgumentsNode;
import org.jruby.truffle.core.cast.ToStrNode;
import org.jruby.truffle.core.cast.ToStrNodeGen;
import org.jruby.truffle.core.rope.RopeOperations;
import org.jruby.truffle.core.string.StringOperations;
import org.jruby.truffle.language.objects.TaintNode;
import org.jruby.truffle.language.objects.TaintNodeGen;

@CoreClass("Truffle::ReadlineHistory")
public abstract class ReadlineHistoryNodes {

    @CoreMethod(names = { "push", "<<" }, rest = true)
    public abstract static class PushNode extends CoreMethodArrayArgumentsNode {

        @Child private ToStrNode toStrNode;

        public PushNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
            toStrNode = ToStrNodeGen.create(context, sourceSection, null);
        }

        @Specialization
        public DynamicObject push(VirtualFrame frame, DynamicObject history, Object... lines) {
            final ConsoleHolder consoleHolder = getContext().getConsoleHolder();

            for (Object line : lines) {
                final DynamicObject asString = toStrNode.executeToStr(frame, line);
                consoleHolder.getHistory().add(RopeOperations.decodeUTF8(StringOperations.rope(asString)));
            }

            return history;
        }

    }

    @CoreMethod(names = "pop")
    public abstract static class PopNode extends CoreMethodArrayArgumentsNode {

        @Child private TaintNode taintNode;

        public PopNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
            taintNode = TaintNodeGen.create(context, sourceSection, null);
        }

        @Specialization
        public Object pop() {
            final ConsoleHolder consoleHolder = getContext().getConsoleHolder();

            if (consoleHolder.getHistory().isEmpty()) {
                return nil();
            }

            final String lastLine = consoleHolder.getHistory().removeLast().toString();
            final DynamicObject ret = createString(StringOperations.createRope(lastLine, getDefaultInternalEncoding()));

            return taintNode.executeTaint(ret);
        }

    }

}
