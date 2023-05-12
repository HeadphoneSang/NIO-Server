package com.chbcraft.net.util;

import com.chbcraft.internals.components.FloatSphere;
import com.chbcraft.internals.components.enums.SectionName;
import com.chbcraft.internals.components.listen.PojoResponse;
import com.chbcraft.internals.components.listen.RegisteredRouter;
import com.chbcraft.net.handlers.outbound.HttpResponseMessage;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;

import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class RequestUtil {
    private RequestUtil(){}

    /**
     * 向客户端返回一个允许继续传送完整请求的100状态码
     * @param ctx 连接上下文对象
     * @return 返回是否给客户端发送成功
     * @throws InterruptedException 线程中断错误
     */
    public static boolean send100StateContinue(ChannelHandlerContext ctx) throws InterruptedException {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE);
        ChannelFuture future = ctx.writeAndFlush(response);
        future.sync();
        return future.isSuccess();
    }

    /**
     * 返回404未找到状态,并且关闭通道
     * @param ctx 上下文
     */
    public static void send404State(ChannelHandlerContext ctx){
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND);
        response.content().writeBytes("404 Not Find".getBytes(StandardCharsets.UTF_8));
        ctx.writeAndFlush(response).addListener(future -> {
            if(future.isSuccess()||future.isCancellable()||future.isCancelled()){
                ctx.close();
            }
        });

    }

    /**
     * 返回500错误
     * @param ctx 上下文
     */
    public static void send500State(ChannelHandlerContext ctx){
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR);
        response.content().writeBytes("500 INTERNAL SERVER_ERROR!".getBytes(StandardCharsets.UTF_8));
        ctx.writeAndFlush(response).addListener(future -> {
            if(future.isSuccess()||future.isCancellable()||future.isCancelled()){
                ctx.close();
            }
        });
    }

    /**
     * 返回510错误
     * @param ctx 上下文
     */
    public static void send510State(ChannelHandlerContext ctx){
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_EXTENDED);
        response.content().writeBytes("510 Not Extended!".getBytes(StandardCharsets.UTF_8));
        ctx.writeAndFlush(response).addListener(future -> {
            if(future.isSuccess()||future.isCancellable()||future.isCancelled()){
                ctx.close();
            }
        });
    }

    /**
     * 返回405 不支持的请求方式
     * @param ctx 上下文
     */
    public static void send405State(ChannelHandlerContext ctx){
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.METHOD_NOT_ALLOWED);
        response.content().writeBytes("405 Method Not Allowed".getBytes(StandardCharsets.UTF_8));
        ctx.writeAndFlush(response).addListener(future -> {
            if(future.isSuccess()||future.isCancellable()||future.isCancelled()){
                ctx.close();
            }
        });

    }

    /**
     * 返回405 不支持的请求方式
     * @param ctx 上下文
     */
    public static void sendOptionsResponse(ChannelHandlerContext ctx){
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        ResponseUtil.setDefaultHeaders(response.headers(),0);
        response.headers().set(HttpHeaderNames.ALLOW,"OPTIONS, GET, POST");
        ctx.writeAndFlush(response);
    }

    /**
     * 返回状态码204,表示没有处理的内容要返回
     * @param ctx 请求连接对话
     */
    public static void send204States(ChannelHandlerContext ctx){
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,HttpResponseStatus.NO_CONTENT);
        ctx.writeAndFlush(response).addListener(future -> {
            if(future.isSuccess()||future.isCancellable()||future.isCancelled()){
                ctx.close();
            }
        });
    }

    /**
     * 解码urlencoded的请求
     * @param request 完整请求
     * @return 返回解码后的结果
     */
    public static String decodeUrl(FullHttpRequest request){
        String ret;
        try{
            ret = URLDecoder.decode(request.uri(), FloatSphere.getProperties().getString(SectionName.DECODE_CHARSET.value()));

        } catch (UnsupportedEncodingException e) {
            return null;
        }
        return ret;
    }

    /**
     * 将name=1231&user=12313这种字符串解析成MAP
     * @param paramStr 要解析的字符串
     * @return 返回的表
     */
    public static Map<String,Object> decodeGetPath(String paramStr){
        Map<String,Object> ret = new HashMap<>();
        String[] params = paramStr.split("&");
        for (String p : params){
            String[] temp = p.split("=");
            if(temp==null||temp.length<2)
                break;
            ret.put(temp[0],temp[1]);
        }
        return ret;
    }

    /**
     * 创建一个response消息封装对象
     * @param original 数据源
     * @return 返回封装对象
     */
    public static HttpResponseMessage createResponseMessage(Object original, RegisteredRouter router){
        HttpResponseMessage ret = new HttpResponseMessage(original);
        Collection<Class<? extends Annotation>> tags = router.getTags();
        if(tags==null)
            return ret;
        for (Class<? extends Annotation> tag : router.getTags()) {
            ret.addTags(tag.getSimpleName());
        }
        return ret;
    }
    public enum HandlerResultState{
        OPTIONS_REQUEST("OPTIONS_REQUEST"),
        UNSUPPORTED_METHOD("UNSUPPORTED_METHOD"),
        FORMAT_ERROR("FORMAT_ERROR"),
        RUNTIME_ERROR("RUNTIME_ERROR"),
        NO_MATCHES("NO_MATCHES"),
        NO_RESULT("NO_RESULT");
        private final String value;
        HandlerResultState(String res){
            this.value = res;
        }

        public String getValue() {
            return value;
        }
    }
}
