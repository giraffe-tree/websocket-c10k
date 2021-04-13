package me.giraffetree.websocket.c10k.netty;

import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.Attribute;
import io.netty.util.concurrent.FastThreadLocalThread;
import io.netty.util.internal.InternalThreadLocalMap;
import lombok.extern.slf4j.Slf4j;
import me.giraffetree.websocket.c10k.base.ThreadLocalUtils;

import java.io.IOException;

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
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            Attribute<String> attr = ctx.channel().attr(Constants.ATTRIBUTE_KEY_ID);
            String id = attr.get();
            log.info("userEventTriggered - {} - try to close id:{}", evt, id);
            ctx.close();
        }
        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        super.handlerAdded(ctx);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        Attribute<String> attr = ctx.channel().attr(Constants.ATTRIBUTE_KEY_ID);
        String id = attr.get();
        log.debug("get handler removed id:{}", id);

        ThreadLocalUtils.removeConnection(id);
        ConnectionStatisticsManager.removeConnection();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        String text = msg.text();
        WebSocketDTO webSocketDTO = JSON.parseObject(text, WebSocketDTO.class);
        String cmd = webSocketDTO.getCmd();
        switch (cmd) {
            case PING_CMD:
                Attribute<String> attr = ctx.channel().attr(Constants.ATTRIBUTE_KEY_ID);
                String id = attr.get();
                log.debug("get ping  id:{}", id);

                if (!ThreadLocalUtils.flushConnection(id)) {
                    ctx.close();
                }
                break;
            default:
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Attribute<String> attr = ctx.channel().attr(Constants.ATTRIBUTE_KEY_ID);
        String id = attr.get();

        if (cause instanceof IOException) {
            log.error("io exception - try to close ctx - id:{} msg{}", id, cause.getLocalizedMessage());
        } else {
            log.error("cause id:{} msg:{}", id, cause.getLocalizedMessage());
        }
        ctx.close();
    }
}
