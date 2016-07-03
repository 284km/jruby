/*
 * Copyright (c) 2013, 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.core.symbol;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.object.DynamicObject;
import org.jcodings.specific.USASCIIEncoding;
import org.jruby.truffle.Layouts;
import org.jruby.truffle.RubyContext;
import org.jruby.truffle.core.rope.Rope;
import org.jruby.truffle.core.rope.RopeOperations;
import org.jruby.truffle.core.string.StringOperations;
import org.jruby.truffle.language.control.RaiseException;
import org.jruby.util.ByteList;
import org.jruby.util.IdUtil;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class SymbolTable {

    private final RubyContext context;

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    // Cache searches based on String
    private final Map<String, Reference<DynamicObject>> stringSymbolMap = new WeakHashMap<>();
    // Cache searches based on Rope
    private final Map<Rope, Reference<DynamicObject>> ropeSymbolMap = new WeakHashMap<>();
    // Weak set of Symbols, SymbolEquality implements equality based on inner rope, to be able to
    // deduplicate symbols
    private final Map<SymbolEquality, Reference<DynamicObject>> symbolSet = new WeakHashMap<>();

    public SymbolTable(RubyContext context) {
        this.context = context;
    }

    @TruffleBoundary
    public DynamicObject getSymbol(String stringKey) {
        lock.readLock().lock();
        DynamicObject symbol = null;
        try {
            final Reference<DynamicObject> reference = stringSymbolMap.get(stringKey);
            symbol = reference == null ? null : reference.get();
            if (symbol != null) {
                return symbol;
            }

        } finally {
            lock.readLock().unlock();
        }

        lock.writeLock().lock();
        try {
            final Reference<DynamicObject> reference1 = stringSymbolMap.get(stringKey);
            symbol = reference1 == null ? null : reference1.get();
            if (symbol != null) {
                return symbol;
            }

            final Rope rope = StringOperations.createRope(stringKey, USASCIIEncoding.INSTANCE);

            symbol = getDeduplicatedSymbol(rope);

            stringSymbolMap.put(stringKey, new WeakReference<DynamicObject>(symbol));
        } finally {
            lock.writeLock().unlock();
        }

        return symbol;
    }

    @TruffleBoundary
    public DynamicObject getSymbol(Rope ropeKey) {
        lock.readLock().lock();
        DynamicObject symbol = null;
        try {
            final Reference<DynamicObject> reference = ropeSymbolMap.get(ropeKey);
            symbol = reference == null ? null : reference.get();
            if (symbol != null) {
                return symbol;
            }

        } finally {
            lock.readLock().unlock();
        }

        lock.writeLock().lock();
        try {
            final Reference<DynamicObject> reference1 = ropeSymbolMap.get(ropeKey);
            symbol = reference1 == null ? null : reference1.get();
            if (symbol != null) {
                return symbol;
            }

            final Rope rope = RopeOperations.flatten(ropeKey);

            symbol = getDeduplicatedSymbol(rope);

            ropeSymbolMap.put(rope, new WeakReference<DynamicObject>(symbol));
        } finally {
            lock.writeLock().unlock();
        }

        return symbol;
    }

    private DynamicObject getDeduplicatedSymbol(Rope rope) {
        final DynamicObject newSymbol = createSymbol(rope);
        final SymbolEquality newKey = Layouts.SYMBOL.getEqualityWrapper(newSymbol);
        final Reference<DynamicObject> reference = symbolSet.get(newKey);
        final DynamicObject currentSymbol = reference == null ? null : reference.get();

        if (currentSymbol == null) {
            symbolSet.put(newKey, new WeakReference<DynamicObject>(newSymbol));
            return newSymbol;
        } else {
            return currentSymbol;
        }
    }

    private DynamicObject createSymbol(Rope rope) {
        final DynamicObject symbolClass = context.getCoreLibrary().getSymbolClass();
        final String string = ByteList.decode(
                rope.getBytes(),
                0,
                rope.byteLength(),
                "ISO-8859-1");
        // Symbol has to have reference to its SymbolEquality otherwise it would be GCed.
        final SymbolEquality equalityWrapper = new SymbolEquality();
        final DynamicObject symbol = Layouts.SYMBOL.createSymbol(
                Layouts.CLASS.getInstanceFactory(symbolClass),
                string,
                rope,
                string.hashCode(),
                equalityWrapper);

        equalityWrapper.setSymbol(symbol);
        return symbol;
    }

    @TruffleBoundary
    public Collection<DynamicObject> allSymbols() {
        final Collection<Reference<DynamicObject>> symbolReferences;

        lock.readLock().lock();

        try {
            symbolReferences = symbolSet.values();
        } finally {
            lock.readLock().unlock();
        }

        final Collection<DynamicObject> symbols = new ArrayList<>(symbolReferences.size());

        for (Reference<DynamicObject> reference : symbolReferences) {
            final DynamicObject symbol = reference.get();

            if (symbol != null) {
                symbols.add(symbol);
            }
        }

        return symbols;
    }

    // TODO (eregon, 10/10/2015): this check could be done when a Symbol is created to be much cheaper
    @TruffleBoundary(throwsControlFlowException = true)
    public static String checkInstanceVariableName(
            RubyContext context,
            String name,
            Node currentNode) {
        // if (!IdUtil.isValidInstanceVariableName(name)) {

        // check like Rubinius does for compatibility with their Struct Ruby implementation.
        if (!(name.startsWith("@") && name.length() > 1 && IdUtil.isInitialCharacter(name.charAt(1)))) {
            throw new RaiseException(context.getCoreExceptions().nameErrorInstanceNameNotAllowable(
                    name,
                    currentNode));
        }
        return name;
    }

    @TruffleBoundary(throwsControlFlowException = true)
    public static String checkClassVariableName(
            RubyContext context,
            String name,
            Node currentNode) {
        if (!IdUtil.isValidClassVariableName(name)) {
            throw new RaiseException(context.getCoreExceptions().nameErrorInstanceNameNotAllowable(
                    name,
                    currentNode));
        }
        return name;
    }

}
