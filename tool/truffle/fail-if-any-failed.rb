#!/usr/bin/env ruby
# Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
# This code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

require 'json'

result = JSON.parse(File.read(ARGV[0]))
wait = ARGV[1] == '--wait'

failures = []

if File.exist?('failures')
  failures = File.read('failures').split("\n").map { |failure| eval(failure) }
else
  failures = []
end

any_failed = result['queries'].any? do |q|
  if q['extra.error'] == 'failed'
    failures.push [q['host-vm'], q['host-vm-config'], q['guest-vm'], q['guest-vm-config'], q['bench-suite'], q['benchmark']]
  end
end

if wait
  if any_failed
    STDERR.puts 'waiting to return failure...'
    File.write('failures', failures.map(&:inspect).join("\n"))
  end
else
  if any_failed || File.exist?('failures')
    STDERR.puts 'these failed:'
    failures.each do |failure|
      STDERR.puts failure.inspect
    end
    exit 1
  end
end
