package com.chbcraft.internals.components;

import com.chbcraft.internals.components.sysevent.Event;
import com.chbcraft.plugin.Plugin;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public interface PluginManager extends AbstractManager{
    void enablePlugins();
    void callEvent(Event event);
    void disablePlugin(String pluginName);
    void loadPlugin(String pluginName) throws Exception;
    void registerEventListener(Plugin plugin, Listener listener);
    List<String> getPluginList();
    void noticeDis(String name);
}
