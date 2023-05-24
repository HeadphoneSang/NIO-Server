package com.chbcraft.net.handlers.inbound.websocket;

import com.alibaba.fastjson.JSON;
import com.chbcraft.internals.components.FloatSphere;
import com.chbcraft.net.handlers.inbound.OperationInbound;
import com.chbcraft.internals.components.sysevent.net.ws.WebSocketOpenEvent;
import com.chbcraft.net.tranfer.*;
import com.chbcraft.net.tranfer.frameHandlers.*;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class TextFrameHandler extends OperationInbound<TextWebSocketFrame> {

    private static final FrameAdaptor ctrlAdaptor = new CtrlFrameAdaptor();

    /**
     * 用来存储上传任务的表
     * 键是UUID
     * 值是上传任务的数据管道上下文对象
     */
    private static final ConcurrentHashMap<String,ChannelHandlerContext> TASK_MAP = new ConcurrentHashMap<>();

    static{
        FrameHandler createHandler = new TaskCreateHandler();
        ctrlAdaptor
                .addFrameHandler(TranProtocol.TASK_CREATING,new TaskCreatingHandler()
                    .addFrameHandler(TranProtocol.NEW_TASK,createHandler)
                    .addFrameHandler(TranProtocol.CONTINUE_TASK,createHandler)
                    .addFrameHandler(TranProtocol.CREATE_CTRL,new ControlTaskCreateHandler())
                )
                .addFrameHandler(TranProtocol.TASK_RUNNING,new TaskRunningHandler()
                    .addFrameHandler(TranProtocol.CANCEL_CONTINUE,new CancelTaskHandler())
                )
                .addFrameHandler(TranProtocol.TASK_COMPLETED,new TaskCompletedHandler());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {

        super.channelInactive(ctx);
    }

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
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) {
        TransferFrame frame = JSON.parseObject(msg.text(),TransferFrame.class);
        ByteBuf ret = ctx.alloc().buffer();
        try {
            ctrlAdaptor.handler(frame,ret,ctx);
        } catch (Exception e) {
            FrameUtil.writeFrame(ret, TranProtocol.TASK_FAILED, 0);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }

    /**
     * 记录上传数据任务通道
     * @param uuid 唯一标识
     * @param ctx 上下文
     * @return 返回记录是否成功
     */
    public boolean putCtx(String uuid,ChannelHandlerContext ctx){
        if(TASK_MAP.contains(uuid))
            return false;
        TASK_MAP.put(uuid, ctx);
        return true;
    }

    /**
     * 根据uuid获取到上传任务数据连接的上下文对象
     * @param uuid 任务的唯一标识符
     * @return 返回null或者是上下文对象
     */
    public ChannelHandlerContext getHandlerContext(String uuid){
        return TASK_MAP.get(uuid);
    }

    public void removeHandlerCtxByUUID(String uuid){
        TASK_MAP.remove(uuid);
    }
}
