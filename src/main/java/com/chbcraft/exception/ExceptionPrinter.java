package com.chbcraft.exception;
import org.fusesource.jansi.Ansi;

import java.io.OutputStream;
import java.io.PrintStream;

import static org.fusesource.jansi.Ansi.ansi;

public class ExceptionPrinter extends PrintStream {
    public ExceptionPrinter(OutputStream out) {
        super(out);
    }

    @Override
    public void print(String s) {
        super.print(ansi().fgRed().a(s).fg(Ansi.Color.DEFAULT));
    }
}
