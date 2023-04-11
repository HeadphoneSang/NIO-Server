package com.chbcraft.exception;

import com.chbcraft.internals.components.MessageBox;

public class DuplicatePluginMainException extends RuntimeException{
    private String pluginName;
    @Override
    public void printStackTrace() {
        super.printStackTrace();
        MessageBox.getLogger().error("Reason on: ["+pluginName+"] Can Not be new twice!");
    }

    public DuplicatePluginMainException(String pluginName) {
        super();
        this.pluginName = pluginName;
    }
}
