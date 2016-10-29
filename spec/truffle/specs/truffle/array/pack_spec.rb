# Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1
# OTHER DEALINGS IN THE SOFTWARE.

require_relative '../../../../ruby/spec_helper'

describe "Array#pack" do
  it "should be able to pack a tarball entry" do
    ary = ["metadata.gz", "0000444", "0000000", "0000000", "00000001244", "00000000044", "        ", " ", "0", nil,
           "ustar", "00", "wheel", "wheel", "0000000", "0000000", ""]
    pack_format = "a100a8a8a8a12a12a7aaa100a6a2a32a32a8a8a155"
    packed = ary.pack(pack_format)

    packed.should == "metadata.gz0000444000000000000000000000124400000000044        0ustar00wheelwheel00000000000000"
  end
end
