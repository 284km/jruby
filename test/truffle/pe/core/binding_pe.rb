# Copyright (c) 2015 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

# why does this fail but the next one work?
tagged_example "x = 14; p = Proc.new { }; p.binding.local_variable_get(:x)", 14

example "x = 14; p = Proc.new { }; p.binding.local_variable_get(:x) * 2", 28
