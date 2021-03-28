package me.giraffetree.websocket.c10k.netty;

import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;

/**
 * @author GiraffeTree
 * @date 2021/3/25 16:37
 */
@Slf4j
public class TextWebsocketFrameHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    /**
     * {"seq":"0","cmd":"pong","response":{"code":200}}
     */
    private final static String PONG_STRING = "{\"seq\":\"0\",\"cmd\":\"pong\",\"response\":{\"code\":200}}";

    private final static String PING_CMD = "ping";
    private final static String CMD = "cmd";

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        super.handlerAdded(ctx);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        super.handlerRemoved(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        String text = msg.text();
        WebSocketDTO webSocketDTO = JSON.parseObject(text, WebSocketDTO.class);
        String cmd = webSocketDTO.getCmd();
        switch (cmd) {
            case PING_CMD:
                log.info("try to send ping ...");
                ctx.channel().writeAndFlush(new TextWebSocketFrame(PONG_STRING));
                break;
            default:
        }
    }
}
