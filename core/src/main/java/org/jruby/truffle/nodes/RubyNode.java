/*
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.nodes;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.source.SourceSection;
import com.oracle.truffle.api.dsl.TypeSystemReference;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.UnexpectedResultException;
import org.jruby.truffle.nodes.dispatch.Dispatch;
import org.jruby.truffle.nodes.yield.YieldDispatchNode;
import org.jruby.truffle.runtime.NilPlaceholder;
import org.jruby.truffle.runtime.RubyCallStack;
import org.jruby.truffle.runtime.RubyContext;
import org.jruby.truffle.runtime.UndefinedPlaceholder;
import org.jruby.truffle.runtime.backtrace.Backtrace;
import org.jruby.truffle.runtime.core.*;
import org.jruby.truffle.runtime.core.RubyArray;
import org.jruby.truffle.runtime.core.RubyHash;
import org.jruby.truffle.runtime.core.RubyRange;
import org.jruby.truffle.runtime.core.RubyBasicObject;

import java.math.BigInteger;

/**
 * Base class for most nodes in Ruby.
 *
 * @see YieldDispatchNode
 */
@TypeSystemReference(RubyTypes.class)
public abstract class RubyNode extends Node {

    private final RubyContext context;

    public RubyNode(RubyContext context, SourceSection sourceSection) {
        super(sourceSection);
        assert context != null;
        this.context = context;
    }

    public RubyNode(RubyNode prev) {
        this(prev.context, prev.getSourceSection());
    }

    public abstract Object execute(VirtualFrame frame);

    /**
     * Ruby's parallel semantic path.
     * 
     * @see DefinedNode
     */
    public Object isDefined(@SuppressWarnings("unused") VirtualFrame frame) {
        throw new UnsupportedOperationException("no definition for " + getClass().getName());
    }

    public RubyArray executeArray(VirtualFrame frame) throws UnexpectedResultException {
        return RubyTypesGen.RUBYTYPES.expectRubyArray(execute(frame));
    }

    public BigInteger executeBignum(VirtualFrame frame) throws UnexpectedResultException {
        return RubyTypesGen.RUBYTYPES.expectBigInteger(execute(frame));
    }

    public boolean executeBoolean(VirtualFrame frame) throws UnexpectedResultException {
        return RubyTypesGen.RUBYTYPES.expectBoolean(execute(frame));
    }

    public int executeIntegerFixnum(VirtualFrame frame) throws UnexpectedResultException {
        return RubyTypesGen.RUBYTYPES.expectInteger(execute(frame));
    }

    public long executeLongFixnum(VirtualFrame frame) throws UnexpectedResultException {
        return RubyTypesGen.RUBYTYPES.expectLong(execute(frame));
    }

    public RubyRange.IntegerFixnumRange executeIntegerFixnumRange(VirtualFrame frame) throws UnexpectedResultException {
        return RubyTypesGen.RUBYTYPES.expectIntegerFixnumRange(execute(frame));
    }

    public RubyRange.LongFixnumRange executeLongFixnumRange(VirtualFrame frame) throws UnexpectedResultException {
        return RubyTypesGen.RUBYTYPES.expectLongFixnumRange(execute(frame));
    }

    public double executeFloat(VirtualFrame frame) throws UnexpectedResultException {
        return RubyTypesGen.RUBYTYPES.expectDouble(execute(frame));
    }

    public NilPlaceholder executeNilPlaceholder(VirtualFrame frame) throws UnexpectedResultException {
        return RubyTypesGen.RUBYTYPES.expectNilPlaceholder(execute(frame));
    }

    public Object[] executeObjectArray(VirtualFrame frame) throws UnexpectedResultException {
        return RubyTypesGen.RUBYTYPES.expectObjectArray(execute(frame));
    }

    public RubyRange.ObjectRange executeObjectRange(VirtualFrame frame) throws UnexpectedResultException {
        return RubyTypesGen.RUBYTYPES.expectObjectRange(execute(frame));
    }

    public RubyBasicObject executeRubyBasicObject(VirtualFrame frame) throws UnexpectedResultException {
        return RubyTypesGen.RUBYTYPES.expectRubyBasicObject(execute(frame));
    }

    public RubyBinding executeRubyBinding(VirtualFrame frame) throws UnexpectedResultException {
        return RubyTypesGen.RUBYTYPES.expectRubyBinding(execute(frame));
    }

    public RubyClass executeRubyClass(VirtualFrame frame) throws UnexpectedResultException {
        return RubyTypesGen.RUBYTYPES.expectRubyClass(execute(frame));
    }

    public RubyContinuation executeRubyContinuation(VirtualFrame frame) throws UnexpectedResultException {
        return RubyTypesGen.RUBYTYPES.expectRubyContinuation(execute(frame));
    }

    public RubyException executeRubyException(VirtualFrame frame) throws UnexpectedResultException {
        return RubyTypesGen.RUBYTYPES.expectRubyException(execute(frame));
    }

    public RubyFiber executeRubyFiber(VirtualFrame frame) throws UnexpectedResultException {
        return RubyTypesGen.RUBYTYPES.expectRubyFiber(execute(frame));
    }

    public RubyFile executeRubyFile(VirtualFrame frame) throws UnexpectedResultException {
        return RubyTypesGen.RUBYTYPES.expectRubyFile(execute(frame));
    }

    public RubyHash executeRubyHash(VirtualFrame frame) throws UnexpectedResultException {
        return RubyTypesGen.RUBYTYPES.expectRubyHash(execute(frame));
    }

    public RubyMatchData executeRubyMatchData(VirtualFrame frame) throws UnexpectedResultException {
        return RubyTypesGen.RUBYTYPES.expectRubyMatchData(execute(frame));
    }

    public RubyModule executeRubyModule(VirtualFrame frame) throws UnexpectedResultException {
        return RubyTypesGen.RUBYTYPES.expectRubyModule(execute(frame));
    }

    public RubyNilClass executeRubyNilClass(VirtualFrame frame) throws UnexpectedResultException {
        return RubyTypesGen.RUBYTYPES.expectRubyNilClass(execute(frame));
    }

    public RubyObject executeRubyObject(VirtualFrame frame) throws UnexpectedResultException {
        return RubyTypesGen.RUBYTYPES.expectRubyObject(execute(frame));
    }

    public RubyProc executeRubyProc(VirtualFrame frame) throws UnexpectedResultException {
        return RubyTypesGen.RUBYTYPES.expectRubyProc(execute(frame));
    }

    public RubyRange executeRubyRange(VirtualFrame frame) throws UnexpectedResultException {
        return RubyTypesGen.RUBYTYPES.expectRubyRange(execute(frame));
    }

    public RubyRegexp executeRubyRegexp(VirtualFrame frame) throws UnexpectedResultException {
        return RubyTypesGen.RUBYTYPES.expectRubyRegexp(execute(frame));
    }

    public RubySymbol executeRubySymbol(VirtualFrame frame) throws UnexpectedResultException {
        return RubyTypesGen.RUBYTYPES.expectRubySymbol(execute(frame));
    }

    public RubyThread executeRubyThread(VirtualFrame frame) throws UnexpectedResultException {
        return RubyTypesGen.RUBYTYPES.expectRubyThread(execute(frame));
    }

    public RubyTime executeRubyTime(VirtualFrame frame) throws UnexpectedResultException {
        return RubyTypesGen.RUBYTYPES.expectRubyTime(execute(frame));
    }

    public RubyString executeString(VirtualFrame frame) throws UnexpectedResultException {
        return RubyTypesGen.RUBYTYPES.expectRubyString(execute(frame));
    }
    public RubyEncoding executeRubyEncoding(VirtualFrame frame) throws UnexpectedResultException {
        return RubyTypesGen.RUBYTYPES.expectRubyEncoding(execute(frame));
    }

    public UndefinedPlaceholder executeUndefinedPlaceholder(VirtualFrame frame) throws UnexpectedResultException {
        return RubyTypesGen.RUBYTYPES.expectUndefinedPlaceholder(execute(frame));
    }

    public Dispatch.DispatchAction executeDispatchAction(VirtualFrame frame) {
        throw new UnsupportedOperationException();
    }

    public void executeVoid(VirtualFrame frame) {
        execute(frame);
    }

    public RubyNode getNonProxyNode() {
        return this;
    }

    public RubyContext getContext() {
        return context;
    }

    public static void notDesignedForCompilation() {
        CompilerAsserts.neverPartOfCompilation();
    }

    private static void panic(RubyContext context, RubyNode currentNode, String message) {
        CompilerDirectives.transferToInterpreter();

        System.err.println("PANIC");

        if (message != null) {
            System.err.println(message);
        }

        for (String line : Backtrace.PANIC_FORMATTER.format(context, null, RubyCallStack.getBacktrace(currentNode))) {
            System.err.println(line);
        }

        new Exception().printStackTrace();

        System.exit(1);
    }

    public void panic(String format, Object... args) {
        panic(String.format(format, args));
    }

    public void panic(String message) {
        panic(getContext(), this, message);
    }

    public void panic() {
        panic(getContext(), this, null);
    }

    public static void panic(RubyContext context) {
        panic(context, null, null);
    }

}
