package com.chbcraft.net.handlers.router;

import com.chbcraft.net.util.RequestUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

public abstract class RequestSorter {
    /**
     * 实际调用接口
     * @param ctx 远程连接的连接对话
     * @param request 请求的完整请求内容对象
     * @return 返回请求结果或者是请求状态
     */
    public Object handlerRequest(ChannelHandlerContext ctx, FullHttpRequest request){
        Object ret = handler(ctx, request);
        return ret==null? RequestUtil.HandlerResultState.NO_MATCHES:ret;
    }

    /**
     * 处理Request请求,分拣请求地址到对应的路由
     * @param ctx 远程连接的连接对话
     * @param request 请求的完整请求内容对象
     * @return 返回请求结果或者是请求状态
     */
    protected abstract Object handler(ChannelHandlerContext ctx, FullHttpRequest request);

}
