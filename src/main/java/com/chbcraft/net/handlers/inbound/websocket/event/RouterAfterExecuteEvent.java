package com.chbcraft.net.handlers.inbound.websocket.event;

import com.chbcraft.internals.components.listen.HandlerList;
import com.chbcraft.internals.components.listen.RegisteredRouter;
import com.chbcraft.internals.components.sysevent.PluginEvent;

import java.lang.annotation.Annotation;

/**
 * 路由方法执行后出发的事件
 */
public class RouterAfterExecuteEvent extends PluginEvent {

    private static final HandlerList handlerList = new HandlerList();

    private final RegisteredRouter.RouterExecutorInfo info;

    public RouterAfterExecuteEvent(RegisteredRouter.RouterExecutorInfo info){
        this.info = info;
    }

    @Override
    public String getEventType() {
        return super.getEventType();
    }

    @Override
    protected HandlerList getHandler() {
        return handlerList;
    }

    public static HandlerList getHandlerList(){
        return handlerList;
    }

    public boolean hasTag(Class<? extends Annotation> clazz){
        return info.hasTags(clazz);
    }

    public Object[] getParams(){
        return info.getParams();
    }

    public Object getResult(){
        return info.getRouterResult();
    }

    public Object updateResult(Object newRet){
        return info.updateRouterResult(newRet);
    }

}
