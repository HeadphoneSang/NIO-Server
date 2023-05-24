package com.chbcraft.net.handlers.inbound;

import com.chbcraft.internals.components.FloatSphere;
import com.chbcraft.internals.components.MessageBox;
import com.chbcraft.internals.components.listen.RegisteredRouter;
import com.chbcraft.internals.components.sysevent.net.http.RequestInboundEvent;
import com.chbcraft.net.handlers.router.*;
import com.chbcraft.net.handlers.outbound.HttpResponseMessage;
import com.chbcraft.net.util.RequestUtil;
import com.chbcraft.net.util.ResponseUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;

/**
 * 接受普通的HTTP请求,根绝类型去匹配路由
 * 然后去执行对应的路由方法,获得返回结果
 */
public class HttpMessageHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private static final RouterAdaptor adaptor = new RouterAdaptor();
    static {
        adaptor
                .addSorter(RegisteredRouter.RouteMethod.POST.name(), new PostRequestSorter())
                .addSorter(RegisteredRouter.RouteMethod.GET.name(), new GetRequestSorter())
                .addSorter(RegisteredRouter.RouteMethod.DELETE.name(), new DeleteRequestSorter())
                .addSorter(RegisteredRouter.RouteMethod.OPTIONS.name(), new OptionRequestSorter());
    }
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        super.channelRead(ctx,msg);
        ctx.flush();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpRequest request) throws Exception {
        if(request.uri().startsWith("/download")&&request.method()==HttpMethod.GET){
            channelHandlerContext.pipeline().addAfter("http","download",new HttpDownloadHandler());
            channelHandlerContext.fireChannelRead(request.retain());
            return;
        }
        Object ret;
        RequestInboundEvent event = new RequestInboundEvent(new HttpRequestMessage(request));
        FloatSphere.getPluginManager().callEvent(event);
        if(event.isCancel()){
            ResponseUtil.send403Forbidden(channelHandlerContext);
            return;
        }
        RequestSorter sorter = adaptor.getSorter(request.method().name());
        ret = sorter.handlerRequest(channelHandlerContext,request);
        if(ret instanceof RequestUtil.HandlerResultState){
            /**
             * 分拣状态结果,返回请求
             */
            RequestUtil.HandlerResultState state = (RequestUtil.HandlerResultState) ret;
            switch (state){
                case NO_MATCHES:{
                    RequestUtil.send404State(channelHandlerContext);
                    MessageBox.getLogger().log("Invalid "+request.method()+" "+request.uri());
                    break;
                }
                case NO_RESULT:{
                    RequestUtil.send204States(channelHandlerContext);
                    break;

                }case RUNTIME_ERROR:{
                    RequestUtil.send500State(channelHandlerContext);
                    break;
                }
                case FORMAT_ERROR:{
                    RequestUtil.send510State(channelHandlerContext);
                    break;
                }
                case UNSUPPORTED_METHOD:{
                    RequestUtil.send405State(channelHandlerContext);
                    MessageBox.getLogger().log("Unsupported Method "+request.method());
                    break;
                }
                case OPTIONS_REQUEST:{
                    RequestUtil.sendOptionsResponse(channelHandlerContext);
                    break;
                }
            }
        }
        else if(ret instanceof HttpResponseMessage){
            /**
             * 处理请求消息类型的路由执行结果
             */
            HttpResponseMessage message = (HttpResponseMessage) ret;
            message.setMethod(RegisteredRouter.RouteMethod.valueOf(request.method().name()));
            message.setRoute(request.uri());
            //写入管道
            channelHandlerContext.pipeline().write(message);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
    }
}
