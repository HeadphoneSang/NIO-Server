package com.chbcraft.net.tranfer.frameHandlers;

import com.chbcraft.internals.components.MessageBox;
import com.chbcraft.net.tranfer.FrameUtil;
import com.chbcraft.net.tranfer.TranProtocol;
import com.chbcraft.net.tranfer.TransferFrame;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

public class PingHandler implements FrameHandler{
    @Override
    public void handler(TransferFrame frame, ByteBuf ret, ChannelHandlerContext ctx) throws Exception {
        FrameUtil.writeFrame(ret, TranProtocol.TASK_RUNNING|TranProtocol.PONG,0);
        if(ctx.channel().isOpen()){
            ctx.writeAndFlush(new TextWebSocketFrame(ret));
        }
    }
}
