package com.chbcraft.internals.components.sysevent.net.http;

import com.chbcraft.internals.components.listen.HandlerList;
import com.chbcraft.internals.components.listen.RegisteredRouter;
import com.chbcraft.internals.components.sysevent.PluginEvent;
import com.chbcraft.net.handlers.inbound.HttpRequestImp;
import com.chbcraft.net.handlers.inbound.HttpRequestMessage;

import java.util.List;
import java.util.Map;

/**
 * 响应入站事件,
 */
public class RequestInboundEvent extends PluginEvent implements HttpRequestImp {
    private static final HandlerList handlerList = new HandlerList();

    private final HttpRequestMessage message;

    public RequestInboundEvent(HttpRequestMessage message){
        this.message = message;
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

    @Override
    public String url() {
        return message.url();
    }

    @Override
    public RegisteredRouter.RouteMethod method() {
        return message.method();
    }

    @Override
    public String getContentText() {
        return message.getContentText();
    }

    @Override
    public void setContentText(String text) {
        message.setContentText(text);
    }

    @Override
    public String getHeader(String key) {
        return message.getHeader(key);
    }

    @Override
    public List<Map.Entry<String, String>> getHeaders() {
        return message.getHeaders();
    }

    @Override
    public String protocolVersion() {
        return message.protocolVersion();
    }
}
