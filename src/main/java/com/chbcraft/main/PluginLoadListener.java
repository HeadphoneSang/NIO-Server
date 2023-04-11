package com.chbcraft.main;

import com.chbcraft.internals.components.Listener;
import com.chbcraft.internals.components.enums.EventPriority;
import com.chbcraft.internals.components.listen.EventHandler;
import com.chbcraft.internals.components.sysevent.PluginLoadedEvent;

public class PluginLoadListener implements Listener {
    public void onPluginLoaded(PluginLoadedEvent event){
        event.getPlugin().onEnable();
    }
}
