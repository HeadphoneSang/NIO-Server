package com.chbcraft.net.handlers.router;

import com.chbcraft.net.util.RequestUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

/**
 * 不支持的请求模式默认分拣器
 */
public class UndefinedRequestSorter extends RequestSorter{
    @Override
    public Object handlerRequest(ChannelHandlerContext ctx, FullHttpRequest request) {
        return super.handlerRequest(ctx, request);
    }

    @Override
    protected Object handler(ChannelHandlerContext ctx, FullHttpRequest request) {
        return RequestUtil.HandlerResultState.UNSUPPORTED_METHOD;
    }
}
