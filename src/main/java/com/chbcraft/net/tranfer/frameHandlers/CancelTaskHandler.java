package com.chbcraft.net.tranfer.frameHandlers;

import com.chbcraft.internals.components.FloatSphere;
import com.chbcraft.internals.components.MessageBox;
import com.chbcraft.net.handlers.inbound.websocket.TextFrameHandler;
import com.chbcraft.internals.components.sysevent.net.ws.FileUploadCancelEvent;
import com.chbcraft.net.handlers.inbound.websocket.pojo.FileInfo;
import com.chbcraft.net.tranfer.TransferFrame;
import com.chbcraft.net.util.CodeUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class CancelTaskHandler implements FrameHandler{
    @Override
    public void handler(TransferFrame frame, ByteBuf ret, ChannelHandlerContext ctx) throws Exception {

        String uuid = String.valueOf(frame.getAddition().get("uuid"));
        if(uuid!=null){
            TextFrameHandler txtHandler = (TextFrameHandler)ctx.handler();
            ChannelHandlerContext dataCtx =  txtHandler.getHandlerContext(uuid);
            if(dataCtx==null){
                return;
            }
            txtHandler.removeHandlerCtxByUUID(uuid);
            dataCtx.close();
            MessageBox.getLogger().debug("{}管道已注销",uuid);
        }
        else{
            MessageBox.getLogger().debug("取消任务错误");
        }

    }
}
