package com.chbcraft.net.tranfer.frameHandlers;

import com.chbcraft.net.tranfer.TransferFrame;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class CtrlFrameAdaptor extends FrameAdaptor{
    @Override
    protected void handlerSub(TransferFrame frame, ByteBuf ret, ChannelHandlerContext ctx) throws Exception {
        int ctrlCode = frame.getProtocol() & ~0xf;
        FrameHandler handler = handlers.get(ctrlCode);
        if(handler!=null)
            handler.handler(frame,ret,ctx);
    }

    @Override
    public FrameAdaptor addFrameHandler(int protocol, FrameHandler handler) {
        return super.addFrameHandler(protocol, handler);
    }
}
