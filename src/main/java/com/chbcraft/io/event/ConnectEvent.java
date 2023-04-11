package com.chbcraft.io.event;

import com.chbcraft.internals.components.listen.HandlerList;
import com.chbcraft.internals.components.sysevent.Event;

import java.nio.channels.Channel;

public abstract class ConnectEvent extends Event {
    private static HandlerList handlerList = new HandlerList();
    /**
     * 事件类型的名称
     *
     * @return 返回事件的类型，例如PluginLoadEvent
     */
    @Override
    public String getEventType() {
        return super.getEventType();
    }

    /**
     * 获得这个时间对应的监听器集合对象
     * 继承的子类事件需要声明一个方法,格式为:
     *
     * @return 返回集合对象
     * @<code> private static HandlerList handlerList = new HandlerList();
     * <p>
     * public static HandlerList getHandlerList(){
     * <p>
     * return this.handlerList;
     * <p>
     * }
     * </code>
     */
    @Override
    public HandlerList getHandler() {
        return null;
    }
    public static HandlerList getHandlerList(){

        return handlerList;

    }
    public abstract Channel getChannel();
}
