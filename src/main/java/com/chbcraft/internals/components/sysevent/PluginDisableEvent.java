package com.chbcraft.internals.components.sysevent;

import com.chbcraft.internals.components.listen.HandlerList;
import com.chbcraft.plugin.Plugin;

public class PluginDisableEvent extends PluginEvent{
    private static HandlerList handlerList = new HandlerList();
    private Plugin plugin;
    @Override
    public HandlerList getHandler() {
        return handlerList;
    }
    public PluginDisableEvent(Plugin plugin){
        this.plugin = plugin;
    }
    public String getPluginName() {
        return plugin.getName();
    }
    public static HandlerList getHandlerList(){
        return handlerList;
    }
}
