package com.chbcraft.net.tranfer.frameHandlers;

import com.chbcraft.internals.components.MessageBox;
import com.chbcraft.net.handlers.inbound.OperationInbound;
import com.chbcraft.net.handlers.inbound.websocket.TextFrameHandler;
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

public class TaskCreateHandler implements FrameHandler{
    @Override
    public void handler(TransferFrame frame, ByteBuf ret, ChannelHandlerContext ctx) throws Exception {
        File tempFile = new File(frame.getAddition().get("path")+".tmp");
        File tarFile = new File((String) frame.getAddition().get("path"));
        OperationInbound<?> h = null;
        if(ctx.handler() instanceof OperationInbound){
            h = ((OperationInbound<?>)ctx.handler());
            h.getFileInfo().setFileSize(frame.getData());
            h.getFileInfo().setTempFile(tempFile);
            h.getFileInfo().setTarFile(tarFile);
            h.getFileInfo().setUsername(frame.getAddition().get("username").toString());
            h.getFileInfo().setUuid(String.valueOf(frame.getAddition().get("uuid")));
            MessageBox.getLogger().debug("上传任务数据连接建立中:{}",tarFile.getName());
        }
        if(tarFile.exists()){//目标文件已经存在了,无需上传直接返回文件存在
            FrameUtil.writeFrame(ret, TranProtocol.TASK_CREATING|TranProtocol.FILE_EXIST,0);
        }
        else if(tempFile.exists()){//临时文件已经存在了,续传
            BasicFileAttributes attrs = Files.readAttributes(tempFile.toPath(),BasicFileAttributes.class);
            if(attrs.size()<frame.getData()){
                ((TextFrameHandler)ctx.handler()).putCtx(String.valueOf(frame.getAddition().get("uuid")),ctx);
                FrameUtil.writeFrame(ret,TranProtocol.TASK_CREATING|TranProtocol.CONTINUE_TASK,attrs.size());
                if(h!=null)
                    h.getFileInfo().setFileOffSet(attrs.size());
            }else if(attrs.size()==frame.getData()){
                Files.move(tempFile.toPath(),tarFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                FrameUtil.writeFrame(ret,TranProtocol.TASK_COMPLETED,attrs.size());
            }else{
                FrameUtil.writeFrame(ret,TranProtocol.TASK_FAILED,0);
            }
        }else if(tempFile.createNewFile()){//临时文件目标文件都不存在，直接建立新文件,创建新任务
            ((TextFrameHandler)ctx).putCtx(String.valueOf(frame.getAddition().get("uuid")),ctx);
            FrameUtil.writeFrame(ret,TranProtocol.TASK_CREATING|TranProtocol.NEW_TASK,0);
        }else{//异常错误,未知
            FrameUtil.writeFrame(ret,TranProtocol.TASK_FAILED,0);
        }
//        if(!ret.hasArray()){
//            MessageBox.getLogger().log(ret.readableBytes());
//        }
        ctx.writeAndFlush(new TextWebSocketFrame(ret));
    }
}
