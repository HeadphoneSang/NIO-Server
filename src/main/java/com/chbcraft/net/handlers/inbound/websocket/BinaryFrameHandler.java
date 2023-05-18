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
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;


public class BinaryFrameHandler extends SimpleChannelInboundHandler<BinaryWebSocketFrame> {

    private long size = 0;

    private File targetFile = null;

    private int times = 0;

    private long start;

    private final int max = FloatSphere.getProperties().getInt(SectionName.RELOAD_TIMES.value());

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        super.channelRead(ctx, msg);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {

        if(targetFile==null){
            ctx.channel().close();
            MessageBox.getLogger().warnTips("no file");
            return;
        }
        if(targetFile.getName().lastIndexOf(".temp")!=-1){
            MessageBox.getLogger().warnTips("upload task:{} will done",targetFile.getName());
            System.gc();
            MessageBox.getLogger().warnTips("has file");
            if(targetFile.delete()){
                if(ctx.channel().isOpen())
                try{
                    ctx.channel().writeAndFlush(new TextWebSocketFrame(ResultUtil.getResultString(WebFileResult.FILE_BROKEN,null))).sync();
                }catch (Exception e){
                    MessageBox.getLogger().warnTips("connect is closed");
                }
                finally {
                    ctx.channel().close();
                }
            }

        }
        super.channelInactive(ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
//        MessageBox.getLogger().warnTips("upload end:");
//        if(targetFile==null){
//            MessageBox.getLogger().warnTips("upload task:{} completed","123");
//            return;
//        }
//        if(targetFile.getName().lastIndexOf(".temp")!=-1){
//            System.gc();
//            targetFile.delete();
//            MessageBox.getLogger().warnTips("upload task interrupted");
//        }
        super.channelUnregistered(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, BinaryWebSocketFrame msg) throws Exception {
        FileInfo fileInfo = ctx.pipeline().get(OperationInbound.class).getFileInfo();
        if(targetFile==null&&fileInfo.getOriginalFile()!=null){
            targetFile = fileInfo.getOriginalFile();
            targetFile.renameTo(new File(targetFile.getAbsolutePath()+".temp"));
            MessageBox.getLogger().warn("开始上传文件:"+targetFile.getName());
            start = System.currentTimeMillis();
        }else if(targetFile==null){
            targetFile = new File(CodeUtil.decodeBase64(fileInfo.getFileModifier()),fileInfo.getFileName()+".temp");
            MessageBox.getLogger().warn("开始上传文件:"+targetFile.getName());
            start = System.currentTimeMillis();
        }
        if(size==0&&new File(targetFile.getAbsolutePath().substring(0,targetFile.getAbsolutePath().lastIndexOf(".temp"))).exists()){
            Object[] data = {1,fileInfo.getFileSize()};
            ctx.channel().writeAndFlush(new TextWebSocketFrame(ResultUtil.getResultString(WebFileResult.TRANSPORT_CONTINUE,data)));
            return;
        }
        if(targetFile.exists()||targetFile.createNewFile()){
            int length;
            try (FileOutputStream output = new FileOutputStream(targetFile, true)) {
                FileChannel fc = output.getChannel();
                length = msg.content().readableBytes();
                fc.force(true);
                fc.write(msg.content().nioBuffer());
                fc.close();
            }
            size +=length;
            if(size>=fileInfo.getFileSize()){
                if(size!=fileInfo.getFileSize()){
                    ctx.channel().writeAndFlush(new TextWebSocketFrame(ResultUtil.getResultString(WebFileResult.FILE_BROKEN,null))).sync();
                    System.gc();
                    targetFile.delete();
                    ctx.close();
                    return;
                }
                FileUploadCompletedEvent event = new FileUploadCompletedEvent(fileInfo);
                FloatSphere.getPluginManager().callEvent(event);
                if(event.isCancel()){
                    ctx.close();
                    return;
                }
                File comFile = new File(targetFile.getAbsolutePath().substring(0,targetFile.getAbsolutePath().lastIndexOf(".temp")));
                System.gc();
                targetFile.renameTo(comFile);
                Object[] data = progress(ctx, fileInfo);
                if(ctx.channel().isOpen()&&ctx.channel().isActive()){
                    ctx.channel().writeAndFlush(new TextWebSocketFrame(ResultUtil.getResultString(WebFileResult.UPLOAD_COMPLETED,data))).addListener(future -> {
                        if(!future.isSuccess()){
                            MessageBox.getLogger().warn(future.cause().getMessage()+'\n');
                        }
                    });
                }
                MessageBox.getLogger().warn("文件:"+targetFile.getName()+"传输耗时:"+(System.currentTimeMillis()-start)+"ms");
                ctx.close();
                return;
            }
            Object[] data = progress(ctx, fileInfo);
            if(ctx.channel().isOpen()&&ctx.channel().isActive()){
                ctx.channel().writeAndFlush(new TextWebSocketFrame(ResultUtil.getResultString(WebFileResult.TRANSPORT_CONTINUE,data))).addListener(future -> {
                    if(!future.isSuccess()){
                        MessageBox.getLogger().warn(future.cause().getMessage()+'\n');
                    }
                });
            }
        }else{
            MessageBox.getLogger().warn("wrong fileInfo: "+fileInfo.getFileName()+" channel is closed");
            ctx.channel().writeAndFlush(new TextWebSocketFrame(ResultUtil.getResultString(WebFileResult.FORBIDDEN_WS,null)));
            ctx.channel().close().addListener(future -> {
                if(future.isSuccess())
                    MessageBox.getLogger().warnTips("403 forbidden connected ws is closed");
            });
        }
    }

    private Object[] progress(ChannelHandlerContext ctx, FileInfo fileInfo) {
        float e = (float)size/(float)fileInfo.getFileSize();
        return new Object[]{e,size};
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
