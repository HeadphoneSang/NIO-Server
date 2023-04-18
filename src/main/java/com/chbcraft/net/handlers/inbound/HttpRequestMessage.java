package com.chbcraft.net.handlers.inbound;

import com.chbcraft.internals.components.FloatSphere;
import com.chbcraft.internals.components.enums.SectionName;
import com.chbcraft.internals.components.listen.RegisteredRouter;
import io.netty.handler.codec.http.FullHttpRequest;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HttpRequestMessage implements HttpRequestImp {
    /**
     * 用于代理的请求体
     */
    private FullHttpRequest request;

    public HttpRequestMessage(FullHttpRequest request){
        this.request = request;
    }

    /**
     * 获得请求的完整URL
     * @return 返回URL字符串
     */
    @Override
    public String url(){
        return request.uri();
    }

    /**
     * 获得请求的类型
     * @return 返回完整的请求类型
     */
    @Override
    public RegisteredRouter.RouteMethod method(){
        return RegisteredRouter.RouteMethod.valueOf(request.method().name());
    }

    /**
     * 获得请求体内容字符串
     * @return 返回字符串
     */
    @Override
    public String getContentText(){
        if(request.content().hasArray()){
            return new String(request.content().array(),0,request.content().arrayOffset());
        }else{
            return request.content().toString(Charset.forName(FloatSphere.getProperties().getString(SectionName.DECODE_CHARSET.value())));
        }
    }

    /**
     * 覆盖掉原来的请求体数据
     * @param text 新的请求体数据
     */
    @Override
    public void setContentText(String text){
        request.content().clear();
        request.content().writeBytes(text.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 获得请求头键名对应的值
     * @param key 键
     * @return 值
     */
    @Override
    public String getHeader(String key){
        return request.headers().get(key);
    }

    /**
     * 获得所有的请求体
     * @return 返回一个请求集合,只读,更改不会起作用
     */
    @Override
    public List<Map.Entry<String,String>> getHeaders(){
        return new ArrayList<>(request.headers().entries());
    }

    /**
     * 获得版本号
     * @return 返回版本号
     */
    @Override
    public String protocolVersion(){
        return request.protocolVersion().toString();
    }

}
