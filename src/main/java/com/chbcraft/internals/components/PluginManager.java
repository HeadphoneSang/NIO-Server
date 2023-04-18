package com.chbcraft.internals.components;

import com.chbcraft.internals.components.listen.RegisteredRouter;
import com.chbcraft.internals.components.sysevent.Event;
import com.chbcraft.plugin.Plugin;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface PluginManager extends AbstractManager{
    void enablePlugins();
    void callEvent(Event event);
    void disablePlugin(String pluginName);
    void loadPlugin(String pluginName) throws Exception;
    void registerEventListener(Plugin plugin, Listener listener);
    void registerRouters(Plugin plugin, Routers routers);
    List<String> getPluginList();
    void noticeDis(String name);
    /**
     * 注销掉所有的正在运行的插件
     */
    void disablePlugins();
    RegisteredRouter getRouter(String method,String route,int length,int mLength);
}
