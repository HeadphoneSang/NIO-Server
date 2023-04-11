package com.chbcraft.internals.components;

import com.chbcraft.internals.components.sysevent.Event;
import com.chbcraft.plugin.Plugin;

public interface AbstractManager {

    void registerEventListener(Listener listener);

    void unregisterEventListener(Plugin plugin);
}
