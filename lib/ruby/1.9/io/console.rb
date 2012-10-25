# This implementation of io/console is a little hacky. It shells out to `stty`
# for most operations, which does not work on Windows, in secured environments,
# and so on. In addition, because on Java 6 we can't actually launch
# subprocesses with tty control, stty will not actually manipulate the
# controlling terminal.
#
# For platforms where shelling to stty does not work, most operations will
# just be pass-throughs. This allows them to function, but does not actually
# change any tty flags.
#
# Finally, since we're using stty to shell out, we can only manipulate stdin/
# stdout tty rather than manipulating whatever terminal is actually associated
# with the IO we're calling against. This will produce surprising results if
# anyone is actually using io/console against non-stdio ttys...but that case
# seems like it would be pretty rare.
#
# Note: we are incorporating this into 1.7.0 since RubyGems uses io/console
# when pushing gems, in order to mask the password entry. Worst case is that
# we don't actually disable echo and the password is shown...we will try to
# do a better version of this in 1.7.1.

# attempt to call stty; if failure, fall back on stubbed version

if RbConfig::CONFIG['host_os'].downcase =~ /darwin|openbsd|freebsd|netbsd/
  result = begin
    require File.join(File.dirname(__FILE__), 'bsd_console')

    class IO

      def ttymode
        termios = LibC::Termios.new
        if LibC.tcgetattr(fileno, termios) != 0
          raise SystemCallError.new("tcgetattr", FFI.errno)
        end

        if block_given?
          yield tmp = termios.dup
          if LibC.tcsetattr(fileno, LibC::TCSADRAIN, tmp) != 0
            raise SystemCallError.new("tcsetattr", FFI.errno)
          end
        end
        termios
      end

      def ttymode_yield(block, &setup)
        begin
          orig_termios = ttymode &setup
          block.call(self)
        ensure
          if orig_termios && LibC.tcsetattr(fileno, LibC::TCSADRAIN, orig_termios) != 0
            raise SystemCallError.new("tcsetattr", FFI.errno)
          end
        end
      end

      def raw(*, &block)
        ttymode_yield(block) do |t|
          LibC.cfmakeraw(t)
          t[:c_lflag] &= ~(LibC::ECHOE|LibC::ECHOK)
        end
      end

      def raw!(*)
        ttymode do |t|
          LibC.cfmakeraw(t)
          t[:c_lflag] &= ~(LibC::ECHOE|LibC::ECHOK)
        end
      end

      def cooked(*, &block)
        ttymode_yield(block) do |t|
          t[:c_iflag] |= (LibC::BRKINT|LibC::ISTRIP|LibC::ICRNL|LibC::IXON)
          t[:c_oflag] |= LibC::OPOST
          t[:c_lflag] |= (LibC::ECHO|LibC::ECHOE|LibC::ECHOK|LibC::ECHONL|LibC::ICANON|LibC::ISIG|LibC::IEXTEN)
        end
      end

      def cooked!(*)
        ttymode do |t|
          t[:c_iflag] |= (LibC::BRKINT|LibC::ISTRIP|LibC::ICRNL|LibC::IXON)
          t[:c_oflag] |= LibC::OPOST
          t[:c_lflag] |= (LibC::ECHO|LibC::ECHOE|LibC::ECHOK|LibC::ECHONL|LibC::ICANON|LibC::ISIG|LibC::IEXTEN)
        end
      end

      def echo=(echo)
        ttymode do |t|
          if echo
            t[:c_lflag] |= (LibC::ECHO | LibC::ECHOE | LibC::ECHOK | LibC::ECHONL)
          else
            t[:c_lflag] &= ~(LibC::ECHO | LibC::ECHOE | LibC::ECHOK | LibC::ECHONL)
          end
        end
      end

      def echo?
        (ttymode[:c_lflag] & (LibC::ECHO | LibC::ECHONL)) != 0
      end

      def noecho(&block)
        ttymode_yield(block) { |t| t[:c_lflag] &= ~(LibC::ECHO | LibC::ECHOE | LibC::ECHOK | LibC::ECHONL) }
      end

      def getch(*)
        getc
      end

      def winsize
        ws = LibC::Winsize.new
        if LibC.ioctl(fileno, LibC::TIOCGWINSZ, :pointer, ws.pointer) != 0
          raise SystemCallError.new("ioctl(TOCGWINSZ)", FFI.errno)
        end
        [ ws[:ws_row], ws[:ws_col] ]
      end

      def winsize=(size)
        ws = LibC::Winsize.new
        if LibC.ioctl(fileno, LibC::TIOCGWINSZ, :pointer, ws.pointer) != 0
          raise SystemCallError.new("ioctl(TOCGWINSZ)", FFI.errno)
        end

        ws[:ws_row] = size[0]
        ws[:ws_col] = size[1]
        if LibC.ioctl(fileno, LibC::TIOCSWINSZ, :pointer, ws.pointer) != 0
          raise SystemCallError.new("ioctl(TOCSWINSZ)", FFI.errno)
        end
      end

      def iflush
        raise SystemCallError.new("tcflush(TCIFLUSH)", FFI.errno) unless LibC.tcflush(fileno, LibC::TCIFLUSH) == 0
      end

      def oflush
        raise SystemCallError.new("tcflush(TCOFLUSH)", FFI.errno) unless LibC.tcflush(fileno, LibC::TCOFLUSH) == 0
      end

      def ioflush
        raise SystemCallError.new("tcflush(TCIOFLUSH)", FFI.errno) unless LibC.tcflush(fileno, LibC::TCIOFLUSH) == 0
      end
    end
    true
  rescue Exception => ex
    $stderr.puts "failed to load native termios support: #{ex}"
    false
  end
else
  result = begin
    old_stderr = $stderr.dup
    $stderr.reopen('/dev/null')
    `stty -a`
    $?.exitstatus != 0
  rescue Exception
    nil
  ensure
    $stderr.reopen(old_stderr)
  end
end

if !result || RbConfig::CONFIG['host_os'] =~ /(mswin)|(win32)|(ming)/
  warn "io/console not supported; tty will not be manipulated"

  # Windows version is always stubbed for now
  class IO
    def raw(*)
      yield self
    end

    def raw!(*)
    end

    def cooked(*)
      yield self
    end

    def cooked!(*)
    end

    def getch(*)
      getc
    end

    def echo=(echo)
    end

    def echo?
      true
    end

    def noecho
      yield self
    end

    def winsize
      [25, 80]
    end

    def winsize=(size)
    end

    def iflush
    end

    def oflush
    end

    def ioflush
    end
  end
elsif !IO.method_defined?:ttymode
  warn "io/console on JRuby shells out to stty for most operations"

  # Non-Windows assumes stty command is available
  class IO
    def raw(*)
      saved = `stty -g`
      `stty raw`
      yield self
    ensure
      `stty #{saved}`
    end

    def raw!(*)
      `stty raw`
    end

    def cooked(*)
      saved = `stty -g`
      `stty cooked`
      yield self
    ensure
      `stty #{saved}`
    end

    def cooked!(*)
      `stty -raw`
    end

    def getch(*)
      getc
    end

    def echo=(echo)
      `stty #{echo ? 'echo' : '-echo'}`
    end

    def echo?
      (`stty -a` =~ (/ -echo /)) ? false : true
    end

    def noecho
      saved = `stty -g`
      `stty -echo`
      yield self
    ensure
      `stty #{saved}`
    end

    # Not all systems return same format of stty -a output
    IEEE_STD_1003_2 = '(?<rows>\d+) rows; (?<columns>\d+) columns'
    UBUNTU = 'rows (?<rows>\d+); columns (?<columns>\d+)'

    def winsize
      match = `stty -a`.match(/#{IEEE_STD_1003_2}|#{UBUNTU}/)
      [match[:rows].to_i, match[:columns].to_i]
    end

    def winsize=(size)
      `stty rows #{size[0]} cols #{size[1]}`
    end

    def iflush
    end

    def oflush
    end

    def ioflush
    end
  end
end
