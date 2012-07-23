package org.jruby.util;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.util.HashSet;
import java.util.Set;
import org.jcodings.Encoding;
import org.jruby.Ruby;
import org.jruby.RubyHash;
import org.jruby.RubyString;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;

/**
 * Encapsulate all logic associated with using Java Charset transcoding 
 * facilities.
 */
public class CharsetTranscoder {
    // Java seems to find these specific Java charsets but they seem to trancode
    // some strings a little differently than MRI.  Since Java Charset transcoding
    // is a temporary implementation for us, having this gruesome hack is ok
    // for the time being.
    private static Set<String> BAD_TRANSCODINGS_HACK = new HashSet<String>() {{
        add("ISO-2022-JP-2");
        add("CP50220");
        add("CP50221");
    }};
    
    private Encoding toEncoding;
    private CodingErrorActions actions;
    private Encoding forceEncoding = null;
    
    public CharsetTranscoder(ThreadContext context, Encoding toEncoding, IRubyObject options) {
        this.toEncoding = toEncoding;

        actions = getCodingErrorActions(context, options);
    }
    
    public CharsetTranscoder(ThreadContext context, Encoding toEncoding, Encoding forceEncoding, IRubyObject options) {
        this(context, toEncoding, options);
        
        this.forceEncoding = forceEncoding;
    }
    
    public ByteList transcode(ThreadContext context, ByteList value) {
        Encoding fromEncoding = forceEncoding != null ? forceEncoding : value.getEncoding();
        
        return transcode(context.runtime, value, fromEncoding);
    }
    
    protected ByteList transcode(Ruby runtime, ByteList value, Encoding fromEncoding) {
        Encoding encoding = toEncoding != null ? toEncoding : value.getEncoding();
        String toName = encoding.toString();
        String fromName = fromEncoding.toString();
        
        Charset from = transcodeCharsetFor(runtime, fromEncoding, fromName, toName);
        Charset to = transcodeCharsetFor(runtime, encoding, fromName, toName);

        CharsetEncoder encoder = getCharsetEncoder(to);
        CharsetDecoder decoder = getCharsetDecoder(from);

        ByteBuffer fromBytes = ByteBuffer.wrap(value.getUnsafeBytes(), value.begin(), value.length());

        // MRI does not allow ASCII-8BIT chars > 127 to transcode to multibyte encodings
        if (fromName.equals("ASCII-8BIT") && encoder.maxBytesPerChar() > 1.0) {
            for (int byteidx = value.begin(); byteidx < value.begin() + value.length(); byteidx++) {
                byte b = fromBytes.get(byteidx);
                if ((b & 0xFF) > 0x7F) {
                    throw runtime.newUndefinedConversionError(
                            "\"\\x" + Integer.toHexString(b & 0xFF).toUpperCase() +
                                    "\" from " + fromName +
                                    " to " + toName);
                }
            }
        }
        
        try {
            ByteBuffer toBytes = encoder.encode(decoder.decode(fromBytes));

            // CharsetEncoder#encode guarantees a newly-allocated buffer, so no need to copy.
            return new ByteList(toBytes.array(), toBytes.arrayOffset(),
                    toBytes.limit() - toBytes.arrayOffset(), encoding, false);
        } catch (CharacterCodingException e) {
            throw runtime.newUndefinedConversionError(e.getLocalizedMessage());
        }        
    }

    /**
     * This will try and transcode the supplied ByteList to the supplied toEncoding.  It will use
     * forceEncoding as its encoding if it is supplied; otherwise it will use the encoding it has
     * tucked away in the bytelist.  This will return a new copy of a ByteList in the request
     * encoding or die trying (ConverterNotFound).
     * 
     * c: rb_str_conv_enc_opts
     */
    public static ByteList transcode(ThreadContext context, ByteList value, Encoding forceEncoding,
            Encoding toEncoding, IRubyObject opts) {
        if (toEncoding == null) return value;
        
        return new CharsetTranscoder(context, toEncoding, forceEncoding, opts).transcode(context, value);
    }

    private static class CodingErrorActions {
        final CodingErrorAction onUnmappableCharacter;
        final CodingErrorAction onMalformedInput;
        final RubyString replaceWith;

        CodingErrorActions(CodingErrorAction onUnmappableCharacter,
                CodingErrorAction onMalformedInput, RubyString replaceWith) {
            this.onUnmappableCharacter = onUnmappableCharacter;
            this.onMalformedInput = onMalformedInput;
            this.replaceWith = replaceWith;
        }
    }
    
   private static CodingErrorActions getCodingErrorActions(ThreadContext context, IRubyObject opts) {
        if (opts == null || opts.isNil()) {
            return new CodingErrorActions(CodingErrorAction.REPORT,
                    CodingErrorAction.REPORT, null);
        } 

        Ruby runtime = context.runtime;
        RubyHash hash = (RubyHash) opts;
        CodingErrorAction onMalformedInput = CodingErrorAction.REPORT;
        CodingErrorAction onUnmappableCharacter = CodingErrorAction.REPORT;
        RubyString replaceWith = null;
            
        IRubyObject replace = hash.fastARef(runtime.newSymbol("replace"));
        if (replace != null && !replace.isNil()) {
            RubyString replaceWithStr = replace.convertToString();
            if (replaceWithStr.size() == 1) { // we can only replaceWith a single char
                replaceWith = replaceWithStr;
            }
        }
            
        IRubyObject invalid = hash.fastARef(runtime.newSymbol("invalid"));
        if (invalid != null && invalid.op_equal(context, runtime.newSymbol("replace")).isTrue()) {
            onMalformedInput = CodingErrorAction.REPLACE;
        }

        IRubyObject undef = hash.fastARef(runtime.newSymbol("undef"));
        if (undef != null && undef.op_equal(context, runtime.newSymbol("replace")).isTrue()) {
            onUnmappableCharacter = CodingErrorAction.REPLACE;
        }

        return new CodingErrorActions(onUnmappableCharacter, onMalformedInput, replaceWith);

        /*
         * Missing options from MRI 1.9.3 source:

 *  :replace ::
 *    Sets the replacement string to the given value. The default replacement
 *    string is "\uFFFD" for Unicode encoding forms, and "?" otherwise.
 *  :fallback ::
 *    Sets the replacement string by the given object for undefined
 *    character.  The object should be a Hash, a Proc, a Method, or an
 *    object which has [] method.
 *    Its key is an undefined character encoded in the source encoding
 *    of current transcoder. Its value can be any encoding until it
 *    can be converted into the destination encoding of the transcoder.
 *  :xml ::
 *    The value must be +:text+ or +:attr+.
 *    If the value is +:text+ #encode replaces undefined characters with their
 *    (upper-case hexadecimal) numeric character references. '&', '<', and '>'
 *    are converted to "&amp;", "&lt;", and "&gt;", respectively.
 *    If the value is +:attr+, #encode also quotes the replacement result
 *    (using '"'), and replaces '"' with "&quot;".
 *  :cr_newline ::
 *    Replaces LF ("\n") with CR ("\r") if value is true.
 *  :crlf_newline ::
 *    Replaces LF ("\n") with CRLF ("\r\n") if value is true.
 *  :universal_newline ::
 *    Replaces CRLF ("\r\n") and CR ("\r") with LF ("\n") if value is true.
 *    
             */
    }    

    private CharsetDecoder getCharsetDecoder(Charset charset) {
        CharsetDecoder decoder = charset.newDecoder();

        decoder.onUnmappableCharacter(actions.onUnmappableCharacter);
        decoder.onMalformedInput(actions.onMalformedInput);
        
        if (actions.replaceWith != null) decoder.replaceWith(actions.replaceWith.toString());

        return decoder;
    }

    private CharsetEncoder getCharsetEncoder(Charset charset) {
        CharsetEncoder encoder = charset.newEncoder();

        encoder.onUnmappableCharacter(actions.onUnmappableCharacter);
        encoder.onMalformedInput(actions.onMalformedInput);
        if (actions.replaceWith != null) {
            encoder.replaceWith(actions.replaceWith.getBytes());
        }

        return encoder;
    } 

    private static Charset transcodeCharsetFor(Ruby runtime, Encoding encoding, String fromName, String toName) {
        Charset from = null;
        String realEncodingName = new String(encoding.getName());
        
        // Doing a manual forName over and over sucks, but this is only meant
        // to be a transitional impl.  The reason for this extra mechanism is 
        // that jcodings is representing these encodings with an alias.  So,
        // for example, IBM866 ends up being associated with ISO-8859-1 which
        // will not know how to trancsode higher than ascii values properly.
        if (!realEncodingName.equals(encoding.getCharsetName()) && !BAD_TRANSCODINGS_HACK.contains(realEncodingName)) {
            try {
                from = Charset.forName(realEncodingName);
                
                if (from != null) return from;
            } catch (Exception e) {}
        }
        try {
            from = encoding.getCharset();
            
            if (from != null) return from;
        } catch (Exception e) {}

        try { // We try looking up based on Java's supported charsets...likely missing charset entry in jcodings
            from = Charset.forName(encoding.toString());
        } catch (Exception e) {}
        
        if (from == null) {
            throw runtime.newConverterNotFoundError("code converter not found (" + fromName + " to " + toName + ")");
        }

        return from;
    }    
}
