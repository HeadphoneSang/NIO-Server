package com.chbcraft.net.util;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;

import java.nio.charset.StandardCharsets;
import java.util.Date;

public class ResponseUtil {
    private ResponseUtil(){}
    public static void setDefaultHeaders(HttpHeaders headers,int length){
        headers.set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_HEADERS,"*");
        headers.set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN,"*");
        headers.set(HttpHeaderNames.DATE,new Date().toString());
        headers.set(HttpHeaderNames.CONTENT_LENGTH,length);
        headers.set(HttpHeaderNames.CONTENT_TYPE,"application/json; charset=utf-8");
        headers.set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
    }

    /**
     * 发送403禁止请求
     * @param ctx 请求连接管道对象
     */
    public static void send403Forbidden(ChannelHandlerContext ctx){
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FORBIDDEN);
        response.content().writeBytes("403 Request Forbidden".getBytes(StandardCharsets.UTF_8));
        ctx.writeAndFlush(response).addListener(future -> {
            if(future.isSuccess()||future.isCancellable()||future.isCancelled()){
                ctx.channel().close();
            }
        });
    }
}
