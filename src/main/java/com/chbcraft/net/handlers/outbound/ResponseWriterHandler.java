package com.chbcraft.net.handlers.outbound;

import com.alibaba.fastjson.JSON;
import com.chbcraft.internals.components.FloatSphere;
import com.chbcraft.internals.components.sysevent.net.ResponseOutboundEvent;
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
    public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        super.disconnect(ctx, promise);
    }

    @Override
    public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        super.close(ctx, promise);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        HttpResponseMessage responseMessage = (HttpResponseMessage)msg;
        FullHttpResponse response;
        try{
            String retMsg = JSON.toJSONString(((HttpResponseMessage) msg).getOriginalBody());
            response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
            byte[] res = retMsg.getBytes(StandardCharsets.UTF_8);
            response.content().writeBytes(res);
            ResponseUtil.setDefaultHeaders(response.headers(),res.length);
        }catch (Exception e){
            response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,HttpResponseStatus.INTERNAL_SERVER_ERROR);
            response.content().writeBytes("500 INTERNAL ERROR".getBytes(StandardCharsets.UTF_8));
            promise.addListener(future -> {
                if (future.isSuccess())
                    ctx.close();
            });
        }
        ResponseOutboundEvent event = new ResponseOutboundEvent(responseMessage);
        FloatSphere.getPluginManager().callEvent(event);
        if(event.isCancel()){
            response.setStatus(HttpResponseStatus.FORBIDDEN);
            response.content().clear();
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH,0);
        }
        responseMessage.setResponse(response);
        super.write(ctx, responseMessage.getResponse(), promise);
        super.flush(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }
}
