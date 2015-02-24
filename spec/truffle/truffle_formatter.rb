require 'mspec/expectations/expectations'
require 'mspec/utils/ruby_name'
require 'mspec/runner/formatters/dotted'

class TruffleFormatter < DottedFormatter
  def initialize(out=nil)
    super
    @tests = []
  end

  def register
    super
    MSpec.register :start, self
    MSpec.register :before, self
    MSpec.register :load, self
    MSpec.register :unload, self
    MSpec.register :tagged, self
  end

  def start
    # TODO (nirvdrum 23-Feb-15) Remove this hack when Truffle supports deleting directories with files and can do so synchronously.
    system("rm -r tmp/*")
    sleep(1)
  end

  def load
    parts = MSpec.retrieve(:file).sub(Dir.pwd + '/', '').split('/')

    @spec_type = parts[0...2].join('.')
    @classname = parts[2...-1].join('_')
    @filename_base = parts[-1].split('.rb').first.split('_spec').first

    @tests.clear
    @file_time = current_time

    (@local_tally = TallyAction.new).register

    @testsuite_name = [@spec_type, @classname, @filename_base].compact.join('.')
    @dir = File.join('tmp', @spec_type, @classname)

    mkdir_p(@dir)

    @filename = File.expand_path(File.join(@dir, "TEST-#{@filename_base}.xml"))
    @file = File.open(@filename, 'w')
  end

  def before(state = nil)
    @spec_time = current_time
  end

  def after(state = nil)
    super
    @tests << {:test => state, :exception => false, :tagged => false, :time => current_time - @spec_time} unless exception?
  end

  def exception(exception)
    super
    @tests << {:test => exception, :exception => true, :tagged => false, :time => current_time - @spec_time}
  end

  def tagged(state = nil)
    @tests << {:test => state, :exception => false, :tagged => true, :time => 0.0}
  end

  def unload
    start = current_time

    tests = @local_tally.counter.examples
    errors = @local_tally.counter.errors
    failures = @local_tally.counter.failures
    tagged = @local_tally.counter.tagged

    skipped_stats = if tagged > 0
                      # TODO (nirvdrum 23-Feb-15) Clean this up when Truffle supports Float#round(int)
                      skipped_percentage = ((tagged / tests.to_f) * 1000).round / 10.0
                      "#{skipped_percentage}% Skipped"
                    else
                      ''
                    end

    @file.puts <<-XML
        <testsuite
          tests="#{tests}"
          errors="#{errors}"
          failures="#{failures}"
          skipped="#{tagged}"
          time="#{current_time - @file_time}"
          hostname="#{skipped_stats}"
          name="#{@testsuite_name}">
    XML

    @tests.each do |h|
      description = encode_for_xml h[:test].description

      @file.puts <<-XML
        <testcase classname="#{@classname}" name="#{description}" time="#{h[:time]}">
      XML

      if h[:exception]
        outcome = h[:test].failure? ? "failure" : "error"
        message = encode_for_xml h[:test].message
        backtrace = encode_for_xml h[:test].backtrace
        @file.puts <<-XML
          <#{outcome} message="error in #{description}" type="#{outcome}">
            #{message}
        #{backtrace}
          </#{outcome}>
        XML
      end

      if h[:tagged]
        @file.puts <<-XML
          <skipped message="tagged"></skipped>
        XML
      end

      @file.puts <<-XML
        </testcase>
      XML
    end


    @file.puts "</testsuite>"
    @file.close

    @local_tally.unregister
  end

  private
  LT = "&lt;"
  GT = "&gt;"
  QU = "&quot;"
  AP = "&apos;"
  AM = "&amp;"
  TARGET_ENCODING = "ISO-8859-1"

  def filename(describe)
    normalized = describe.gsub(' ', '_')

    "TEST-#{normalized}.xml"
  end

  def encode_for_xml(str)
    encode_as_latin1(str).gsub("&", AM).gsub("<", LT).gsub(">", GT).
        gsub('"', QU).gsub("'", AP).
        gsub(/[#{Regexp.escape("\0\1\2\3\4\5\6\7\8")}]/, "?")
  end

  if defined? Encoding
    def encode_as_latin1(str)
      str.encode(TARGET_ENCODING, :undef => :replace, :invalid => :replace)
    end
  else
    require 'iconv'
    def encode_as_latin1(str)
      Iconv.conv("#{TARGET_ENCODING}//TRANSLIT//IGNORE", "UTF-8", str)
    end
  end

  begin
    Process.clock_gettime(Process::CLOCK_MONOTONIC)

    def current_time
      Process.clock_gettime(Process::CLOCK_MONOTONIC)
    end
  rescue Exception
    def current_time
      Time.now
    end
  end

  def mkdir_p(dir)
    sub_path = Dir.pwd
    parts = dir.split('/')

    parts.each do |part|
      sub_path = File.join(sub_path, part)

      puts "#{sub_path} exists?: #{Dir.exist?(sub_path)}"

      Dir.mkdir(sub_path) unless Dir.exist?(sub_path)
    end
  end
end

CUSTOM_MSPEC_FORMATTER = TruffleFormatter
