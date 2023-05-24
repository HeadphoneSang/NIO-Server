package com.chbcraft.net.tranfer.frameHandlers;

import com.chbcraft.internals.components.MessageBox;
import com.chbcraft.net.handlers.inbound.OperationInbound;
import com.chbcraft.net.tranfer.FrameUtil;
import com.chbcraft.net.tranfer.TranProtocol;
import com.chbcraft.net.tranfer.TransferFrame;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;

public class TaskCreatingHandler extends FrameAdaptor{


    @Override
    protected void handlerSub(TransferFrame frame, ByteBuf ret, ChannelHandlerContext ctx) throws Exception {
        FrameHandler handler = handlers.get(frame.getProtocol() & 0xf);
        if(handler==null){
            MessageBox.getLogger().warnTips("unknown protocol: {}",frame.getProtocol());
            return;
        }
        handler.handler(frame,ret,ctx);
    }
}
