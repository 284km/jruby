# Copyright (c) 2014, 2015 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

module RbConfig
  jruby_home = Truffle::Boot.jruby_home_directory

  bindir = if jruby_home.end_with?('/mxbuild/ruby-zip-extracted')
    File.expand_path('../../bin', jruby_home)
  else
    "#{jruby_home}/bin"
  end

  CONFIG = {
    'exeext' => '',
    'EXEEXT' => '',
    'host_os' => Truffle::System.host_os,
    'host_cpu' => Truffle::System.host_cpu,
    'bindir' => bindir,
    'libdir' => "#{jruby_home}/lib/ruby/truffle",
    "sitelibdir"=>"#{jruby_home}/lib/ruby/2.3/site_ruby", # TODO BJF Oct 21, 2016 Need to review these values
    "sitearchdir"=>"#{jruby_home}/lib/ruby/2.3/site_ruby",
    'ruby_install_name' => 'jruby-truffle',
    'RUBY_INSTALL_NAME' => 'jruby-truffle',
    # 'ruby_install_name' => 'jruby',
    # 'RUBY_INSTALL_NAME' => 'jruby',
    'ruby_version' => '2.2.0',
    'OBJEXT' => 'll',
    'DLEXT' => 'su',
    'rubyhdrdir' => "#{jruby_home}/lib/ruby/truffle/cext",
    'topdir' => "#{jruby_home}/lib/ruby/stdlib",
    "rubyarchhdrdir"=>"#{jruby_home}/lib/ruby/truffle/cext",
    'includedir' => ''
  }

  MAKEFILE_CONFIG = {
      'configure_args' => ' ',
      'ARCH_FLAG' => '',
      'CPPFLAGS' => '',
      'LDFLAGS' => '',
      'DLDFLAGS' => '',
      'DLEXT' => 'su',
      'LIBEXT' => 'c',
      'OBJEXT' => 'll',
      'EXEEXT' => '',
      'LIBS' => '',
      'DLDLIBS' => '',
      'LIBRUBYARG_STATIC' => '',
      'LIBRUBYARG_SHARED' => '',
      'libdirname' => 'libdir',
      'LIBRUBY' => '',
      'LIBRUBY_A' => '',
      'LIBRUBYARG' => '',
      'prefix' => '',
      'ruby_install_name' => 'jruby-truffle',
      'RUBY_SO_NAME' => '$(RUBY_BASE_NAME)',
      'hdrdir' => "#{jruby_home}/lib/ruby/truffle/cext",
      'COMPILE_C' => '$(CC) $(INCFLAGS) $(CPPFLAGS) $(CFLAGS) $(COUTFLAG)$< -o $@'
  }

  if Truffle::Safe.memory_safe? && Truffle::Safe.processes_safe?
    MAKEFILE_CONFIG.merge!({
        'CC' => "mx -p #{ENV['SULONG_HOME']} su-clang -I#{ENV['SULONG_HOME']}/include",
        'CFLAGS' => "  -S -emit-llvm -I#{ENV['OPENSSL_HOME']}/include -DRUBY_EXTCONF_H=\"extconf.h\" -DHAVE_OPENSSL_110_THREADING_API -DHAVE_HMAC_CTX_COPY -DHAVE_EVP_CIPHER_CTX_COPY -DHAVE_BN_RAND_RANGE -DHAVE_BN_PSEUDO_RAND_RANGE -DHAVE_X509V3_EXT_NCONF_NID -Wall -Wno-int-conversion -Wno-int-to-pointer-cast -Wno-unused-variable -Wno-uninitialized -Wno-unused-function -Werror ",
        'LINK_SO' => "mx -p #{ENV['SULONG_HOME']} su-link -o $@ $(OBJS)"
    })
  end

  def self.ruby
    # TODO (eregon, 30 Sep 2016): should be the one used by the launcher!
    File.join CONFIG['bindir'], CONFIG['ruby_install_name'], CONFIG['exeext']
  end

  def RbConfig::expand(val, config = CONFIG)
    newval = val.gsub(/\$\$|\$\(([^()]+)\)|\$\{([^{}]+)\}/) {
      var = $&
      if !(v = $1 || $2)
        '$'
      elsif key = config[v = v[/\A[^:]+(?=(?::(.*?)=(.*))?\z)/]]
        pat, sub = $1, $2
        config[v] = false
        config[v] = RbConfig::expand(key, config)
        key = key.gsub(/#{Regexp.quote(pat)}(?=\s|\z)/n) {sub} if pat
        key
      else
        var
      end
    }
    val.replace(newval) unless newval == val
    val
  end
end

CROSS_COMPILING = nil
