package com.chbcraft.net.handlers.inbound;

import com.chbcraft.net.handlers.inbound.websocket.pojo.FileInfo;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.File;

public class OperationInbound<T> extends SimpleChannelInboundHandler<T> {

    private final FileInfo fileInfo = new FileInfo();



    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        super.channelRead(ctx, msg);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, T msg) throws Exception {

    }
    public FileInfo getFileInfo(){
        return fileInfo;
    }
}
