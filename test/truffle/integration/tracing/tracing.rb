# Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

$trace = []

$trace_proc = proc { |*args|
   $trace << args
}

def check(file)
  expected = nil
  
  File.open('test/truffle/integration/tracing/' + file) do |f|
    expected = f.each_line.map { |line| eval(line) }
  end
  
  actual = $trace
  
  #p actual
  
  while actual.size < expected.size
    actual.push [:missing, :missing, :missing, :missing, :missing, :missing]
  end
  
  while expected.size < actual.size
    expected.push [:missing, :missing, :missing, :missing, :missing, :missing]
  end
  
  success = true
  
  expected.zip(actual).each do |e, a|
    unless a[0] == e[0]
      puts "Expected #{e[0]}, actually #{a[0]}"
      success = false
    end
    
    unless a[1].end_with?(e[1])
      puts "Expected #{e[1]}, actually #{a[1]}"
      success = false
    end
  
    unless a[2] == e[2]
      puts "Expected #{e[2]}, actually #{a[2]}"
      success = false
    end
  
    unless a[3] == e[3]
      puts "Expected #{e[3]}, actually #{a[3]}"
      success = false
    end
  
    unless a[4].is_a?(Binding)
      puts "Expected Binding, actually #{a[4].class}"
      success = false
    end
  
    unless a[5] == e[5]
      puts "Expected #{e[5]}, actually #{a[5]}"
      success = false
    end
  end
  
  unless success
    exit 1
  end
end
