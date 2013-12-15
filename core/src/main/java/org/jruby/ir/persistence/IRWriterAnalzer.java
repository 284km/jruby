/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jruby.ir.persistence;

import java.util.HashMap;
import java.util.Map;
import org.jruby.ir.IRScope;
import org.jruby.ir.instructions.Instr;
import org.jruby.ir.operands.Operand;

/**
 *
 * @author enebo
 */
public class IRWriterAnalzer implements IRWriterEncoder {
    // Figure out most commonly used operands for eventual creation of an operand pool
    private final Map<Operand, Integer> operandCounts = new HashMap<Operand, Integer>();

    @Override
    public void encode(Instr instr) {
        for (Operand operand: instr.getOperands()) {
            increment(operand);
        }
    }
    
    @Override
    public void encode(String value) {
    }

    @Override
    public void encode(String[] values) {
    }    

    @Override
    public void encode(IRPersistableEnum value) {
    }

    @Override
    public void encode(boolean value) {
    }

    @Override
    public void encode(int value) {
    }

    @Override
    public void encode(long value) {
    }

    @Override
    public void commit() {
    }

    @Override
    public void startEncodingScopeHeader(IRScope scope) {
    }

    @Override
    public void endEncodingScopeHeader(IRScope scope) {
    }

    @Override
    public void startEncodingScopeInstrs(IRScope scope) {
    }

    @Override
    public void endEncodingScopeInstrs(IRScope scope) {
    }

    @Override
    public void startEncodingScopeHeaders(IRScope script) {
    }

    @Override
    public void endEncodingScopeHeaders(IRScope script) {
    }

    private void increment(Operand operand) {
        Integer count = operandCounts.get(operand);
        if (count == null) count = new Integer(0);
        
        operandCounts.put(operand, count + 1);
    }
}
