package com.chbcraft.net.handlers.inbound.websocket;

import com.chbcraft.internals.components.FloatSphere;
import com.chbcraft.internals.components.MessageBox;
import com.chbcraft.internals.components.sysevent.net.ws.FileUploadCompletedEvent;
import com.chbcraft.internals.components.sysevent.net.ws.FileUploadInterruptEvent;
import com.chbcraft.net.handlers.inbound.websocket.pojo.FileInfo;
import com.chbcraft.net.tranfer.FrameUtil;
import com.chbcraft.net.tranfer.TranProtocol;
import com.chbcraft.net.util.CodeUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;


public class BinaryFrameHandler extends SimpleChannelInboundHandler<BinaryWebSocketFrame> {
    /**
     * 任务编号
     */
    private String uuid;
    /**
     * 开始任务的时间
     */
    private long s;
    /**
     * 目标文件传输后正确的大小
     */
    private long size = 0;
    /**
     * 最终的目标文件
     */
    private File tarFile = null;
    /**
     * 临时写入文件
     */
    private File tempFile = null;
    /**
     * 写入的文件管道
     */
    private FileChannel fc = null;
    /**
     * 文件传输当前的字节偏移量
     */
    private long start;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        super.channelRead(ctx, msg);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        if(uuid!=null){
            TextFrameHandler txtHandler = ctx.pipeline().get(TextFrameHandler.class);
            ChannelHandlerContext ctlCtx= txtHandler.getCtlHandlerContext(uuid);
            if(ctlCtx!=null){
                ctlCtx.close();
                txtHandler.removeCtlHandlerCtxByUUID(uuid);
                txtHandler.removeHandlerCtxByUUID(uuid);
            }
        }

        if(fc!=null&&fc.isOpen()){
            fc.close();
            FileInfo info = ctx.pipeline().get(TextFrameHandler.class).getFileInfo();
            MessageBox.getLogger().warnTips("!Abnormal connection interruption:{}",info.getFileName());
            FloatSphere.getPluginManager().callEvent(new FileUploadInterruptEvent(CodeUtil.encodeBase64(tempFile.getAbsolutePath()),s,info.getUsername(),tempFile.getName(),tarFile.getName()));
        }else{
            if(size>0&&size==start){
                FileInfo info = ctx.pipeline().get(TextFrameHandler.class).getFileInfo();
                FloatSphere.getPluginManager().callEvent(new FileUploadCompletedEvent(info));
            }
        }
        super.channelUnregistered(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, BinaryWebSocketFrame msg) throws Exception {
        if(tarFile==null&&tempFile==null) //初始化传输做的准备
            initTaskInfo(ctx);
        start+=writeBytesToTar(msg);
        ByteBuf ret = ctx.alloc().buffer();
        if(start==size){//文件完整传输完毕
            fc.close();
            Files.move(tempFile.toPath(),tarFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            MessageBox.getLogger().debug("File: {} received spend: {} ms",tarFile.getName(),System.currentTimeMillis()-s);
            FrameUtil.writeFrame(ret,TranProtocol.TASK_COMPLETED,size);
        }else if(start>size){//文件传输不一致
            fc.close();
            MessageBox.getLogger().warnTips("{}Transmission error? The file format is changed!",tarFile.getName());
            Files.delete(tarFile.toPath());
            FrameUtil.writeFrame(ret, TranProtocol.TASK_FAILED|TranProtocol.FILE_CHANGED,0 );

        }else{//传输中
            FrameUtil.writeFrame(ret, TranProtocol.TASK_RUNNING|TranProtocol.CONTINUE_TASK,start);
        }
        if (ctx.channel().isOpen()&&ctx.channel().isActive())//回显数据到对端
            ctx.writeAndFlush(new TextWebSocketFrame(ret)).addListener((future -> {
                if(ret.refCnt()>0){
                    ret.release();
                }
            }));
    }

    /**
     * 写入开始时为文件写入做初始化准备
     * @param ctx 通道上下文
     * @throws IOException io错误
     */
    public void initTaskInfo(ChannelHandlerContext ctx) throws IOException {
        FileInfo info = ctx.pipeline().get(TextFrameHandler.class).getFileInfo();
        uuid = info.getUuid();
        tarFile = info.getTarFile();
        tempFile = info.getTempFile();
        fc = FileChannel.open(tempFile.toPath(), StandardOpenOption.WRITE);
        fc.force(true);
        size = info.getFileSize();
        start = info.getFileOffSet();
        s = System.currentTimeMillis();
        info.setFileName(tarFile.getName());
        info.setFileModifier(CodeUtil.encodeBase64(tarFile.getAbsolutePath()));
    }

    /**
     * 将字节数据写入目标文件
     * @param msg 字节数据对象
     * @return 返回写入的字节数目
     */
    public int writeBytesToTar(BinaryWebSocketFrame msg){
        int s = 0;
        try {
            s = msg.content().readBytes(fc,start,msg.content().readableBytes());
            msg.release();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return s;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        MessageBox.getLogger().warn("unsafe close!");
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
