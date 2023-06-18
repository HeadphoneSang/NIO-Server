package com.chbcraft.net.handlers.outbound;

import com.chbcraft.internals.components.FloatSphere;
import com.chbcraft.internals.components.listen.Resource;
import com.chbcraft.internals.components.sysevent.net.http.ResponseOutboundEvent;
import com.chbcraft.net.util.ImageUtil;
import com.chbcraft.net.util.RequestUtil;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import javax.activation.FileTypeMap;
import javax.activation.MimetypesFileTypeMap;
import java.io.*;

public class HttpResourceResponseHandler extends ChannelOutboundHandlerAdapter {

    private static final MimetypesFileTypeMap fileTypeMap = new MimetypesFileTypeMap();

    private static final File miniCache = new File(FloatSphere.getRootPath(),"cache");

    static {
        fileTypeMap.addMimeTypes("application/atom+xml atom");
        fileTypeMap.addMimeTypes("application/msword doc dot");
        fileTypeMap.addMimeTypes("application/mspowerpoint ppt pot");
        fileTypeMap.addMimeTypes("application/msexcel xls");
        fileTypeMap.addMimeTypes("application/pdf pdf");
        fileTypeMap.addMimeTypes("application/rdf+xml rdf rss");
        fileTypeMap.addMimeTypes("application/x-vnd.openxmlformat docx docm dotx dotm");
        fileTypeMap.addMimeTypes("application/x-vnd.openxmlformat xlsx xlsm");
        fileTypeMap.addMimeTypes("application/x-vnd.openxmlformat pptx pptm potx");
        fileTypeMap.addMimeTypes("application/x-javascript js");
        fileTypeMap.addMimeTypes("application/x-rar-compressed rar");
        fileTypeMap.addMimeTypes("application/x-textedit bat cmd");
        fileTypeMap.addMimeTypes("application/zip zip");
        fileTypeMap.addMimeTypes("audio/mpeg mp3");
        fileTypeMap.addMimeTypes("image/bmp bmp");
        fileTypeMap.addMimeTypes("image/gif gif");
        fileTypeMap.addMimeTypes("image/jpeg jpg jpeg jpe");
        fileTypeMap.addMimeTypes("image/png png");
        fileTypeMap.addMimeTypes("text/css css");
        fileTypeMap.addMimeTypes("text/csv csv");
        fileTypeMap.addMimeTypes("text/html htm html");
        fileTypeMap.addMimeTypes("text/xml xml");
        fileTypeMap.addMimeTypes("video/quicktime qt mov moov");
        fileTypeMap.addMimeTypes("video/mpeg mpeg mpg mpe mpv vbs mpegv");
        fileTypeMap.addMimeTypes("video/msvideo avi");
        fileTypeMap.addMimeTypes("video/mp4 mp4");
        fileTypeMap.addMimeTypes("video/ogg ogg");
        FileTypeMap.setDefaultFileTypeMap(fileTypeMap);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if(!(msg instanceof HttpResponseMessage)){
            super.write(ctx,msg,promise);
            return;
        }
        HttpResponseMessage message = (HttpResponseMessage) msg;//没有cancel才去json字符串
        if(message.hasTag(Resource.class)){
            if(message.getOriginalBody() instanceof File){
                File res = (File) message.getOriginalBody();
                if(!res.exists()){
                    RequestUtil.send404State(ctx);
                    return;
                }
                HttpHeaders headers = getContentTypeHeader(res);
                Resource tag = message.getTag(Resource.class);
                if(tag.compress()&&headers.contains(HttpHeaderNames.CONTENT_TYPE)&&headers.get(HttpHeaderNames.CONTENT_TYPE).startsWith("image/")){

                    File cache = new File(miniCache,res.getName());
                    if(cache.exists()){//如果存在缩略图的缓存，直接写缩略图缓存
                        res = cache;
                    }else {
                        ByteArrayOutputStream bs = ImageUtil.compressPic(res);//压缩后的字节数据
                        if((cache.getParentFile().exists()||cache.getParentFile().mkdirs())&&cache.createNewFile()){
                            ImageUtil.saveFileToCache(cache,bs);
                        }
                        headers.set(HttpHeaderNames.CONTENT_LENGTH,bs.size());
                        writeResponse(ctx,ctx.alloc().buffer().writeBytes(bs.toByteArray()),headers,message);
                        bs.close();
                        return;
                    }
                }
                RandomAccessFile raf = new RandomAccessFile(res,"r");
                headers.set(HttpHeaderNames.CONTENT_LENGTH,raf.length());
                writeResponse(ctx,new DefaultFileRegion(raf.getChannel(),0,raf.length()),headers,message);
            }else{
                super.write(ctx,msg,promise);
            }
        }
    }



    /**
     * 向对端回写响应信息
     * @param ctx 连接通道上下文
     * @param retObj 回写数据内容
     * @param headers 响应头
     */
    public void writeResponse(ChannelHandlerContext ctx,Object retObj,HttpHeaders headers,HttpResponseMessage message){
        long count = 0;
        if(retObj instanceof DefaultFileRegion){
            DefaultFileRegion dfr = (DefaultFileRegion) retObj;
            count = dfr.count();
            if(dfr.count()>5*ImageUtil.MB&&ctx.pipeline().get("idleStateHandler")!=null){
                ctx.pipeline().remove("idleStateHandler");
                ctx.pipeline().remove("timeoutHandler");
            }
        }
        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1,HttpResponseStatus.OK,headers);
        message.setResponse(response);
        FloatSphere.getPluginManager().callEvent(new ResponseOutboundEvent(message));
        ctx.write(response);
        ctx.write(retObj);
        long finalCount = count;
        ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT).addListener(future -> {
            if (future.isSuccess()&& finalCount >(5*ImageUtil.MB)) {
                ctx.close();
            }
        });
    }



    private HttpHeaders getContentTypeHeader(File file) {
        HttpHeaders headers = new DefaultHttpHeaders();
        String contentType = fileTypeMap.getContentType(file);
        if (contentType.equals("text/plain")) {
            //由于文本在浏览器中会显示乱码，此处指定为utf-8编码
            contentType = "text/plain;charset=utf-8";
        }
        headers.set(HttpHeaderNames.CONTENT_TYPE, contentType);
        return headers;
    }

}
