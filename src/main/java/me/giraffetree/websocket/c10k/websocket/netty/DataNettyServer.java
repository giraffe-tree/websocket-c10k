package me.giraffetree.websocket.c10k.websocket.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.InetSocketAddress;

@Slf4j
@Component
public class DataNettyServer {

    @Autowired
    private DataChannelHandler dataChannelHandler;

    private static final String WEB_SOCKET_PROTOCOL = "WebSocket";
    private static final String THREAD_NAME_PREFIX = "nettyStart-";

    @Value("${ws.port}")
    private int port;
    @Value("${ws.path}")
    private String webSocketPath;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workGroup;

    /**
     * 启动
     */
    private void start() throws InterruptedException {
        this.bossGroup = new NioEventLoopGroup();
        this.workGroup = new NioEventLoopGroup(16);
        ServerBootstrap bootstrap = new ServerBootstrap();
        // bossGroup辅助客户端的tcp连接请求, workGroup负责与客户端之前的读写操作
        bootstrap.group(this.bossGroup, this.workGroup);
        // 设置NIO类型的channel
        bootstrap.channel(NioServerSocketChannel.class);
        // 设置监听端口
        bootstrap.localAddress(new InetSocketAddress(this.port));
        // 连接到达时会创建一个通道
        bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) {
                // 流水线管理通道中的处理程序（Handler），用来处理业务
                // webSocket协议本身是基于http协议的，所以这边也要使用http编解码器
                ch.pipeline().addLast(new HttpServerCodec());
                ch.pipeline().addLast(new ObjectEncoder());
                // 以块的方式来写的处理器
                ch.pipeline().addLast(new ChunkedWriteHandler());
                //http数据在传输过程中是分段的，HttpObjectAggregator可以将多个段聚合
                ch.pipeline().addLast(new HttpObjectAggregator(8192));
                ch.pipeline().addLast(new WebSocketServerProtocolHandler(webSocketPath, WEB_SOCKET_PROTOCOL, true, 65536 * 10));
                // 自定义的handler，处理业务逻辑
                ch.pipeline().addLast(dataChannelHandler);
            }
        });
        // 配置完成，开始绑定server，通过调用sync同步方法阻塞直到绑定成功
        ChannelFuture channelFuture = bootstrap.bind().sync();
        // 对关闭通道进行监听
        channelFuture.channel().closeFuture().sync();
    }

    @PreDestroy
    public void destroy() throws InterruptedException {
        if (this.bossGroup != null) {
            this.bossGroup.shutdownGracefully().sync();
        }
        if (this.workGroup != null) {
            this.workGroup.shutdownGracefully().sync();
        }
    }

    @PostConstruct
    public void init() {
        //需要开启一个新的线程来执行netty server 服务器
        Thread initThread = new Thread(() -> {
            try {
                start();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        initThread.setName(THREAD_NAME_PREFIX + "-server");
        initThread.start();
    }
}
