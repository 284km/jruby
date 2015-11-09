# encoding: UTF-8
require 'test/unit'

class TestString < Test::Unit::TestCase

  # JRUBY-4987
  def test_paragraph
    # raises ArrayIndexOutOfBoundsException in 1.5.1
    assert_equal ["foo\n"], "foo\n".lines('').to_a
  end

  # Test fix for JRUBY-1215
  def test_invalid_float_from_string
    for string in [
      "1a",
      "a1",
      "1.0a",
      "10a",
      "10.1a",
      "0.10a",
      "1.1e1a",
      "1.1e10a",
      "\3 1",
      "1 \3",
    ]
      assert_raises(ArgumentError) { Float(string) }
    end
  end

  # Test fix for JRUBY-1215-related (unreported) bug
  def test_invalid_integer_from_string
    for string in [
      "a1",
      "1a",
      "10a",
      "\3 1",
      "1 \3",
    ]
      assert_raises(ArgumentError) { Integer(string) }
    end
  end

  EOL = "\r\n"

  def test_sub_utf8
    do_sub "a" + EOL + EOL + "a", 6, 3, 1  # 1byte + 2byte + 2byte + 1byte
    do_sub "a" + EOL + EOL + "あ", 6, 3, 1
    do_sub "あ" + EOL + EOL + "a", 6, 3, 1
    do_sub "あ" + EOL + EOL + "あ", 6, 3, 1
  end

  private

  def do_sub buf, e1, e2, e3
    assert_equal e1, buf.size

    head = ''

    #from cgi.rb..
    buf = buf.sub(/\A((?:.|\n)*?#{EOL})#{EOL}/n) do
      head = $1.dup
      ""
    end
    # ..cgi.rb

    assert_equal e2,  head.size
    assert_equal e3,  buf.size
  end

end
