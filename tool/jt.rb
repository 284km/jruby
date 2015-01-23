# Copyright (c) 2015 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
# 
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

# A workflow tool for JRuby+Truffle development

# Recommended: function jt { ruby tool/jt.rb $@; }

module ShellUtils
  private

  def sh(*args)
    system(args.join(' '))
    raise "failed" unless $? == 0
  end

  def mvn(*args)
    sh 'mvn', *args
  end

  def mspec(command, *args)
    sh 'ruby', 'spec/mspec/bin/mspec', command, '--config', 'spec/truffle/truffle.mspec', *args
  end
end

module Commands
  include ShellUtils

  def help
    puts 'jt build                                     build'
    puts 'jt clean                                     clean'
    puts 'jt rebuild                                   clean and build'
    puts 'jt run args...                               run JRuby with -X+T and args'
    puts 'jt test                                      run all specs'
    puts 'jt test fast                                 run all specs except sub-processes, GC, sleep, ...'
    puts 'jt test spec/ruby/language                   run specs in this directory'
    puts 'jt test spec/ruby/language/while_spec.rb     run specs in this file'
    puts 'jt tag spec/ruby/language                    tag failing specs in this directory'
    puts 'jt tag spec/ruby/language/while_spec.rb      tag failing specs in this file'
    puts 'jt untag spec/ruby/language                  untag passing specs in this directory'
    puts 'jt untag spec/ruby/language/while_spec.rb    untag passing specs in this file'
    puts 'jt findbugs                                  run findbugs'
    puts 'jt findbugs report                           run findbugs and generate an HTML report'
    puts
    puts 'you can also put build or rebuild in front of any command'
  end

  def build
    mvn 'package'
  end

  def clean
    mvn 'clean'
  end

  def rebuild
    clean
    build
  end

  def run(*args)
    sh *(%w[VERIFY_JRUBY=1 bin/jruby -J-cp truffle/target/jruby-truffle-9.0.0.0-SNAPSHOT.jar -X+T] + args)
  end

  def test(*args)
    options = %w[--excl-tag fails]
    if args.first == 'fast'
      args.shift
      options += %w[--excl-tag slow]
    end
    args = [':language', ':core'] if args.empty?
    mspec 'run', *options, *args
  end

  def tag(path, *args)
    mspec 'tag', '--add', 'fails', '--fail', path, *args
  end

  def untag(path, *args)
    puts
    puts "WARNING: untag is currently not very reliable - run `jt test #{path}` after and manually annotate any new failures"
    puts
    mspec 'tag', '--del', 'fails', '--pass', path, *args
  end

  def findbugs(report=nil)
    case report
    when "report"
      sh 'tool/truffle-findbugs.sh', '--report' rescue nil
      sh 'open', 'truffle-findbugs-report.html' rescue nil
    when nil
      sh 'tool/truffle-findbugs.sh'
    else
      raise ArgumentError, report
    end
  end
end

class JT
  include Commands

  def main(args)
    args = args.dup

    if args.empty? or %w[-h -help --help].include? args.first
      help
      exit
    end

    send args.shift if %w[build rebuild].include? args.first

    return if args.empty?

    commands = Commands.public_instance_methods(false).map(&:to_s)

    abort "no command matched #{args.first.inspect}" unless commands.include?(args.first)

    begin
      send(*args)
    rescue
      puts "Error during command: #{args*' '}"
      raise $!
    end
  end
end

JT.new.main(ARGV)
