package com.chbcraft.net.handlers.inbound.websocket;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.chbcraft.internals.components.FloatSphere;
import com.chbcraft.internals.components.MessageBox;
import com.chbcraft.net.handlers.inbound.OperationInbound;
import com.chbcraft.net.handlers.inbound.websocket.event.UploadFIleInfoEvent;
import com.chbcraft.net.handlers.inbound.websocket.event.WebSocketOpenEvent;
import com.chbcraft.net.handlers.inbound.websocket.pojo.WebFileResult;
import com.chbcraft.net.handlers.inbound.websocket.utils.ResultUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;

import java.util.Map;

public class TextFrameHandler extends OperationInbound<TextWebSocketFrame> {
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

        if(evt == WebSocketServerProtocolHandler.ServerHandshakeStateEvent.HANDSHAKE_COMPLETE){
            FloatSphere.getPluginManager().callEvent(new WebSocketOpenEvent());
        }else{
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        super.channelRead(ctx, msg);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        String text = msg.text();
        Map<String,String> map = JSONObject.parseObject(text,new TypeReference<Map<String,String>>(){});
        getFileInfo().setFileModifier(map.get("modifier"));
        getFileInfo().setFileName(map.get("fileName"));
        getFileInfo().setFileSize(Long.parseLong(map.get("fileSize")));
        getFileInfo().setUsername(map.get("username"));
        UploadFIleInfoEvent event = new UploadFIleInfoEvent(getFileInfo());
        FloatSphere.getPluginManager().callEvent(event);
        if(event.isCancel()||getFileInfo().getFileModifier()==null||getFileInfo().getFileModifier().equals("")){
            ctx.channel().writeAndFlush(new TextWebSocketFrame(ResultUtil.getResultString(WebFileResult.FORBIDDEN_WS,null)));
            MessageBox.getLogger().warn("wrong fileInfo: "+getFileInfo().getFileName()+" channel is closed"+'\n');
            ctx.channel().close().addListener((future)->{
               if(future.isSuccess()){
                   MessageBox.getLogger().log("403 forbidden connected ws is closed");
               }
            });
            return;
        }
        /**
         * 解码filemodifier
         * 写入file
         */
        ctx.channel().writeAndFlush(new TextWebSocketFrame(ResultUtil.getResultString(WebFileResult.INIT_OK,null)));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }


}
