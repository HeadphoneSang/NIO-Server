package com.chbcraft.net.handlers.inbound.websocket;

import com.chbcraft.internals.components.FloatSphere;
import com.chbcraft.internals.components.MessageBox;
import com.chbcraft.internals.components.enums.SectionName;
import com.chbcraft.net.handlers.inbound.OperationInbound;
import com.chbcraft.net.handlers.inbound.websocket.event.FileUploadCompletedEvent;
import com.chbcraft.net.handlers.inbound.websocket.pojo.FileInfo;
import com.chbcraft.net.handlers.inbound.websocket.pojo.WebFileResult;
import com.chbcraft.net.handlers.inbound.websocket.utils.ResultUtil;
import com.chbcraft.net.util.CodeUtil;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.io.File;
import java.io.FileOutputStream;


public class BinaryFrameHandler extends SimpleChannelInboundHandler<BinaryWebSocketFrame> {

    private long size = 0;

    private File targetFile = null;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        super.channelRead(ctx, msg);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, BinaryWebSocketFrame msg) throws Exception {
        FileInfo fileInfo = ctx.pipeline().get(OperationInbound.class).getFileInfo();
        if(targetFile==null&&fileInfo.getOriginalFile()!=null){
            targetFile = fileInfo.getOriginalFile();
        }else if(targetFile==null){
            targetFile = new File(CodeUtil.decodeBase64(fileInfo.getFileModifier()),fileInfo.getFileName());
        }
        if(size==0&&targetFile.exists()){
            Object[] data = {1,fileInfo.getFileSize()};
            ctx.channel().writeAndFlush(new TextWebSocketFrame(ResultUtil.getResultString(WebFileResult.TRANSPORT_CONTINUE,data)));
            return;
        }
        if(targetFile.exists()||targetFile.createNewFile()){
            FileOutputStream output = new FileOutputStream(targetFile,true);
            int length = msg.content().readableBytes();
            byte[] buf = new byte[length];
            msg.content().readBytes(buf);
            output.write(buf,0,length);
            output.close();
            size +=length;
            if(size==fileInfo.getFileSize()){
                FileUploadCompletedEvent event = new FileUploadCompletedEvent(fileInfo);
                FloatSphere.getPluginManager().callEvent(event);
                if(event.isCancel()){
                    ctx.channel().close();
                    return;
                }
            }
            float e = (float)size/(float)fileInfo.getFileSize();
            Object[] data = {e,size};
            ctx.channel().writeAndFlush(new TextWebSocketFrame(ResultUtil.getResultString(WebFileResult.TRANSPORT_CONTINUE,data))).addListener(future -> {
                if(!future.isSuccess()){
                    MessageBox.getLogger().warn(future.cause().getMessage()+'\n');
                }
            });
        }else{
            MessageBox.getLogger().warn("wrong fileInfo: "+fileInfo.getFileName()+" channel is closed");
            ctx.channel().writeAndFlush(new TextWebSocketFrame(ResultUtil.getResultString(WebFileResult.FORBIDDEN_WS,null)));
            ctx.channel().close().addListener(future -> {
                if(future.isSuccess())
                    MessageBox.getLogger().warnTips("403 forbidden connected ws is closed");
            });
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        super.channelWritabilityChanged(ctx);
    }


}
