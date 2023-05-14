package com.chbcraft.net;

import com.chbcraft.internals.components.FloatSphere;
import com.chbcraft.internals.components.MessageBox;
import com.chbcraft.internals.components.enums.SectionName;
import com.chbcraft.net.handlers.inbound.HttpDownloadHandler;
import com.chbcraft.net.handlers.inbound.HttpMessageHandler;
import com.chbcraft.net.handlers.inbound.SwitchProtocolAdaptor;
import com.chbcraft.net.handlers.inbound.TimeOutHandler;
import com.chbcraft.net.handlers.inbound.websocket.BinaryFrameHandler;
import com.chbcraft.net.handlers.inbound.websocket.TextFrameHandler;
import com.chbcraft.net.handlers.outbound.ResponseWriterHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.Future;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

public class NioHttpAcceptor extends HttpAcceptor{

    private final MessageBox logger = MessageBox.getLogger();
    private final ServerBootstrap server;
    private final EventLoopGroup loopGroup;
    public NioHttpAcceptor(int port) {
        super(port);
        logger.log("server is initializing");
        this.server = new ServerBootstrap();
        this.loopGroup = new NioEventLoopGroup();
        long s = Long.parseLong(FloatSphere.getProperties().getString(SectionName.TIME_OUT.value()));
        this.server.group(loopGroup).
                channel(NioServerSocketChannel.class).
                option(ChannelOption.WRITE_BUFFER_WATER_MARK,new WriteBufferWaterMark(1024*1024,1024*1024)).
                localAddress(new InetSocketAddress(port)).childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline()
                        .addLast("idleStateHandler",new IdleStateHandler(s,s,s, TimeUnit.SECONDS))
                        .addLast("timeoutHandler",new TimeOutHandler())
                        .addLast("codec",new HttpServerCodec())
                        .addLast("aggregator",new HttpObjectAggregator(1024*1024))
                        .addLast("adaptor",new SwitchProtocolAdaptor())
                        .addLast("http",new HttpMessageHandler())
//                        .addLast("downloadHandler",new HttpDownloadHandler())
                        .addLast("messageDeliver",new ResponseWriterHandler());
            }
        });
        logger.log("server is initialized");
    }

    @Override
    void accepting() {
        logger.log("launching.....");
        server.bind().addListener((ChannelFutureListener) future ->
                logger.log("server is listening on "+future.channel().localAddress()));

    }

    @Override
    public Future<?> shutdown() {
        return loopGroup.shutdownGracefully();
    }
}
