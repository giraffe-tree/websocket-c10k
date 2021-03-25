package me.giraffetree.websocket.c10k.websocket.netty2;

import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.springframework.web.socket.TextMessage;

/**
 * @author GiraffeTree
 * @date 2021/3/25 16:37
 */
public class WebsocketFrameHandler extends SimpleChannelInboundHandler<WebSocketFrame> {
    /**
     * {"seq":"0","cmd":"pong","response":{"code":200}}
     */
    private final String PONG_STRING = "{\"seq\":\"0\",\"cmd\":\"pong\",\"response\":{\"code\":200}}";

    private final static String PING_CMD = "ping";
    private final static String CMD = "cmd";

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        super.handlerAdded(ctx);
        ConnectionStatisticsManager.addConnection();
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        super.handlerRemoved(ctx);
        ConnectionStatisticsManager.removeConnection();
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);

    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);

    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame msg) throws Exception {

        if (msg instanceof TextWebSocketFrame) {
            String text = ((TextWebSocketFrame) msg).text();
            WebSocketDTO webSocketDTO = JSON.parseObject(text, WebSocketDTO.class);
            String cmd = webSocketDTO.getCmd();
            switch (cmd) {
                case PING_CMD:
                    ctx.channel().writeAndFlush(new TextWebSocketFrame(PONG_STRING));
                    break;
                default:
            }
        } else if (msg instanceof CloseWebSocketFrame) {
            ctx.close();
        }

    }
}
