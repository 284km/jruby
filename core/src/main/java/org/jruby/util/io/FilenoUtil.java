package org.jruby.util.io;

import jnr.unixsocket.UnixServerSocketChannel;
import jnr.unixsocket.UnixSocketChannel;

import java.io.FileDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.channels.Channel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Utilities for working with native fileno and Java structures that wrap them.
 */
public class FilenoUtil {
    public static FileDescriptor getDescriptorFromChannel(Channel channel) {
        if (SEL_CH_IMPL_GET_FD != null && SEL_CH_IMPL.isInstance(channel)) {
            // Pipe Source and Sink, Sockets, and other several other selectable channels
            try {
                return (FileDescriptor)SEL_CH_IMPL_GET_FD.invoke(channel);
            } catch (Exception e) {
                // return bogus below
            }
        } else if (FILE_CHANNEL_IMPL_FD != null && FILE_CHANNEL_IMPL.isInstance(channel)) {
            // FileChannels
            try {
                return (FileDescriptor)FILE_CHANNEL_IMPL_FD.get(channel);
            } catch (Exception e) {
                // return bogus below
            }
        } else if (FILE_DESCRIPTOR_FD != null) {
            FileDescriptor unixFD = new FileDescriptor();

            // UNIX sockets, from jnr-unixsocket
            try {
                if (channel instanceof UnixSocketChannel) {
                    FILE_DESCRIPTOR_FD.set(unixFD, ((UnixSocketChannel)channel).getFD());
                    return unixFD;
                } else if (channel instanceof UnixServerSocketChannel) {
                    FILE_DESCRIPTOR_FD.set(unixFD, ((UnixServerSocketChannel)channel).getFD());
                    return unixFD;
                }
            } catch (Exception e) {
                // return bogus below
            }
        }
        return new FileDescriptor();
    }

    public static ChannelFD getWrapperFromFileno(int fileno) {
        return filenoMap.get(fileno);
    }

    public static void registerWrapper(int fileno, ChannelFD wrapper) {
        if (fileno == -1) return;
        filenoMap.put(fileno, wrapper);
    }

    public static void unregisterWrapper(int fileno) {
        if (fileno == -1) return;
        filenoMap.remove(fileno);
    }

    public static int getNewFileno() {
        return internalFilenoIndex.getAndIncrement();
    }

    public static boolean isFake(int fileno) {
        return fileno >= FIRST_FAKE_FD;
    }

    public static int getFilenoFromChannel(Channel channel) {
        if (FILE_DESCRIPTOR_FD != null) {
            FileDescriptor fd = getDescriptorFromChannel(channel);
            if (fd.valid()) {
                try {
                    return (Integer)FILE_DESCRIPTOR_FD.get(fd);
                } catch (Exception e) {
                    // failed to get
                }
            }
        }
        return -1;
    }

    static {
        Method getFD;
        Class selChImpl;
        try {
            selChImpl = Class.forName("sun.nio.ch.SelChImpl");
            try {
                getFD = selChImpl.getMethod("getFD");
                getFD.setAccessible(true);
            } catch (Exception e) {
                getFD = null;
            }
        } catch (Exception e) {
            selChImpl = null;
            getFD = null;
        }
        SEL_CH_IMPL = selChImpl;
        SEL_CH_IMPL_GET_FD = getFD;

        Field fd;
        Class fileChannelImpl;
        try {
            fileChannelImpl = Class.forName("sun.nio.ch.FileChannelImpl");
            try {
                fd = fileChannelImpl.getDeclaredField("fd");
                fd.setAccessible(true);
            } catch (Exception e) {
                fd = null;
            }
        } catch (Exception e) {
            fileChannelImpl = null;
            fd = null;
        }
        FILE_CHANNEL_IMPL = fileChannelImpl;
        FILE_CHANNEL_IMPL_FD = fd;

        Field ffd;
        try {
            ffd = FileDescriptor.class.getDeclaredField("fd");
            ffd.setAccessible(true);
        } catch (Exception e) {
            ffd = null;
        }
        FILE_DESCRIPTOR_FD = ffd;
    }

    @Deprecated
    public static ChannelDescriptor getDescriptorByFileno(int aFileno) {
        return filenoDescriptorMap.get(aFileno);
    }

    @Deprecated
    public static void registerDescriptor(ChannelDescriptor descriptor) {
        filenoDescriptorMap.put(descriptor.getFileno(), descriptor);
    }

    @Deprecated
    public static void unregisterDescriptor(int aFileno) {
        filenoDescriptorMap.remove(aFileno);
    }

    // FIXME shouldn't use static; may interfere with other runtimes in the same JVM
    public static int FIRST_FAKE_FD = 100000;
    protected static final AtomicInteger internalFilenoIndex = new AtomicInteger(FIRST_FAKE_FD);
    @Deprecated
    private static final Map<Integer, ChannelDescriptor> filenoDescriptorMap = new ConcurrentHashMap<Integer, ChannelDescriptor>();
    private static final Map<Integer, ChannelFD> filenoMap = new ConcurrentHashMap<Integer, ChannelFD>();

    private static final Class SEL_CH_IMPL;
    private static final Method SEL_CH_IMPL_GET_FD;
    private static final Class FILE_CHANNEL_IMPL;
    private static final Field FILE_CHANNEL_IMPL_FD;
    private static final Field FILE_DESCRIPTOR_FD;
}
