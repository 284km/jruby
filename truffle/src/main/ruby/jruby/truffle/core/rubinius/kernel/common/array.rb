# Copyright (c) 2007-2014, Evan Phoenix and contributors
# All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are met:
#
# * Redistributions of source code must retain the above copyright notice, this
#   list of conditions and the following disclaimer.
# * Redistributions in binary form must reproduce the above copyright notice
#   this list of conditions and the following disclaimer in the documentation
#   and/or other materials provided with the distribution.
# * Neither the name of Rubinius nor the names of its contributors
#   may be used to endorse or promote products derived from this software
#   without specific prior written permission.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
# AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
# IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
# DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
# FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
# DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
# SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
# CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
# OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
# OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

# Only part of Rubinius' array.rb

# Rubinius uses the instance variable @total to store the size. We replace this
# in the translator with a call to size.

class Array

  def self.[](*args)
    ary = allocate
    ary.replace args
    ary
  end

  # Modified implementation until we support Rubinius::IdentityMap.
  def &(other)
    other = Rubinius::Type.coerce_to other, Array, :to_ary

    array = []

    each { |x| array << x if other.include? x }

    array
  end


  def values_at(*args)
    out = []

    args.each do |elem|
      # Cannot use #[] because of subtly different errors
      if elem.kind_of? Range
        finish = Rubinius::Type.coerce_to_collection_index elem.last
        start = Rubinius::Type.coerce_to_collection_index elem.first

        start += @total if start < 0
        next if start < 0

        finish += @total if finish < 0
        finish -= 1 if elem.exclude_end?

        next if finish < start

        start.upto(finish) { |i| out << at(i) }

      else
        i = Rubinius::Type.coerce_to_collection_index elem
        out << at(i)
      end
    end

    out
  end

  def first(n = undefined)
    return at(0) if undefined.equal?(n)

    n = Rubinius::Type.coerce_to_collection_index n
    raise ArgumentError, "Size must be positive" if n < 0

    Array.new self[0, n]
  end

  def hash
    hash_val = size
    mask = Fixnum::MAX >> 1

    # This is duplicated and manually inlined code from Thread for performance
    # reasons. Before refactoring it, please benchmark it and compare your
    # refactoring against the original.

    id = object_id
    objects = Thread.current.recursive_objects

    # If there is already an our version running...
    if objects.key? :__detect_outermost_recursion__

      # If we've seen self, unwind back to the outer version
      if objects.key? id
        raise Thread::InnerRecursionDetected
      end

      # .. or compute the hash value like normal
      begin
        objects[id] = true

        each { |x| hash_val = ((hash_val & mask) << 1) ^ x.hash }
      ensure
        objects.delete id
      end

      return hash_val
    else
      # Otherwise, we're the outermost version of this code..
      begin
        objects[:__detect_outermost_recursion__] = true
        objects[id] = true

        each { |x| hash_val = ((hash_val & mask) << 1) ^ x.hash }

        # An inner version will raise to return back here, indicating that
        # the whole structure is recursive. In which case, abondon most of
        # the work and return a simple hash value.
      rescue Thread::InnerRecursionDetected
        return size
      ensure
        objects.delete :__detect_outermost_recursion__
        objects.delete id
      end
    end

    return hash_val
  end

  def last(n=undefined)
    if undefined.equal?(n)
      return at(-1)
    elsif size < 1
      return []
    end

    n = Rubinius::Type.coerce_to_collection_index n
    return [] if n == 0

    raise ArgumentError, "count must be positive" if n < 0

    n = size if n > size
    Array.new self[-n..-1]
  end

  def permutation(num=undefined, &block)
    return to_enum(:permutation, num) unless block_given?

    if undefined.equal? num
      num = @total
    else
      num = Rubinius::Type.coerce_to_collection_index num
    end

    if num < 0 || @total < num
      # no permutations, yield nothing
    elsif num == 0
      # exactly one permutation: the zero-length array
      yield []
    elsif num == 1
      # this is a special, easy case
      each { |val| yield [val] }
    else
      # this is the general case
      perm = Array.new(num)
      used = Array.new(@total, false)

      if block
        # offensive (both definitions) copy.
        offensive = dup
        Rubinius.privately do
          offensive.__permute__(num, perm, 0, used, &block)
        end
      else
        __permute__(num, perm, 0, used, &block)
      end
    end

    self
  end

  def __permute__(num, perm, index, used, &block)
    # Recursively compute permutations of r elements of the set [0..n-1].
    # When we have a complete permutation of array indexes, copy the values
    # at those indexes into a new array and yield that array.
    #
    # num: the number of elements in each permutation
    # perm: the array (of size num) that we're filling in
    # index: what index we're filling in now
    # used: an array of booleans: whether a given index is already used
    #
    # Note: not as efficient as could be for big num.
    @total.times do |i|
      unless used[i]
        perm[index] = i
        if index < num-1
          used[i] = true
          __permute__(num, perm, index+1, used, &block)
          used[i] = false
        else
          yield values_at(*perm)
        end
      end
    end
  end
  private :__permute__

  def <=>(other)
    other = Rubinius::Type.check_convert_type other, Array, :to_ary
    return 0 if equal? other
    return nil if other.nil?

    total = Rubinius::Mirror::Array.reflect(other).total

    Thread.detect_recursion self, other do
      i = 0
      count = total < @total ? total : @total

      while i < count
        order = self[i] <=> other[i]
        return order unless order == 0

        i += 1
      end
    end

    # subtle: if we are recursing on that pair, then let's
    # no go any further down into that pair;
    # any difference will be found elsewhere if need be
    @total <=> total
  end

  def -(other)
    other = Rubinius::Type.coerce_to other, Array, :to_ary

    array = []
    im = Rubinius::IdentityMap.from other

    each { |x| array << x unless im.include? x }

    array
  end

end
