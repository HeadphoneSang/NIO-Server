package com.chbcraft.net.handlers.inbound.websocket.event;

import com.chbcraft.internals.components.listen.HandlerList;
import com.chbcraft.internals.components.sysevent.PluginEvent;
import com.chbcraft.net.handlers.inbound.websocket.pojo.FileInfo;

public class FileDownloadCompletedEvent extends PluginEvent {

    private static HandlerList handlerList = new HandlerList();

    private final FileInfo info;

    public FileDownloadCompletedEvent(FileInfo fileInfo){
        this.info = fileInfo;
    }

    public String getFileName(){
        return info.getFileName();
    }

    public String getModifier(){
        return info.getFileModifier();
    }

    public String getUsername(){
        return info.getUsername();
    }

    @Override
    public String getEventType() {
        return super.getEventType();
    }

    @Override
    protected HandlerList getHandler() {
        return handlerList;
    }

    public static HandlerList getHandlerList(){
        return handlerList;
    }
}
