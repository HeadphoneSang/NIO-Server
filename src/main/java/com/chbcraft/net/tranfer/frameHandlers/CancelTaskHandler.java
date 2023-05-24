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
        ctx.close();
        //触发取消任务事件
        FileInfo info = ctx.pipeline().get(TextFrameHandler.class).getFileInfo();
        String name = info.getFileName()!=null?info.getFileName():info.getTarFile().getName();
        MessageBox.getLogger().warnTips("!connection is canceled:{} by user: {}",name,info.getUsername());
        FloatSphere.getPluginManager().callEvent(new FileUploadCancelEvent(CodeUtil.encodeBase64(info.getTempFile().getAbsolutePath()),System.currentTimeMillis(),info.getUsername()));
    }
}
