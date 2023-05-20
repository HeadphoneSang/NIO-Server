package com.chbcraft.net.handlers.inbound.websocket.event;

import com.chbcraft.internals.components.listen.HandlerList;
import com.chbcraft.internals.components.sysevent.PluginEvent;
import com.chbcraft.net.handlers.inbound.websocket.pojo.FileInfo;

import java.io.File;

public class FileUploadCompletedEvent extends PluginEvent {
    private static HandlerList handlerList = new HandlerList();

    private final FileInfo fileInfo;

    public FileUploadCompletedEvent(FileInfo info){
        this.fileInfo = info;
    }

    @Override
    public String getEventType() {
        return super.getEventType();
    }

    @Override
    protected HandlerList getHandler() {
        return handlerList;
    }

    public void setFileModifier(String modifier){
        fileInfo.setFileModifier(modifier);
    }

    public String getFileModifier(){
        return fileInfo.getFileModifier();
    }
    public String getUsername(){
        return fileInfo.getUsername();
    }

    public String getFilename(){
        return fileInfo.getFileName();
    }

    public void setOriginalFile(File file){
        fileInfo.setTarFile(file);
    }

    public File getFile(){
        return fileInfo.getTarFile();
    }
    public static HandlerList getHandlerList(){
        return handlerList;
    }
}
