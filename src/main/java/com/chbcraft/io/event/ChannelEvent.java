package com.chbcraft.io.event;

import com.chbcraft.internals.components.listen.HandlerList;
import com.chbcraft.internals.components.sysevent.Event;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelectableChannel;

public class ChannelEvent <T extends AbstractSelectableChannel> extends Event {
    private static HandlerList handlerList = new HandlerList();
    private T channel;
    public ChannelEvent(T channel){
        this.channel = channel;
    }
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
     * {@code
     * private static HandlerList handlerList = new HandlerList();
     * <p>
     * public static HandlerList getHandlerList(){
     * <p>
     * return handlerList;
     * <p>
     * }
     * }
     *
     * @return 返回集合对象
     */
    @Override
    public HandlerList getHandler() {
        return null;
    }

    public static HandlerList getHandlerList(){
        return handlerList;
    }

    /**
     * 返回连接事件发生的管道的IP信息
     * @return 返回null或者是地址对象
     */
    public InetSocketAddress getAddress(){
        SocketAddress ret = null;

        if(channel instanceof SocketChannel){
            try {
                ret = ((SocketChannel)channel).getLocalAddress();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else if(channel instanceof ServerSocketChannel){
            try {
                ret = ((ServerSocketChannel)channel).getLocalAddress();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return ret!=null?(InetSocketAddress) ret:null;
    }
}
