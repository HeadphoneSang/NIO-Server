package com.chbcraft.internals.components.sysevent;

import com.chbcraft.internals.components.listen.HandlerList;

public abstract class Event {
    /**
     * 存储事件的类别
     */
    private String eventType;

    /**
     * 事件类型的名称
     * @return 返回事件的类型，例如PluginLoadEvent
     */
    public String getEventType(){
        if(eventType==null){
            eventType = this.getClass().getSimpleName();
        }
        return eventType;
    }

    /**
     * 获得这个时间对应的监听器集合对象
     * 继承的子类事件需要声明一个方法,格式为:
     * {@code
     *  private static HandlerList handlerList = new HandlerList();
     *
     *  public static HandlerList getHandlerList(){
     *
     *        return handlerList;
     *
     *  }
     * }
     * @return 返回集合对象
     */
    protected abstract HandlerList getHandler();
    public enum EventState{
        ENABLE,
        CANCELABLE

    }
}
