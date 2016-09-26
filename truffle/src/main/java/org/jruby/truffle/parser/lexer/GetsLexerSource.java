/*
 ***** BEGIN LICENSE BLOCK *****
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
 ***** END LICENSE BLOCK *****/
package org.jruby.truffle.parser.lexer;

import org.jcodings.Encoding;
import org.jruby.RubyArray;
import org.jruby.RubyEncoding;
import org.jruby.RubyIO;
import org.jruby.RubyString;
import org.jruby.runtime.builtin.IRubyObject;
import org.jruby.util.ByteList;
import org.jruby.util.IOInputStream;
import org.jruby.util.io.ChannelHelper;

import java.nio.channels.Channel;

/**
 *  Lexer source from ripper getting a line at a time via 'gets' calls.
 */
public class GetsLexerSource extends LexerSource {
    private IRubyObject io;
    private Encoding encoding;
    private int offset;

    // Main-line Parsing constructor
    public GetsLexerSource(String sourceName, int line, IRubyObject io, RubyArray scriptLines, Encoding encoding) {
        super(sourceName, line, scriptLines);

        this.io = io;
        this.encoding = encoding;
    }

    // FIXME (enebo 21-Apr-15): ripper probably has same problem as main-line parser so this constructor
    // may need to be a mix of frobbing the encoding of an incoming object plus defaultEncoding if not.
    // But main-line parser should not be asking IO for encoding.
    // Ripper constructor
    public GetsLexerSource(String sourceName, int line, IRubyObject io, RubyArray scriptLines) {
        this(sourceName, line, io, scriptLines, frobnicateEncoding(io));
    }

    // FIXME: Should be a hard failure likely if no encoding is possible
    public static final Encoding frobnicateEncoding(IRubyObject io) {
        // Non-ripper IO will not have encoding so we will just use default external
        if (!io.respondsTo("encoding")) return io.getRuntime().getDefaultExternalEncoding();
        
        IRubyObject encodingObject = io.callMethod(io.getRuntime().getCurrentContext(), "encoding");

        return encodingObject instanceof RubyEncoding ? 
                ((RubyEncoding) encodingObject).getEncoding() : io.getRuntime().getDefaultExternalEncoding();
    }
    
    @Override
    public Encoding getEncoding() {
        return encoding;
    }

    @Override
    public void setEncoding(Encoding encoding) {
        this.encoding = encoding;
        encodeExistingScriptLines(encoding);
    }

    @Override
    public ByteList gets() {
        IRubyObject result = io.callMethod(io.getRuntime().getCurrentContext(), "gets");
        
        if (result.isNil()) return null;
        
        ByteList bytelist = result.convertToString().getByteList();
        offset += bytelist.getRealSize();
        bytelist.setEncoding(encoding);

        if (scriptLines != null) scriptLines.append(RubyString.newString(scriptLines.getRuntime(), bytelist));

        return bytelist;
    }

    @Override
    public int getOffset() {
        return offset;
    }

    @Override
    public Channel getRemainingAsChannel() {
        if (io instanceof RubyIO) return ((RubyIO) io).getChannel();
        return ChannelHelper.readableChannel(new IOInputStream(io));
    }

    @Override
    public IRubyObject getRemainingAsIO() {
        return io;
    }
}
