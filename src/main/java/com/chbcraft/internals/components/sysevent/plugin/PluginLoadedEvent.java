package com.chbcraft.internals.components.sysevent.plugin;

import com.chbcraft.internals.components.listen.HandlerList;
import com.chbcraft.internals.components.sysevent.PluginEvent;
import com.chbcraft.plugin.Plugin;

/**
 * 插件加载后触发的事件，可以在此处进行一些自动注册等操作
 */
public class PluginLoadedEvent extends PluginEvent {
    private static final HandlerList handlerList = new HandlerList();
    private final String pluginName;
    public PluginLoadedEvent(String pluginName){
        this.pluginName = pluginName;
    }
    @Override
    public HandlerList getHandler() {
        return handlerList;
    }
    public static HandlerList getHandlerList(){
        return handlerList;
    }
    public String getPluginName(){
        return pluginName;
    }
}
