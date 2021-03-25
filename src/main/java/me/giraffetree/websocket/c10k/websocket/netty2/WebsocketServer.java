package me.giraffetree.websocket.c10k.websocket.netty2;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import me.giraffetree.websocket.c10k.websocket.netty.StompWebSocketChatServer;
import me.giraffetree.websocket.c10k.websocket.netty.StompWebSocketChatServerInitializer;
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
        t.setDaemon(false);
        t.setName("start-thread-%d");
        return t;
    });

    @Value("${ws.port}")
    private Integer port;

    @Value("${ws.path}")
    private String path;

    public void start(final int port) throws Exception {
        NioEventLoopGroup boosGroup = new NioEventLoopGroup(1);
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap()
                    .group(boosGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new WebsocketServerInitializer(path));
            bootstrap.bind(port).addListener(
                    (ChannelFutureListener) future -> {
                        if (future.isSuccess()) {
                            log.info("listen websocket on port:{} path:{}", port, path);
                        } else {
                            log.error("listen websocket on port:{}, follows exception:{} ", port, future.cause().getLocalizedMessage());
                        }
                    }).channel().closeFuture().sync();
        } finally {
            boosGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }


    public static void main(String[] args) throws Exception {
        new WebsocketServer().start(8010);

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
