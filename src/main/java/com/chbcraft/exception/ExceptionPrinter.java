package com.chbcraft.exception;
import org.fusesource.jansi.Ansi;
import org.jetbrains.annotations.Nullable;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.fusesource.jansi.Ansi.ansi;

public class ExceptionPrinter extends PrintStream {

    public ExceptionPrinter(OutputStream out) {
        super(out);
    }

    private static final List<String> filters = new ArrayList<>();

    static{
//        filters.add("java.io.IOException: Connection reset by peer");
//        filters.add("at sun.nio.ch.FileDispatcherImpl.read0(Native Method)");
//        filters.add("at sun.nio.ch.SocketDispatcher.read(SocketDispatcher.java:39)");
//        filters.add("at sun.nio.ch.IOUtil.read(IOUtil.java:192)");
//        filters.add("at sun.nio.ch.SocketChannelImpl.read(SocketChannelImpl.java:378)");
//        filters.add("at io.netty.buffer.PooledUnsafeDirectByteBuf.setBytes(PooledUnsafeDirectByteBuf.java:288)");
//        filters.add("at io.netty.buffer.AbstractByteBuf.writeBytes(AbstractByteBuf.java:1100)");

    }

    @Override
    public void println(@Nullable Object x) {
        String s = String.valueOf(x);
        for (String filter : filters) {
            if(s.contains(filter))
                return;
        }
        super.println(ansi().fg(Ansi.Color.RED).a(s));
    }


    @Override
    public void print(String s) {
        super.print(ansi().fg(Ansi.Color.RED).a(s));
    }

    public static void addFilter(String filter){
        filters.add(filter);
    }
}
