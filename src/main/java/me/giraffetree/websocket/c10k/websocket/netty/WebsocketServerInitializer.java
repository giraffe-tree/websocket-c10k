package me.giraffetree.websocket.c10k.websocket.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import me.giraffetree.websocket.c10k.websocket.netty3.MyWebsocketHandler;

/**
 * @author GiraffeTree
 * @date 2021/3/25 16:12
 */
public class WebsocketServerInitializer extends ChannelInitializer<SocketChannel> {

    private final String path;

    public WebsocketServerInitializer(String path) {
        this.path = path;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        //因为websocket是基于http，所以要添加http的编码和解码器
        ch.pipeline().addLast(new HttpServerCodec());

        //以块的方式写，添加ChunkedWriteHandler处理器
        ch.pipeline().addLast(new ChunkedWriteHandler());

        //http数据在传输的过程是分段，HttpObjectAggregator可以将多个段聚合
        //当浏览器发送大量数据时，就会发出多次
        ch.pipeline().addLast(new HttpObjectAggregator(8192));

        //说明:
        // 1，对于websocket的数据是以帧（Frame）的形式传递的
        // 2，WebSocketServerProtocolHandler核心功能是将http协议升级为ws协议，保持长连接
        ch.pipeline().addLast(new WebSocketServerProtocolHandler("/test"));
        ch.pipeline().addLast(new MyWebsocketHandler());
//        ch.pipeline()
//                .addLast(new HttpServerCodec())
//                .addLast(new HttpObjectAggregator( 65535))
//                .addLast(new WebSocketServerProtocolHandler(path))
//        .addLast(new WebsocketFrameHandler())
//        ;
    }

}
