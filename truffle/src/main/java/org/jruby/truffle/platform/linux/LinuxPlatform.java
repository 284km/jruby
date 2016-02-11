/*
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.platform.linux;

import jnr.posix.POSIX;
import jnr.posix.POSIXFactory;
import org.jruby.truffle.RubyContext;
import org.jruby.truffle.platform.ProcessName;
import org.jruby.truffle.platform.NativePlatform;
import org.jruby.truffle.platform.TrufflePOSIXHandler;
import org.jruby.truffle.platform.java.JavaProcessName;
import org.jruby.truffle.platform.signal.SignalManager;
import org.jruby.truffle.platform.sunmisc.SunMiscSignalManager;

public class LinuxPlatform implements NativePlatform {

    private final POSIX posix;
    private final SignalManager signalManager;
    private final ProcessName processName;

    public LinuxPlatform(RubyContext context) {
        posix = POSIXFactory.getNativePOSIX(new TrufflePOSIXHandler(context));
        signalManager = new SunMiscSignalManager();
        processName = new JavaProcessName();
    }

    @Override
    public POSIX getPosix() {
        return posix;
    }

    @Override
    public SignalManager getSignalManager() {
        return signalManager;
    }

    @Override
    public ProcessName getProcessName() {
        return processName;
    }

}
