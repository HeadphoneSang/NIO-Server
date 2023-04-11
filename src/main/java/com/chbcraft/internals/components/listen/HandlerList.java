package com.chbcraft.internals.components.listen;

import com.chbcraft.internals.components.Listener;
import com.chbcraft.internals.components.MessageBox;
import com.chbcraft.internals.components.enums.EventPriority;
import com.chbcraft.plugin.Plugin;

import java.util.*;
import java.util.Map.Entry;

public class HandlerList {
    private RegisteredListener[] prepareListeners;
    private EnumMap<EventPriority, ArrayList<RegisteredListener>> listenMap = new EnumMap<>(EventPriority.class);
    private static final ArrayList<HandlerList> overHandlers = new ArrayList<>();
    public HandlerList(){
        for(EventPriority priority : EventPriority.values())
            listenMap.put(priority,new ArrayList<>());
        synchronized (overHandlers){
            overHandlers.add(this);
        }
    }

    /**
     * 注销掉所有的插件注册过的监听器
     * @param plugin 对应的插件
     */
    public static void unregisterAll(Plugin plugin){
        for(HandlerList handlerList : overHandlers){
            handlerList.unregister(plugin);
        }
    }
    /**
     * 将Map中的监听器按照顺序写入数组中
     */
    public synchronized void transform(){
        if(prepareListeners==null){
            ArrayList<RegisteredListener> tempList = new ArrayList<>();
            for(Entry<EventPriority, ArrayList<RegisteredListener>> entry:listenMap.entrySet()){
                ArrayList<RegisteredListener> listeners = entry.getValue();
                tempList.addAll(listeners);
            }
            prepareListeners = tempList.toArray(new RegisteredListener[tempList.size()]);
        }
    }
    /**
     * 从当前集合中删除掉plugin对象对应的监听器
     * @param plugin 删除的插件
     */
    public synchronized void unregister(Plugin plugin){
        ArrayList<RegisteredListener> priorityListener;
        boolean changed = false;
        for(Entry<EventPriority, ArrayList<RegisteredListener>> entry : listenMap.entrySet()){
            priorityListener = entry.getValue();
            Iterator<RegisteredListener> listeners = priorityListener.iterator();
            RegisteredListener listener;

            while(listeners.hasNext()){
                listener = listeners.next();
                if(listener.getPlugin().getName().equals(plugin.getName())){
                    listeners.remove();
                    changed = true;
                }
            }
        }
        if(changed)
            this.prepareListeners = null;
    }

    /**
     * 注销某一个特定的监听器
     * @param listener 监听对象
     */
    public synchronized void unregister(RegisteredListener listener){
        ArrayList<RegisteredListener> list = listenMap.get(listener.getPriority());
        boolean changed = false;
        Iterator<RegisteredListener> iterator = list.iterator();
        while(iterator.hasNext()){
            if(iterator.next().equals(listener)){
                iterator.remove();
                changed = true;
            }
        }
        if(changed)
            prepareListeners = null;
    }

    /**
     * 注销来自同一个Listener对象的监听器
     * @param listener 监听器的类
     */
    public synchronized void unregister(Listener listener){
        boolean changed = false;
        for(Entry<EventPriority,ArrayList<RegisteredListener>> entry : listenMap.entrySet()){
            Iterator<RegisteredListener> iterator = entry.getValue().iterator();
            while(iterator.hasNext()){
                if(iterator.next().getListener().equals(listener)){
                    iterator.remove();
                    changed = true;
                }
            }
        }
        if(changed)
            prepareListeners = null;
    }

    /**
     * 注册一个监听器到这个集合
     * @param listener 监听器
     */
    public synchronized void register(RegisteredListener listener){
        ArrayList<RegisteredListener> listeners = listenMap.get(listener.getPriority());
        if(listeners.contains(listener))
            MessageBox.getLogger().warnTips("Can not register this Listener in "+listener.getPlugin().getName()+"'s "+listener.getListener().getClass().getSimpleName()+",Because it already register a Listener!");
        else{
            listeners.add(listener);
            prepareListeners = null;
        }
    }

    /**
     * 注册集合中的所有的监听器
     * @param registerListeners 被注册的监听器
     */
    public synchronized void registerAll(Collection<RegisteredListener> registerListeners){
        for(RegisteredListener registeredListener : registerListeners)
            register(registeredListener);
    }

    /**
     * 获得所有的监听器
     * @return 返回监听器或者是null
     */
    public synchronized RegisteredListener[] getListeners(){
        if(prepareListeners==null)
            transform();
        return this.prepareListeners;
    }
}
