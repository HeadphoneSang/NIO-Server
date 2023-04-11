package com.chbcraft.exception;

import com.chbcraft.internals.components.MessageBox;

public class ClassSameNameException extends RuntimeException{
    public ClassSameNameException(String message) {
        super(message);
    }

    @Override
    public void printStackTrace() {
        MessageBox logger = MessageBox.getLogger();
        logger.error(getMessage()+"类名重复!");
        super.printStackTrace();
    }
}
