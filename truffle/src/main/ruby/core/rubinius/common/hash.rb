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

# Only part of Rubinius' hash.rb

class Hash

  alias_method :store, :[]=

  # Used internally to get around subclasses redefining #[]=
  alias_method :__store__, :[]=

  def merge!(other)
    Rubinius.check_frozen

    other = Rubinius::Type.coerce_to other, Hash, :to_hash

    if block_given?
      other.each_item do |item|
        key = item.key
        if key? key
          __store__ key, yield(key, self[key], item.value)
        else
          __store__ key, item.value
        end
      end
    else
      other.each_item do |item|
        __store__ item.key, item.value
      end
    end
    self
  end

  alias_method :update, :merge!

  def each_key
    return to_enum(:each_key) unless block_given?

    each_item { |item| yield item.key }
    self
  end

  def each_value
    return to_enum(:each_value) unless block_given?

    each_item { |item| yield item.value }
    self
  end

  def keys
    ary = []
    each_item do |item|
      ary << item.key
    end
    ary
  end

  def values
    ary = []

    each_item do |item|
      ary << item.value
    end

    ary
  end

  def invert
    inverted = {}
    each_item do |item|
      inverted[item.value] = item.key
    end
    inverted
  end

  def to_a
    ary = []

    each_item do |item|
      ary << [item.key, item.value]
    end

    Rubinius::Type.infect ary, self
    ary
  end

end
