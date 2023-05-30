package com.chbcraft.net.tranfer.frameHandlers;

import com.chbcraft.internals.components.MessageBox;
import com.chbcraft.net.handlers.inbound.websocket.BinaryFrameHandler;
import com.chbcraft.net.handlers.inbound.websocket.LongTimeOutHandler;
import com.chbcraft.net.handlers.inbound.websocket.TextFrameHandler;
import com.chbcraft.net.tranfer.FrameUtil;
import com.chbcraft.net.tranfer.TranProtocol;
import com.chbcraft.net.tranfer.TransferFrame;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.timeout.IdleStateHandler;

import java.nio.charset.Charset;

/**
 * 创建控制连接
 */
public class ControlTaskCreateHandler implements FrameHandler{
    @Override
    public void handler(TransferFrame frame, ByteBuf ret, ChannelHandlerContext ctx) throws Exception {
        MessageBox.getLogger().debug("控制管道：建立中...");
        IdleStateHandler time = ctx.pipeline().get(IdleStateHandler.class);
        BinaryFrameHandler binaryFrameHandler = ctx.pipeline().get(BinaryFrameHandler.class);
        if(time!=null){
            ctx.pipeline().remove(IdleStateHandler.class);
            ctx.pipeline().remove(LongTimeOutHandler.class);
        }
        if(binaryFrameHandler!=null){
            ctx.pipeline().remove(BinaryFrameHandler.class);
        }
        FrameUtil.writeFrame(ret,TranProtocol.TASK_CREATING|TranProtocol.CTRL_CREATED,0);
        ctx.writeAndFlush(new TextWebSocketFrame(ret)).addListener((future -> {
            if(!future.isSuccess()){
                MessageBox.getLogger().warnTips("控制连接已失效");
            }
        }));
    }
}
