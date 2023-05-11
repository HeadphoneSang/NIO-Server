package com.chbcraft.net.handlers.inbound.websocket;

import com.chbcraft.net.handlers.inbound.websocket.pojo.WebFileResult;
import com.chbcraft.net.handlers.inbound.websocket.utils.ResultUtil;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

public class LongTimeOutHandler extends ChannelDuplexHandler {
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        IdleStateEvent e = (IdleStateEvent) evt;
        if (e.state() == IdleState.READER_IDLE) {
            ctx.channel().writeAndFlush(new TextWebSocketFrame(ResultUtil.getResultString(WebFileResult.TIME_OUT,null))).addListener(future -> {
                ctx.channel().writeAndFlush(new TextWebSocketFrame(ResultUtil.getResultString(WebFileResult.TIME_OUT,null))).sync();
                ctx.close();
            });
        } else if (e.state() == IdleState.WRITER_IDLE) {
            ctx.channel().writeAndFlush(new TextWebSocketFrame(ResultUtil.getResultString(WebFileResult.TIME_OUT,null))).addListener(future -> {
                ctx.channel().writeAndFlush(new TextWebSocketFrame(ResultUtil.getResultString(WebFileResult.CONNECT_ERROR,null))).sync();
                ctx.close();
            });
        }
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }

    @Override
    public void deregister(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
    }
}
