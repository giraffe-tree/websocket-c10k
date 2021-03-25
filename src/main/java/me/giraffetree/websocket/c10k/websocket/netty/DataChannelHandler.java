package me.giraffetree.websocket.c10k.websocket.netty;

import com.alibaba.fastjson.JSON;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.springframework.stereotype.Component;

@Component
@ChannelHandler.Sharable
public class DataChannelHandler extends ChannelHandlerBase {

    @Override
    public void channelRead0(ChannelHandlerContext channelHandlerContext, TextWebSocketFrame msg) {
        Channel channel = channelHandlerContext.channel();
        WebSocketDTO request = JSON.parseObject(msg.text(), WebSocketDTO.class);
        switch (request.getCmd()) {
            case "ping":
                super.heartCheckHandler(channel);
                break;
            default:
                break;
        }
    }

}
