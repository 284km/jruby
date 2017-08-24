module JRuby
  class << self
    # Get a Java integration reference to the given (Ruby) object.
    # @note implemented in *org.jruby.ext.jruby.JRubyLibrary*
    def reference(obj); end if false

    # Turn a Java integration reference (to a Ruby object) back into a normal Ruby object reference.
    # @note implemented in *org.jruby.ext.jruby.JRubyLibrary*
    def dereference(obj); end if false

    # Get the current JRuby runtime.
    # @note implemented in *org.jruby.ext.jruby.JRubyLibrary*
    def runtime; end if false

    # Get the current runtime's config.
    # Changes to the configuration won't be reflected in the runtime, meant to be read-only.
    # @note implemented in *org.jruby.ext.jruby.JRubyLibrary*
    def config; end if false

    # Run the provided (required) block with the "global runtime" set to the current runtime,
    # for libraries that expect to operate against the global runtime.
    # @note implemented in *org.jruby.ext.jruby.JRubyLibrary*
    def with_current_runtime_as_global; end if false

    # Change the current threads context classloader.
    # By, default call with no arguments to replace it with JRuby's class loader.
    # @note implemented in *org.jruby.ext.jruby.JRubyLibrary*
    def set_context_class_loader(loader = nil); end if false

    # Parse the given block or the provided content, returning a JRuby AST node.
    # @note implemented in *org.jruby.ext.jruby.JRubyLibrary*
    def parse(content, filename = '', extra_position_info = false, lineno = 0); end if false

    # @note implemented in *org.jruby.ext.jruby.JRubyLibrary*
    def compile_ir(content, filename = '', extra_position_info = false); end if false

    # Parse and compile the given block or provided content.
    # @return JRuby::CompiledScript instance
    # @note implemented in *org.jruby.ext.jruby.JRubyLibrary*
    def compile(content, filename = '', extra_position_info = false); end if false

  end

  # NOTE: This is not a public API and is subject to change at our whim.
  # @private no longer used - to be removed
  module IR
    def self.debug=(value)
      org.jruby.RubyInstanceConfig.IR_DEBUG = !!value
    end

    def self.debug
      org.jruby.RubyInstanceConfig.IR_DEBUG
    end

    def self.compiler_debug=(value)
      org.jruby.RubyInstanceConfig.IR_COMPILER_DEBUG = !!value
    end

    def self.compiler_debug
      org.jruby.RubyInstanceConfig.IR_COMPILER_DEBUG
    end

    def self.visualize=(value)
      org.jruby.RubyInstanceConfig.IR_VISUALIZER = !!value
    end

    def self.visualize
      org.jruby.RubyInstanceConfig.IR_VISUALIZER
    end
  end
  deprecate_constant :IR

  class CompiledScript

    attr_reader :name, :class_name, :original_script, :code

    # @private
    def initialize(filename, class_name, content, bytes)
      @name = filename
      @class_name = class_name
      @original_script = content
      @code = bytes
    end

    def to_s
      @original_script
    end

    def inspect
      "\#<#{self.class.name} #{@name}>"
    end

    def inspect_bytecode
      JRuby.init_asm

      writer = java.io.StringWriter.new
      reader = ClassReader.new(@code)
      tracer = TraceClassVisitor.new(java.io.PrintWriter.new(writer))

      reader.accept(tracer, ClassReader::SKIP_DEBUG)

      writer.to_s
    end

  end

  # @private
  def self.init_asm
    return if const_defined? :TraceClassVisitor
    begin
      const_set(:TraceClassVisitor, org.jruby.org.objectweb.asm.util.TraceClassVisitor)
      const_set(:ClassReader, org.jruby.org.objectweb.asm.ClassReader)
    rescue
      const_set(:TraceClassVisitor, org.objectweb.asm.util.TraceClassVisitor)
      const_set(:ClassReader, org.objectweb.asm.ClassReader)
    end
  end

end
