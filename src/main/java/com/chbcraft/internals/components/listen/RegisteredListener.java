package com.chbcraft.internals.components.listen;

import com.chbcraft.internals.components.Listener;
import com.chbcraft.internals.components.enums.EventPriority;
import com.chbcraft.internals.components.sysevent.Event;
import com.chbcraft.internals.components.sysevent.EventExecutor;
import com.chbcraft.plugin.Plugin;

import java.lang.reflect.InvocationTargetException;

public class RegisteredListener {
    private Plugin plugin;
    private EventPriority priority = EventPriority.MIDDLE;
    private EventExecutor executor;
    private Listener listener;
    public Plugin getPlugin() {
        return plugin;
    }
    public RegisteredListener(Plugin plugin,EventPriority priority,EventExecutor executor,Listener listener){
        this.listener = listener;
        this.plugin = plugin;
        this.priority = priority;
        this.executor = executor;
    }
    public void setPlugin(Plugin plugin) {
        this.plugin = plugin;
    }
    public EventPriority getPriority(){
        return this.priority;
    }
    public Listener getListener(){
        return this.listener;
    }
    public void callEvent(Event event){
        try {
            executor.executor(listener,event);
        } catch (InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
