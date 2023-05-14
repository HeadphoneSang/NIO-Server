package com.chbcraft.net.handlers.inbound;

import com.chbcraft.internals.components.FloatSphere;
import com.chbcraft.internals.components.MessageBox;
import com.chbcraft.net.handlers.inbound.websocket.event.FileDownloadCompletedEvent;
import com.chbcraft.net.handlers.inbound.websocket.pojo.FileInfo;
import com.chbcraft.net.util.CodeUtil;
import com.chbcraft.net.util.RequestUtil;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class HttpDownloadHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private String downloadFilePath = null;

    private MessageBox log = MessageBox.getLogger();

    public HttpDownloadHandler(){
        super(false);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ctx.pipeline().remove(IdleStateHandler.class);
        ctx.pipeline().remove(TimeOutHandler.class);
        ctx.pipeline().remove(SwitchProtocolAdaptor.class);
        ctx.pipeline().addAfter("aggregator","writerChunk",new ChunkedWriteHandler());
        super.channelRead(ctx,msg);
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ctx.close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        String uri = request.uri();

        MessageBox.getLogger().log("init download");
        long start = System.currentTimeMillis();
        if (uri.startsWith("/download") && request.method().equals(HttpMethod.GET)) {
            ctx.pipeline().remove("http");
            int end = uri.lastIndexOf("/");
            if(end==0){
                RequestUtil.send405State(ctx);
                return;
            }
            String username = uri.substring(end+1);
            uri = uri.substring(0,end);
            end = uri.lastIndexOf("/");
            if(end==0){
                RequestUtil.send405State(ctx);
                return;
            }
            username = CodeUtil.decodeURL(username);
            String modifier = uri.substring(end+1);
            downloadFilePath = CodeUtil.decodeBase64(modifier);
            File file = new File(downloadFilePath);
            try {
                final RandomAccessFile raf = new RandomAccessFile(file, "r");
                long fileLength = raf.length();
                HttpResponse response = new DefaultHttpResponse(HTTP_1_1, HttpResponseStatus.OK);
                response.headers().set(HttpHeaderNames.CONTENT_LENGTH, fileLength);
                response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/octet-stream");
                response.headers().add(HttpHeaderNames.CONTENT_DISPOSITION, String.format("attachment; filename=\"%s\"", file.getName()));
                ctx.write(response);
                ChannelFuture sendFileFuture = ctx.write(new DefaultFileRegion(raf.getChannel(), 0, fileLength), ctx.newProgressivePromise());
                String finalUsername = username;
                sendFileFuture.addListener(new ChannelProgressiveFutureListener() {
                    @Override
                    public void operationComplete(ChannelProgressiveFuture future)
                            throws Exception {
                        raf.close();
                        FileInfo info = new FileInfo();
                        info.setFileModifier(modifier);
                        info.setUsername(finalUsername);
                        info.setFileName(file.getName());
                        MessageBox.getLogger().log("download spend : "+(System.currentTimeMillis()-start) +"ms");
                        FileDownloadCompletedEvent event = new FileDownloadCompletedEvent(info);
                        FloatSphere.getPluginManager().callEvent(event);
                        if(event.isCancel()){
                            ctx.channel().close();
                        }
                    }

                    @Override
                    public void operationProgressed(ChannelProgressiveFuture future,
                                                    long progress, long total) throws Exception {
//                        if (total < 0) {
//                            log.warn("file {"+file.getName()+"} transfer progress: "+progress);
//                        } else {
//                            log.log("file {"+file.getName()+"} transfer progress: {"+progress+"}/{"+total+"}");
//                        }
                    }

                });
                ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
            } catch (FileNotFoundException e) {
                log.warn("file {"+file.getPath()+"} not found");
                RequestUtil.send404State(ctx);
            } catch (IOException e) {
                log.warn("file {"+file.getName()+"} has a IOException: {"+e.getMessage()+"}");
                RequestUtil.send404State(ctx);
            }
        } else {
            RequestUtil.send404State(ctx);
        }
    }
}
