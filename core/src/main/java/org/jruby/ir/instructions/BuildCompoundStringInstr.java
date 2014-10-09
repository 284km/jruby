package org.jruby.ir.instructions;

import org.jcodings.Encoding;
import org.jruby.RubyString;
import org.jruby.ir.IRVisitor;
import org.jruby.ir.Operation;
import org.jruby.ir.operands.Operand;
import org.jruby.ir.operands.StringLiteral;
import org.jruby.ir.operands.Variable;
import org.jruby.ir.transformations.inlining.CloneInfo;
import org.jruby.parser.StaticScope;
import org.jruby.runtime.DynamicScope;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;
import org.jruby.util.ByteList;
import org.jruby.util.StringSupport;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// This represents a compound string in Ruby
// Ex: - "Hi " + "there"
//     - "Hi #{name}"
public class BuildCompoundStringInstr extends Instr implements ResultInstr {
    Variable result;
    private List<Operand> pieces;
    final private Encoding encoding;

    public BuildCompoundStringInstr(Variable result, List<Operand> pieces, Encoding encoding) {
        super(Operation.BUILD_COMPOUND_STRING);
        this.pieces = pieces;
        this.encoding = encoding;
        this.result = result;
    }

    public List<Operand> getPieces() {
       return pieces;
    }

    public Encoding getEncoding() {
        return encoding;
    }

    @Override
    public Variable getResult() {
        return result;
    }

    @Override
    public void updateResult(Variable v) {
        this.result = v;
    }

    @Override
    public Operand[] getOperands() {
        return pieces.toArray(new Operand[pieces.size()]);
    }

    @Override
    public void simplifyOperands(Map<Operand, Operand> valueMap, boolean force) {
        List<Operand> newPieces = new ArrayList<Operand>();
        for (Operand p : pieces) {
            newPieces.add(p.getSimplifiedOperand(valueMap, force));
        }

       pieces = newPieces;
    }

    @Override
    public Instr clone(CloneInfo ii) {
        List<Operand> newPieces = new ArrayList<Operand>();
        for (Operand p : pieces) {
            newPieces.add(p.cloneForInlining(ii));
        }

        return new BuildCompoundStringInstr(ii.getRenamedVariable(result), newPieces, encoding);
    }

    // SSS FIXME: Buggy?
    String retrieveJavaString(ThreadContext context, IRubyObject self, StaticScope currScope, DynamicScope currDynScope, Object[] temp) {
        StringBuilder buf = new StringBuilder();

        for (Operand p : pieces) {
            buf.append(p.retrieve(context, self, currScope, currDynScope, temp));
        }

        return buf.toString();
    }

    public boolean isSameEncoding(StringLiteral str) {
        return str.bytelist.getEncoding() == encoding;
    }

    public RubyString[] retrievePieces(ThreadContext context, IRubyObject self, StaticScope currScope, DynamicScope currDynScope, Object[] temp) {
        RubyString[] strings = new RubyString[pieces.size()];
        int i = 0;
        for (Operand p : pieces) {
            strings[i++] = (RubyString)p.retrieve(context, self, currScope, currDynScope, temp);
        }
        return strings;
    }

    @Override
    public Object interpret(ThreadContext context, StaticScope currScope, DynamicScope currDynScope, IRubyObject self, Object[] temp) {
        // SSS FIXME: Doesn't work in all cases.  See example below
        //
        //    s = "x\234\355\301\001\001\000\000\000\200\220\376\257\356\b\n#{"\000" * 31}\030\200\000\000\001"
        //    s.length prints 70 instead of 52
        //
        // return context.getRuntime().newString(retrieveJavaString(interp, context, self));

        ByteList bytes = new ByteList();
        bytes.setEncoding(encoding);
        RubyString str = RubyString.newStringShared(context.runtime, bytes, StringSupport.CR_7BIT);
        for (Operand p : pieces) {
            if ((p instanceof StringLiteral) && (isSameEncoding((StringLiteral)p))) {
                str.getByteList().append(((StringLiteral)p).bytelist);
            } else {
               IRubyObject pval = (IRubyObject)p.retrieve(context, self, currScope, currDynScope, temp);
               str.append19(pval);
            }
        }

        return str;
    }

    @Override
    public void visit(IRVisitor visitor) {
        visitor.BuildCompoundStringInstr(this);
    }

    @Override
    public String toString() {
        return super.toString() + (encoding == null? "" : encoding) + (pieces == null ? "[]" : java.util.Arrays.toString(pieces.toArray()));
    }
}
