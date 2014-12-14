# Copyright (c) 2014 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
# 
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

module STDIN
  def self.external_encoding
    @external || Encoding.default_external
  end

  def self.internal_encoding
    @internal
  end

  def self.set_encoding(external, internal)
    @external = external
    @internal = internal
  end
end

module STDOUT
  def self.print(*values)
    Kernel.send(:print, *values)
  end

  def self.write(value)
    print value
  end

  def self.flush
    Truffle::Debug.flush_stdout
  end

  def self.external_encoding
    @external
  end

  def self.internal_encoding
    @internal
  end

  def self.set_encoding(external, internal)
    @external = external
    @internal = internal
  end
end

$stdout = STDOUT

module STDERR
  def self.external_encoding
    @external
  end

  def self.internal_encoding
    @internal
  end

  def self.set_encoding(external, internal)
    @external = external
    @internal = internal
  end
end

ARGF = Object.new

NIL = nil
TRUE = true
FALSE = false
