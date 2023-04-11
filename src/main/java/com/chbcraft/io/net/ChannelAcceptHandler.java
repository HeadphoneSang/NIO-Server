package com.chbcraft.io.net;

import com.chbcraft.internals.components.MessageBox;
import com.chbcraft.io.event.ChannelEvent;

public class ChannelAcceptHandler implements PipeHandler{
    @Override
    public boolean onRegisterHandler() {
        return true;
    }

    @Override
    public <T extends ChannelEvent> boolean onChannelAccepted(T event) {
        MessageBox.getLogger().log(event.getAddress().toString());
        return true;
    }
}
