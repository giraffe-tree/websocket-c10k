/*
 * Copyright 2020 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package me.giraffetree.websocket.c10k.websocket.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class StompWebSocketChatServer {

    @SuppressWarnings("AlibabaThreadPoolCreation")
    private final ExecutorService executorService = Executors.newFixedThreadPool(1, r -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        t.setName("start-thread-%d");
        return t;
    });

    static final int PORT = Integer.parseInt(System.getProperty("port", "8080"));

    public void start(final int port) throws Exception {
        NioEventLoopGroup boosGroup = new NioEventLoopGroup(1);
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap()
                    .group(boosGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new StompWebSocketChatServerInitializer("/chat"));
            bootstrap.bind(port).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    System.out.println("Open your web browser and navigate to http://127.0.0.1:" + PORT + '/');
                } else {
                    System.out.println("Cannot start server, follows exception " + future.cause());
                }
            }).channel().closeFuture().sync();
        } finally {
            boosGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public void startServer() {
        executorService.execute(() -> {
            try {
                new StompWebSocketChatServer().start(PORT);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

    }

}
