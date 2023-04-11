package com.chbcraft.internals.components.sysevent;

import com.chbcraft.internals.components.listen.HandlerList;
import com.chbcraft.plugin.Plugin;

public class PluginLoadedEvent extends PluginEvent{
    private static final HandlerList handlerList = new HandlerList();
    private final Plugin plugin;
    public PluginLoadedEvent(Plugin plugin){
        this.plugin = plugin;
    }
    @Override
    public HandlerList getHandler() {
        return handlerList;
    }
    public static HandlerList getHandlerList(){
        return handlerList;
    }
    public Plugin getPlugin(){
        return this.plugin;
    }
}
