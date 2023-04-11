package com.chbcraft.internals.components.sysevent;

import com.chbcraft.internals.components.listen.Cancelable;

public abstract class PluginEvent extends Event implements Cancelable {
    private Event.EventState state = EventState.ENABLE;
    @Override
    public void setCancel(){
        this.state = EventState.CANCELABLE;
    }
    @Override
    public void enableEvent(){
        this.state = EventState.ENABLE;
    }
    @Override
    public boolean isCancel(){
        return this.state==EventState.CANCELABLE;
    }
}
