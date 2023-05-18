package com.chbcraft.net.handlers.inbound.websocket;

import com.chbcraft.internals.components.MessageBox;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.ContinuationWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

public class WebSocketFrameHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    private ByteBuf buff;
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        super.channelRead(ctx, msg);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame msg) throws Exception {
        if(msg instanceof BinaryWebSocketFrame){
            int length = msg.content().readableBytes();
            if(buff!=null){
                buff.release();
                buff = null;
            }
            buff = ctx.alloc().buffer(length);
            buff.writeBytes(msg.content());
            if(msg.isFinalFragment()){
                ctx.pipeline().context("websocketTextFrame").fireChannelRead(new BinaryWebSocketFrame(buff.retain()));
            }
        }else if(msg instanceof ContinuationWebSocketFrame){
            if(buff==null){
                MessageBox.getLogger().warn("cache is broken");
            }
            buff.writeBytes(msg.content());
            if(msg.isFinalFragment()){
                ctx.pipeline().context("websocketTextFrame").fireChannelRead(new BinaryWebSocketFrame(buff.retain()));
            }
        }else if(msg instanceof TextWebSocketFrame){
            ctx.fireChannelRead(msg.retain());
        }
    }
}
