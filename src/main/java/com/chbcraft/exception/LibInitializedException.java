package com.chbcraft.exception;

import com.chbcraft.internals.components.MessageBox;

public class LibInitializedException extends RuntimeException{

    public LibInitializedException(String message) {
        super(message);
    }

    @Override
    public void printStackTrace() {
        MessageBox.getLogger().error(getMessage());
    }
}
