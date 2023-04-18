package com.chbcraft.net.handlers.outbound;

import com.chbcraft.internals.components.listen.RegisteredRouter;
import io.netty.handler.codec.http.FullHttpResponse;
import java.util.HashMap;


public class HttpResponseMessage {
    /**
     * 请求整体
     */
    private FullHttpResponse response;
    /**
     * 请求处理的标签
     */
    private final HashMap<String,Object> tags = new HashMap<>(3);
    /**
     * 请求体元数据
     */
    private final Object originalBody;
    /**
     * 请求路由
     */
    private String route;
    /**
     * 路由方法
     */
    private RegisteredRouter.RouteMethod method;
    public HttpResponseMessage(FullHttpResponse response,Object originalBody){
        this.response = response;
        this.originalBody = originalBody;
    }
    public HttpResponseMessage(Object originalBody){
        this(null,originalBody);
    }

    public void setHeader(String header,String content){
        response.headers().set(header,content);
    }

    public void removeHeader(String header){
        response.headers().remove(header);
    }

    public String getHeader(String header){
        return response.headers().get(header);
    }

    public void addTags(String tag){
        this.tags.put(tag,null);
    }

    public boolean hasTag(String tag){
        return tags.containsKey(tag);
    }

    public Object getOriginalBody() {
        return originalBody;
    }

    public void setResponse(FullHttpResponse response) {
        this.response = response;
    }

    FullHttpResponse getResponse(){
        return this.response;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public RegisteredRouter.RouteMethod getMethod() {
        return method;
    }

    public void setMethod(RegisteredRouter.RouteMethod method) {
        this.method = method;
    }
}
