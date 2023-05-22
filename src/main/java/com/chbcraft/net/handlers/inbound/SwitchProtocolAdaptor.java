package com.chbcraft.net.handlers.inbound;

import com.chbcraft.internals.components.FloatSphere;
import com.chbcraft.internals.components.enums.SectionName;
import com.chbcraft.net.handlers.inbound.websocket.*;
import com.chbcraft.net.util.RequestUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.timeout.IdleStateHandler;
import java.util.concurrent.TimeUnit;

/**
 * 用来分拣http请求
 */
public class SwitchProtocolAdaptor extends SimpleChannelInboundHandler<FullHttpRequest> {
    private final String isWebSocket = FloatSphere.getProperties().getString(SectionName.WS_URL.value());
    private boolean isKeepAlive = true;

    public boolean isKeepAlive() {
        return isKeepAlive;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        super.channelRead(ctx, msg);
        ctx.flush();
    }

    @Override

    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        if(request.uri().equalsIgnoreCase(isWebSocket)){//传给WebSocket处理器
            ctx.pipeline().remove("messageDeliver");
            ctx.pipeline().remove("idleStateHandler");
            ctx.pipeline().remove("timeoutHandler");
            ctx.pipeline().remove("http");
            long time = FloatSphere.getProperties().getInt(SectionName.LONG_TIME_OUT.value());
            ctx.pipeline().addFirst("idleStateHandler",new IdleStateHandler(time,time,time, TimeUnit.SECONDS))
                    .addAfter("idleStateHandler","timeoutHandler",new LongTimeOutHandler());
            ctx.pipeline()
                    .addLast("websocket",new WebSocketServerProtocolHandler(FloatSphere.getProperties().getString(SectionName.WS_URL.value()),null,true,5*1024*1024))
                    .addLast("frameSwitch",new WebSocketFrameHandler())
                    .addLast("websocketTextFrame",new TextFrameHandler())
                    .addLast("BinaryFrameHandler",new BinaryFrameHandler());
            ctx.fireChannelRead(request.retain());
        }else{
            boolean isOk = true;
            if(HttpHeaders.is100ContinueExpected(request)){
                isOk = RequestUtil.send100StateContinue(ctx);
            }
            if(isOk){
                if(ctx.pipeline().get("http") instanceof SimpleChannelInboundHandler){
                    String connectionValue;
                    if((connectionValue = request.headers().get(HttpHeaderNames.CONNECTION))!=null&&connectionValue.contentEquals(HttpHeaderValues.CLOSE)){
                        isKeepAlive = false;
                        if(ctx.pipeline().get("idleStateHandler")!=null)
                            ctx.pipeline().remove("idleStateHandler");
                        if(ctx.pipeline().get("timeoutHandler")!=null)
                            ctx.pipeline().remove("timeoutHandler");
                    }else{
                        isKeepAlive = true;
                        if(ctx.pipeline().get("timeoutHandler")==null)
                            ctx.pipeline().addFirst("timeoutHandler",new TimeOutHandler());
                        if(ctx.pipeline().get("idleStateHandler")==null){
                            long s = FloatSphere.getProperties().getLong(SectionName.TIME_OUT.value());
                            ctx.pipeline().addFirst("idleStateHandler",new IdleStateHandler(s,s,s, TimeUnit.SECONDS));
                        }
                    }
                    ctx.fireChannelRead(request.retain());
                }
            }
        }
    }



    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        super.channelReadComplete(ctx);
        ctx.flush();
    }
}
