package com.chbcraft.net.tranfer.frameHandlers;

import com.chbcraft.internals.components.MessageBox;
import com.chbcraft.net.handlers.inbound.websocket.TextFrameHandler;
import com.chbcraft.net.tranfer.TransferFrame;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class TaskCompletedHandler implements FrameHandler{
    @Override
    public void handler(TransferFrame frame, ByteBuf ret, ChannelHandlerContext ctx) throws Exception {
        MessageBox.getLogger().trace("Task completed");
//        String uuid = String.valueOf(frame.getAddition().get("uuid"));
//        ChannelHandlerContext data = ctx.pipeline().get(TextFrameHandler.class).getHandlerContext(uuid);
//        if(data!=null){
//            data.close();
//            ctx.pipeline().get(TextFrameHandler.class).removeHandlerCtxByUUID(uuid);
//        }
        ctx.close();
    }
}
