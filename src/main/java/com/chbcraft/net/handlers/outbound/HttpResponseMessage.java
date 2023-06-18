package com.chbcraft.net.handlers.outbound;

import com.chbcraft.internals.components.listen.RegisteredRouter;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponse;

import java.lang.annotation.Annotation;
import java.util.HashMap;


public class HttpResponseMessage {
    /**
     * 请求整体
     */
    private HttpResponse response;
    /**
     * 请求处理的标签
     */
    private final HashMap<Class<? extends Annotation>,Annotation> tags = new HashMap<>(3);
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
    public HttpResponseMessage(HttpResponse response,Object originalBody){
        this.response = response;
        this.originalBody = originalBody;
    }
    public HttpResponseMessage(Object originalBody){
        this(null,originalBody);
    }

    public void setHeader(CharSequence header,String content){
        response.headers().set(header,content);
    }

    public void removeHeader(String header){
        response.headers().remove(header);
    }

    public String getHeader(String header){
        return response.headers().get(header);
    }

    public void addTags(Annotation tag){
        this.tags.put(tag.annotationType(),tag);
    }

    public boolean hasTag(Class<? extends Annotation> clazz){
        return tags.containsKey(clazz);
    }
    public Object getOriginalBody() {
        return originalBody;
    }

    public void setResponse(HttpResponse response) {
        this.response = response;
    }

    HttpResponse getResponse(){
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

    @SuppressWarnings("unchecked")
    public <T> T getTag(Class<T> tagType){
        return tagType.cast(tags.get(tagType));
    }
}
