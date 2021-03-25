package me.giraffetree.websocket.c10k.websocket.netty;

import com.alibaba.fastjson.JSON;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MessageSender {
    @Autowired
    private ChannelGroupUtil channelGroupUtil;

    public void sendMessage(String id, WebSocketDTO webSocketDTO) {
        Channel channel = channelGroupUtil.getChannelById(id);
        if (channel == null) {
            return;
        }
        this.sendMessage(channel, webSocketDTO);
    }

    public void sendMessage(Channel channel, WebSocketDTO webSocketDTO) {
        if (channel != null && channel.isOpen()) {
            channel.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(webSocketDTO)));
        }
    }

}
