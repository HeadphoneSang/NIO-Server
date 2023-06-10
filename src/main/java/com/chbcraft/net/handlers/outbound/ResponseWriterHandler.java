package com.chbcraft.net.handlers.outbound;

import com.alibaba.fastjson.JSON;
import com.chbcraft.internals.components.FloatSphere;
import com.chbcraft.internals.components.listen.Resource;
import com.chbcraft.internals.components.sysevent.net.http.ResponseOutboundEvent;
import com.chbcraft.net.handlers.inbound.SwitchProtocolAdaptor;
import com.chbcraft.net.util.ResponseUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.*;
import java.nio.charset.StandardCharsets;

@ChannelHandler.Sharable
public class ResponseWriterHandler extends ChannelOutboundHandlerAdapter {

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if(!(msg instanceof HttpResponseMessage)){
            super.write(ctx, msg, promise);
            return;
        }
        HttpResponseMessage responseMessage = (HttpResponseMessage)msg;
        if(responseMessage.hasTag(Resource.class)){
            super.write(ctx, msg, promise);
            return;
        }
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        responseMessage.setResponse(response);
        ResponseOutboundEvent event = new ResponseOutboundEvent(responseMessage);
        FloatSphere.getPluginManager().callEvent(event);
        if(event.isCancel()){
            response.setStatus(HttpResponseStatus.FORBIDDEN);
            response.content().clear();
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH,0);
        }else{
            try{
                if(response.headers().get(HttpHeaderNames.CONTENT_TYPE)==null||response.headers().get(HttpHeaderNames.CONTENT_TYPE).startsWith("application/json")){
                    String retMsg = JSON.toJSONString(responseMessage.getOriginalBody());
                    byte[] res = retMsg.getBytes(StandardCharsets.UTF_8);
                    response.content().writeBytes(res);
                    ResponseUtil.setDefaultHeaders(response.headers(),res.length);
                }else{
                    String retMsg = String.valueOf(responseMessage.getOriginalBody());
                    response.content().writeBytes(retMsg.getBytes(StandardCharsets.UTF_8));
                    ResponseUtil.setDefaultHeadersVoid(response.headers(),response.content().readableBytes());
                }
            }catch (Exception e){
                response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,HttpResponseStatus.INTERNAL_SERVER_ERROR);
                responseMessage.setResponse(response);
                response.content().writeBytes("500 INTERNAL ERROR".getBytes(StandardCharsets.UTF_8));
                promise.addListener(future -> {
                    System.out.println("close1");
                    if (future.isSuccess())
                        ctx.close();
                });
            }
        }
        super.write(ctx, responseMessage.getResponse(), promise);
        super.flush(ctx);
        if(!ctx.pipeline().get(SwitchProtocolAdaptor.class).isKeepAlive())
            ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }
}
