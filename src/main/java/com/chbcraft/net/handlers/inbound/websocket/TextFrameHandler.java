package com.chbcraft.net.handlers.inbound.websocket;

import com.alibaba.fastjson.JSON;
import com.chbcraft.internals.components.FloatSphere;
import com.chbcraft.internals.components.MessageBox;
import com.chbcraft.net.handlers.inbound.OperationInbound;
import com.chbcraft.net.handlers.inbound.websocket.event.WebSocketOpenEvent;
import com.chbcraft.net.tranfer.*;
import com.chbcraft.net.tranfer.frameHandlers.*;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;

import java.io.File;
import java.io.IOException;

public class TextFrameHandler extends OperationInbound<TextWebSocketFrame> {

    private static final FrameAdaptor ctrlAdaptor = new CtrlFrameAdaptor();

    static{
        ctrlAdaptor
                .addFrameHandler(TranProtocol.TASK_CREATING,new TaskCreatingHandler())
                .addFrameHandler(TranProtocol.TASK_RUNNING,new TaskRunningHandler()
                    .addFrameHandler(TranProtocol.CANCEL_CONTINUE,new CancelTaskHandler())
                )
                .addFrameHandler(TranProtocol.TASK_COMPLETED,new TaskCompletedHandler());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {

        super.channelInactive(ctx);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

        if(evt == WebSocketServerProtocolHandler.ServerHandshakeStateEvent.HANDSHAKE_COMPLETE){
            FloatSphere.getPluginManager().callEvent(new WebSocketOpenEvent());
        }else{
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        super.channelRead(ctx, msg);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) {
        TransferFrame frame = JSON.parseObject(msg.text(),TransferFrame.class);
        ByteBuf ret = ctx.alloc().buffer();
        try {
            ctrlAdaptor.handler(frame,ret,ctx);
        } catch (Exception e) {
            FrameUtil.writeFrame(ret, TranProtocol.TASK_FAILED, 0);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }


}
