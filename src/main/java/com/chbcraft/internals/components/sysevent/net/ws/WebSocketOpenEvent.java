package com.chbcraft.internals.components.sysevent.net.ws;

import com.chbcraft.internals.components.listen.HandlerList;
import com.chbcraft.internals.components.sysevent.PluginEvent;

public class WebSocketOpenEvent extends PluginEvent {
    private static final HandlerList handlerList = new HandlerList();

    public WebSocketOpenEvent(){
    }
    @Override
    public String getEventType() {
        return super.getEventType();
    }

    @Override
    public HandlerList getHandler() {
        return handlerList;
    }
    public static HandlerList getHandlerList(){
        return handlerList;
    }
}
