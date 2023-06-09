package com.chbcraft.internals.components.sysevent.net.ws;

import com.chbcraft.internals.components.listen.HandlerList;
import com.chbcraft.internals.components.sysevent.PluginEvent;

public class FileUploadInterruptEvent extends PluginEvent {

    private final long uploadTime;

    private final String modifier;

    private final String username;

    private final String tempFileName;

    private final String tarFileName;

    public FileUploadInterruptEvent(String modifier,long time,String username,String tempFileName,String tarFileName){
        this.modifier = modifier;
        this.uploadTime = time;
        this.username = username;
        this.tarFileName = tarFileName;
        this.tempFileName = tempFileName;
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

    private static final HandlerList handlerList = new HandlerList();

    @Override
    public String getEventType() {
        return super.getEventType();
    }

    @Override
    protected HandlerList getHandler() {
        return handlerList;
    }

    private static HandlerList getHandlerList(){
        return handlerList;
    }

    @Override
    public void setCancel() {
        super.setCancel();
    }

    @Override
    public void enableEvent() {
        super.enableEvent();
    }

    @Override
    public boolean isCancel() {
        return super.isCancel();
    }

    public String getTempFileName() {
        return tempFileName;
    }

    public String getTarFileName() {
        return tarFileName;
    }
}
