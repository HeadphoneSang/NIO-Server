package com.chbcraft.internals.components.sysevent.net.http;

import com.chbcraft.internals.components.listen.HandlerList;
import com.chbcraft.internals.components.listen.RegisteredRouter;
import com.chbcraft.internals.components.sysevent.PluginEvent;
import com.chbcraft.net.handlers.outbound.HttpResponseMessage;

import java.lang.annotation.Annotation;

/**
 * 响应出站事件,发生在响应封装完毕,即将出站的最后一步,可以在这里修改响应体,拦截响应,获得请求的地址和类型等
 */
public class ResponseOutboundEvent extends PluginEvent {
    private static final HandlerList handlerList = new HandlerList();
    private final HttpResponseMessage message;
    public ResponseOutboundEvent(HttpResponseMessage message){

        this.message = message;
    }

    @Override
    public String getEventType() {
        return super.getEventType();
    }

    /**
     * 返回请求的路由地址
     * @return 字符串地址
     */
    public String path(){
        return message.getRoute();
    }

    /**
     * 返回请求方法类型
     * @return 返回泛型
     */
    public RegisteredRouter.RouteMethod method(){
        return message.getMethod();
    }
    @Override
    protected HandlerList getHandler() {
        return handlerList;
    }

    /**
     * 获得响应体对象
     * @return 返回响应体对象
     */
    public Object getResponseBody(){
        return message.getOriginalBody();
    }

    /**
     * 设置相应的响应头
     * @param headerName 响应头的名称
     * @param value 响应头的值
     */
    public void setHeader(CharSequence headerName,String value){
        message.setHeader(headerName,value);
    }

    /**
     * 获得响应头的值
     * @param headerName 响应头键值
     * @return 返回属性内容
     */
    public String getHeader(String headerName){
        return message.getHeader(headerName);
    }

    /**
     * 删除响应头
     * @param headerName 要删除的键
     */
    public void removeHeader(String headerName){
        message.removeHeader(headerName);
    }

    /**
     * 查看route处理器的是否有某个注解标注
     * @param tag 标签类型
     * @return 返回是否有这个标注
     */
    public boolean routeHasTag(Class<? extends Annotation> tag){
        return message.hasTag(tag);
    }


    public static HandlerList getHandlerList(){
        return handlerList;
    }

}
