package me.giraffetree.websocket.c10k.websocket.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.atomic.LongAdder;

@Slf4j
public class ChannelHandlerBase extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    @Autowired
    private ChannelGroupUtil channelGroupUtil;

    @Autowired
    private MessageSender messageSender;

    private final LongAdder connectionSize = new LongAdder();
    private final LongAdder heartBeatSize = new LongAdder();

    /**
     * 连接开启
     */
    @Override
    public void handlerAdded(ChannelHandlerContext channelHandlerContext) {
        channelGroupUtil.addChannel(channelHandlerContext.channel());
        connectionSize.increment();
        log.info("get connection - size:{}", connectionSize.longValue());
    }

    /**
     * 接受消息 由子类实现
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TextWebSocketFrame msg) {
    }

    /**
     * 连接关闭
     */
    @Override
    public void handlerRemoved(ChannelHandlerContext channelHandlerContext) {
        channelGroupUtil.removeChannel(channelHandlerContext.channel());
        connectionSize.decrement();
        log.info("close connection - size:{}", connectionSize.longValue());
    }

    /**
     * 异常处理
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable cause) {
        channelGroupUtil.removeChannel(channelHandlerContext.channel());
    }

    /**
     * 心跳检查处理 HEART_CHECK
     */
    protected void heartCheckHandler(Channel channel) {
        messageSender.sendMessage(channel, new WebSocketDTO("pong"));
        heartBeatSize.increment();
        log.info("sendHeartBeat pong - size:{}", heartBeatSize.longValue());
    }

}
