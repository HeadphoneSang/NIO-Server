package com.chbcraft.internals.components.sysevent;

import com.chbcraft.internals.components.listen.HandlerList;
import com.chbcraft.plugin.Plugin;

/**
 * 服务器插件卸载事件，发生在服务器卸载资源后，GC前
 * 此处可以卸载插件在其他插件注册的资源
 */
public class PluginDisableEvent extends PluginEvent{
    private static final HandlerList handlerList = new HandlerList();
    private final String pluginName;
    @Override
    public HandlerList getHandler() {
        return handlerList;
    }
    public PluginDisableEvent(String plugin){
        this.pluginName = plugin;
    }
    public String getPluginName() {
        return this.pluginName;
    }
    public static HandlerList getHandlerList(){
        return handlerList;
    }
}
