package com.chbcraft.net.tranfer.frameHandlers;

import com.chbcraft.net.tranfer.TransferFrame;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;

public interface FrameHandler {
    void handler(TransferFrame frame, ByteBuf ret, ChannelHandlerContext ctx) throws Exception;
}
