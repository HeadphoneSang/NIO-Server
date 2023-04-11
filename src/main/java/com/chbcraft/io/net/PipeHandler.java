package com.chbcraft.io.net;

import com.chbcraft.io.event.ChannelEvent;


public interface PipeHandler {
    boolean onRegisterHandler();
    <T extends ChannelEvent> boolean onChannelAccepted(T event);
}
