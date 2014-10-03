package org.jruby.truffle.runtime.core;

import org.jcodings.Encoding;
import org.jcodings.specific.USASCIIEncoding;
import org.jruby.truffle.nodes.RubyNode;

/**
 * This is a bridge between JRuby encoding and Truffle encoding
 */
public class RubyEncoding extends RubyObject{

    private final org.jruby.RubyEncoding rubyEncoding;

    /**
     * The class from which we create the object that is {@code Encoding}. A subclass of
     * {@link RubyClass} so that we can override {@link RubyClass#newInstance} and allocate a
     * {@link RubyEncoding} rather than a normal {@link RubyBasicObject}.
     */
    public static class RubyEncodingClass extends RubyClass {

        public RubyEncodingClass(RubyClass objectClass) {
            super(null, null, objectClass, "Encoding");
        }

        @Override
        public RubyBasicObject newInstance(RubyNode currentNode) {
            return new RubyEncoding(getContext().getCoreLibrary().getEncodingClass(), USASCIIEncoding.INSTANCE);
        }

    }

    public RubyEncoding(RubyClass encodingClass, Encoding encoding) {
        super(encodingClass);
        assert encoding != null;
        rubyEncoding = org.jruby.RubyEncoding.newEncoding(getJRubyRuntime(), encoding);
    }

    public RubyEncoding(RubyClass encodingClass, org.jruby.RubyEncoding jrubyEncoding) {
        super(encodingClass);
        this.rubyEncoding = jrubyEncoding;
    }

    public static RubyEncoding findEncodingByName(RubyString name) {
        RubyNode.notDesignedForCompilation();

        org.jruby.RubyEncoding enc = findJRubyEncoding(name);
        return new RubyEncoding(name.getContext().getCoreLibrary().getEncodingClass(), enc);
    }

    public static org.jruby.RubyEncoding findJRubyEncoding(RubyString name) {
        RubyNode.notDesignedForCompilation();

        org.jruby.RubyString string = org.jruby.RubyString.newString(name.getJRubyRuntime(), name.toString());
        return (org.jruby.RubyEncoding) name.getJRubyRuntime().getEncodingService().rubyEncodingFromObject(string);
    }
    public String toString(){
        return rubyEncoding.to_s(getJRubyRuntime().getCurrentContext()).asJavaString();
    }

    public org.jruby.RubyEncoding getRubyEncoding() {
        return rubyEncoding;
    }

    public boolean compareTo(RubyEncoding other) {
        RubyNode.notDesignedForCompilation();

        return getRubyEncoding().getEncoding().equals(other.getRubyEncoding().getEncoding());
    }

    @Override
    public boolean equals(Object o) {
        RubyNode.notDesignedForCompilation();

        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        RubyEncoding that = (RubyEncoding) o;

        if (!rubyEncoding.equals(that.rubyEncoding)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        RubyNode.notDesignedForCompilation();

        return rubyEncoding.hashCode();
    }

}