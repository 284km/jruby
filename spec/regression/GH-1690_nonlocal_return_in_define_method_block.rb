# https://github.com/jruby/jruby/issues/1690

def yielder; yield; end

class C
  define_method(:foo) { yielder { return :foo; } }
end

describe 'NonLocalReturn' do
  it 'inside a block of a define_method block body returns normally' do
    C.new.foo.should_be :foo
  end
end
