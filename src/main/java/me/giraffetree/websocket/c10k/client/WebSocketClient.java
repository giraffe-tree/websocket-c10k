package me.giraffetree.websocket.c10k.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class WebSocketClient {

    private final URI uri;
    private Channel ch;
    private static final String PING = "{\"seq\":\"0\",\"cmd\":\"ping\",\"response\":{\"code\":200}}";
    private static final EventLoopGroup group = new NioEventLoopGroup();

    public WebSocketClient(final String uri) {
        this.uri = URI.create(uri);
    }


    public static void main(String[] args) {
        if (args.length != 4) {
            System.err.println("args error");
            System.err.println("format: {count} {heartBeatCount} {heartBeatDurationMills} {uri}");
            System.err.println("example: 1000 20 10000 ws://localhost:8011/websocket/handshake/");
            return;
        }
        int count;
        int heartBeatCount;
        long heartBeatDurationMills;
        try {
            count = Integer.parseInt(args[0]);
            heartBeatCount = Integer.parseInt(args[1]);
            heartBeatDurationMills = Long.parseLong(args[2]);
        } catch (NumberFormatException e) {
            System.err.println("args error");
            System.err.println("format: {count} {heartBeatCount} {heartBeatDurationMills} {uri}");
            System.err.println("example: 1000 20 10000 ws://localhost:8011/websocket/handshake/");
            return;
        }
        String urlPrefix = args[3];
        ThreadLocalRandom cur = ThreadLocalRandom.current();
        ArrayList<WebSocketClient> list = new ArrayList<>(count);
        int randomPrefix = cur.nextInt(10000);
        System.out.println(String.format("[connect] start connect - count:%d", count));
        long startConnectMills = System.currentTimeMillis();
        for (int i = 1; i <= count; i++) {
            String uri = urlPrefix + "?id=" + randomPrefix + "_" + i;
            WebSocketClient webSocketClient = new WebSocketClient(uri);
            try {
                webSocketClient.open();
            } catch (Exception e) {
                System.err.println(String.format("[connect] error connect - cur:%d msg:%s", i, e.getLocalizedMessage()));
                continue;
            }
            list.add(webSocketClient);
        }
        long endConnectMills = System.currentTimeMillis();
        System.out.println(String.format("[connect] success connect - %d/%d cost:%dms", list.size(), count, endConnectMills - startConnectMills));
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("[ close ] try to close all connection... size:" + list.size());
            int c = 0;
            for (WebSocketClient webSocketClient : list) {
                try {
                    webSocketClient.close();
                    c++;
                } catch (InterruptedException e) {
                    // do nothing
                }
            }
            System.out.println("[ close ] close size:" + c);
        }));

        for (int i = 1; i <= heartBeatCount; i++) {
            System.out.println(String.format("[heartBeat] start heartBeat loop:%d size: %d", i, list.size()));
            long l1 = System.currentTimeMillis();
            list.forEach(x -> {
                try {
                    x.eval(PING);
                } catch (IOException e) {
                    try {
                        x.close();
                    } catch (InterruptedException interruptedException) {
                        // do nothing
                        System.exit(0);
                    }
                }
            });
            long l2 = System.currentTimeMillis();
            long cost = l2 - l1;
            System.out.println(String.format("[heartBeat] end heartBeat cost:%dms", cost));
            if (cost > heartBeatDurationMills) {
                continue;
            }
            try {
                Thread.sleep(heartBeatDurationMills - cost);
            } catch (InterruptedException e) {
                System.exit(0);
            }
        }

    }

    public void open() throws Exception {
        Bootstrap b = new Bootstrap();
        String protocol = uri.getScheme();
        if (!"ws".equals(protocol)) {
            throw new IllegalArgumentException("Unsupported protocol: " + protocol);
        }

        // Connect with V13 (RFC 6455 aka HyBi-17). You can change it to V08 or V00.
        // If you change it to V00, ping is not supported and remember to change
        // HttpResponseDecoder to WebSocketHttpResponseDecoder in the pipeline.
        final WebSocketClientHandler handler =
                new WebSocketClientHandler(
                        WebSocketClientHandshakerFactory.newHandshaker(
                                uri, WebSocketVersion.V13, null, false, HttpHeaders.EMPTY_HEADERS, 1280000));

        b.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast("http-codec", new HttpClientCodec());
                        pipeline.addLast("aggregator", new HttpObjectAggregator(65536));
                        pipeline.addLast("ws-handler", handler);
                    }
                });

        ch = b.connect(uri.getHost(), uri.getPort()).sync().channel();
        handler.handshakeFuture().sync();
    }

    public void close() throws InterruptedException {
        ch.writeAndFlush(new CloseWebSocketFrame());
        ch.closeFuture().sync();
    }

    public void eval(final String text) throws IOException {
        ch.writeAndFlush(new TextWebSocketFrame(text));
    }
}