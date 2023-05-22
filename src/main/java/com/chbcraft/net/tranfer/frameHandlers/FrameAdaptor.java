package com.chbcraft.net.tranfer.frameHandlers;

import com.chbcraft.internals.components.MessageBox;
import com.chbcraft.net.tranfer.TransferFrame;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.util.HashMap;
import java.util.Map;

public abstract class FrameAdaptor implements FrameHandler{
    protected final Map<Integer,FrameHandler> handlers = new HashMap<>();

    /**
     * 添加对应协议的处理器
     * @param protocol 协议标志码
     * @param handler 处理器
     * @return 返回自己
     */
    public FrameAdaptor addFrameHandler(int protocol,FrameHandler handler){
        handlers.put(protocol,handler);
        return this;
    }

    @Override
    public void handler(TransferFrame frame, ByteBuf ret, ChannelHandlerContext ctx) {
        try{
            this.handlerSub(frame,ret,ctx);
        }catch (Throwable e){
            e.printStackTrace();
        }finally {
            if(ret!=null&&ret.refCnt()>0){
                ret.release();
            }
        }
    }

    protected abstract void handlerSub(TransferFrame frame,ByteBuf ret, ChannelHandlerContext ctx) throws Exception;
}
