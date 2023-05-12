package com.chbcraft.net.handlers.inbound;

import com.chbcraft.internals.components.FloatSphere;
import com.chbcraft.internals.components.enums.ConfigType;
import com.chbcraft.internals.components.enums.SectionName;
import com.chbcraft.net.handlers.inbound.websocket.LongTimeOutHandler;
import com.chbcraft.net.util.RequestUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.AttributeKey;

import java.net.URLDecoder;
import java.util.concurrent.TimeUnit;

/**
 * 用来分拣http请求
 */
@ChannelHandler.Sharable
public class SwitchProtocolAdaptor extends SimpleChannelInboundHandler<FullHttpRequest> {
    private final String isWebSocket = FloatSphere.getProperties().getString(SectionName.WS_URL.value());
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        super.channelRead(ctx, msg);
        ctx.flush();
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {

        super.channelInactive(ctx);

    }

    @Override

    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        if(request.uri().equalsIgnoreCase(isWebSocket)){//传给WebSocket处理器
            ctx.pipeline().remove("messageDeliver");
            ctx.pipeline().remove("idleStateHandler");
            ctx.pipeline().remove("timeoutHandler");
            ctx.pipeline().remove("adaptor");
            ctx.pipeline().remove("http");
            ctx.pipeline().remove("downloadHandler");
            long time = FloatSphere.getProperties().getInt(SectionName.LONG_TIME_OUT.value());
            ctx.pipeline().addFirst("idleStateHandler",new IdleStateHandler(time,time,time, TimeUnit.SECONDS))
                    .addFirst("timeoutHandler",new LongTimeOutHandler());
            ctx.fireChannelRead(request.retain());
        }else{
            boolean isOk = true;
            if(HttpHeaders.is100ContinueExpected(request)){
                isOk = RequestUtil.send100StateContinue(ctx);
            }
            if(isOk){
                request.retain();
                if(ctx.pipeline().get("http") instanceof SimpleChannelInboundHandler)
                    ((SimpleChannelInboundHandler<?>)ctx.pipeline().get("http")).channelRead(ctx,request);
            }
        }
    }



    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        super.channelReadComplete(ctx);
        ctx.flush();
    }
}
