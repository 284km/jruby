require 'java'

import org.jruby.compiler.impl.SkinnyMethodAdapter

begin
  import org.objectweb.asm.MethodVisitor
  import org.objectweb.asm.Opcodes
rescue # jarjar renames things, so we try the renamed version
  import "org.jruby.org.objectweb.asm.MethodVisitor"
  import "org.jruby.org.objectweb.asm.Opcodes"
end

describe "SkinnyMethodAdapter" do
  it "supports all JVM opcodes" do
    insn_opcodes = Opcodes.constants.select do |c|
      case c.to_s # for 1.9's symbols

      when /ACC_/, # access modifiers
           /V1_/, # version identifiers
           /T_/, # type identifiers
           /F_/, # framing hints
           /H_/, # method handles
           /ASM/ # ASM version stuff
        false

      when "DOUBLE", "FLOAT", "INTEGER", "LONG", "NULL", "TOP", "UNINITIALIZED_THIS"
        false

      when "GOTO", "RETURN", "INSTANCEOF", "NEW"
        false

      when "INVOKEDYNAMIC_OWNER"
        false

      else
        true
      end
    end

    # 1.9 makes them symbols, so to_s the lot
    insn_opcodes = insn_opcodes.map(&:to_s)
    instance_methods = SkinnyMethodAdapter.instance_methods.map(&:to_s)

    insn_opcodes.each do |opcode|
      opcode = opcode.downcase
      expect(instance_methods).to include(opcode)
    end

    expect(instance_methods).to include(
      "go_to",
      "voidreturn",
      "instance_of",
      "newobj"
    )
  end
end
