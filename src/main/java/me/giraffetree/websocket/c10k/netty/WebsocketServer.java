package me.giraffetree.websocket.c10k.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioChannelOption;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.ImmediateEventExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author GiraffeTree
 * @date 2021/3/25 15:57
 */
@Slf4j
@Component
public class WebsocketServer {

    @SuppressWarnings("AlibabaThreadPoolCreation")
    private final ExecutorService executorService = Executors.newFixedThreadPool(1, r -> {
        Thread t = new Thread(r);
        t.setName("start-thread-%d");
        return t;
    });

    @Value("${ws.port}")
    private Integer port;

    @Value("${ws.path}")
    private String path = "/websocket/handshake/";

    public void start(final int port) throws Exception {
        ChannelGroup channelGroup = new DefaultChannelGroup(ImmediateEventExecutor.INSTANCE);
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new WebsocketServerInitializer(channelGroup, path))
                    // child 就是 与客户端连接的 socketChannel 而不是 serverSocketChannel
//                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(NioChannelOption.SO_KEEPALIVE, true)
            ;
            bootstrap.bind(port).addListener(
                    (ChannelFutureListener) future -> {
                        if (future.isSuccess()) {
                            log.info("listen websocket on port:{} path:{}", port, path);
                        } else {
                            log.error("listen websocket on port:{}, follows exception:{} ", port, future.cause().getLocalizedMessage());
                        }
                    }).channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        new WebsocketServer().start(8011);
    }

    @PostConstruct
    public void startServer() throws Exception {
        executorService.execute(() -> {
            try {
                start(port);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

    }
}
