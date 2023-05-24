package com.chbcraft.net.tranfer.frameHandlers;

import com.chbcraft.internals.components.MessageBox;
import com.chbcraft.net.handlers.inbound.websocket.TextFrameHandler;
import com.chbcraft.net.tranfer.FrameUtil;
import com.chbcraft.net.tranfer.TranProtocol;
import com.chbcraft.net.tranfer.TransferFrame;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * 创建控制连接
 */
public class ControlTaskCreateHandler implements FrameHandler{
    @Override
    public void handler(TransferFrame frame, ByteBuf ret, ChannelHandlerContext ctx) throws Exception {
        MessageBox.getLogger().warnTips("控制管道：{}，建立中...",frame.getAddition().get("uuid"));
        ChannelHandlerContext dataCtx = ((TextFrameHandler)ctx.handler()).getHandlerContext(String.valueOf(frame.getAddition().get("uuid")));
        if(dataCtx!=null){
            FrameUtil.writeFrame(ret, TranProtocol.TASK_CREATING|TranProtocol.CTRL_CREATED,0);
        }else{
            FrameUtil.writeFrame(ret,TranProtocol.TASK_CREATING|TranProtocol.CTRL_FAILED,0);
        }
        ctx.writeAndFlush(ret);
    }
}
