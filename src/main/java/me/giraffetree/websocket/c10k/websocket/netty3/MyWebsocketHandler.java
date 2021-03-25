package me.giraffetree.websocket.c10k.websocket.netty3;

import java.time.LocalDateTime;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

/**
 * Created by 王梦思 on 2020/9/16.
 * <p/>
 */
public class MyWebsocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        System.out.println("有人连接进来了:" + ctx.channel().remoteAddress());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        System.out.println("有人退出了:" + ctx + ctx.channel().remoteAddress());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) {
        System.out.println(ctx.channel().remoteAddress() + ":" + msg.text());

        TextWebSocketFrame response = new TextWebSocketFrame("服务器时间:" + LocalDateTime.now() + ":" + msg.text());
        ctx.writeAndFlush(response);
    }
}
