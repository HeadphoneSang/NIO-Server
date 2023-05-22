package com.chbcraft.net.handlers.inbound.websocket.event;

import com.chbcraft.internals.components.listen.HandlerList;
import com.chbcraft.internals.components.sysevent.PluginEvent;

public class FileUploadCancelEvent extends PluginEvent {

    private final long uploadTime;

    private final String modifier;

    private final String username;

    private static final HandlerList handlerList = new HandlerList();

    public FileUploadCancelEvent(String modifier,long time,String username){
        this.modifier = modifier;
        this.uploadTime = time;
        this.username = username;
    }

    @Override
    public String getEventType() {
        return super.getEventType();
    }

    @Override
    protected HandlerList getHandler() {
        return handlerList;
    }

    public long getUploadTime() {
        return uploadTime;
    }

    public String getModifier() {
        return modifier;
    }

    public String getUsername() {
        return username;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }
}
