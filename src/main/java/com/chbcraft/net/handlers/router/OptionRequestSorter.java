package com.chbcraft.net.handlers.router;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

import static com.chbcraft.net.util.RequestUtil.HandlerResultState.OPTIONS_REQUEST;

public class OptionRequestSorter  extends RequestSorter{
    @Override
    public Object handlerRequest(ChannelHandlerContext ctx, FullHttpRequest request) {
        return super.handlerRequest(ctx, request);
    }

    @Override
    protected Object handler(ChannelHandlerContext ctx, FullHttpRequest request) {
        return OPTIONS_REQUEST;
    }
}
