package com.chbcraft.net.tranfer.frameHandlers;

import com.chbcraft.net.tranfer.TransferFrame;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class CancelTaskHandler implements FrameHandler{
    @Override
    public void handler(TransferFrame frame, ByteBuf ret, ChannelHandlerContext ctx) throws Exception {
        ctx.close();
        //触发取消任务事件
    }
}
