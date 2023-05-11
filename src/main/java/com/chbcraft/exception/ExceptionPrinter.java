package com.chbcraft.exception;
import java.io.OutputStream;
import java.io.PrintStream;

public class ExceptionPrinter extends PrintStream {
    public ExceptionPrinter(OutputStream out) {
        super(out);
    }

    @Override
    public void print(String s) {
        super.print(s);
    }
}
