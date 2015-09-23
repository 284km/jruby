# Copyright (c) 2014, 2015 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

module IVarFixtures
  class Foo
    attr_reader :a

    def initialize(a, b)
      @a = a
      @b = b
    end

    def b
      @b
    end

    def reset_b(b)
      @b = b
      self
    end
  end
end

example "IVarFixtures::Foo.new(1,2).a"
example "IVarFixtures::Foo.new(1,2).b"

example "IVarFixtures::Foo.new(1,2).reset_b(42).b"
example "IVarFixtures::Foo.new(1,2).reset_b([]).b.empty?"
