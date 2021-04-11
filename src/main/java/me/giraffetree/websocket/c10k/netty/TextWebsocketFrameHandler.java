package me.giraffetree.websocket.c10k.netty;

import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.util.Attribute;
import io.netty.util.concurrent.FastThreadLocalThread;
import io.netty.util.internal.InternalThreadLocalMap;
import lombok.extern.slf4j.Slf4j;

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
        if (evt instanceof WebSocketServerProtocolHandler.HandshakeComplete) {
            ConnectionStatisticsManager.addConnection();
        }
        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        super.handlerAdded(ctx);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        FastThreadLocalThread thread = (FastThreadLocalThread) Thread.currentThread();
        Attribute<String> attr = ctx.channel().attr(Constants.ATTRIBUTE_KEY_ID);
        String id = attr.get();
        log.debug("get ping thread:{} id:{}", thread.getId(), id);
        InternalThreadLocalMap internalThreadLocalMap = thread.threadLocalMap();
        if (internalThreadLocalMap.isIndexedVariableSet(31)) {
            UnsafeConnectionManager manager = (UnsafeConnectionManager) internalThreadLocalMap.indexedVariable(31);
            log.info("delete connection - id:{}", id);
            manager.deleteConnection(id);
        } else {
            log.warn("not found threadLocal - thread:{}", thread.getId());
        }
        ConnectionStatisticsManager.removeConnection();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        String text = msg.text();
        WebSocketDTO webSocketDTO = JSON.parseObject(text, WebSocketDTO.class);
        String cmd = webSocketDTO.getCmd();
        switch (cmd) {
            case PING_CMD:
                FastThreadLocalThread thread = (FastThreadLocalThread) Thread.currentThread();
                Attribute<String> attr = ctx.channel().attr(Constants.ATTRIBUTE_KEY_ID);
                String id = attr.get();
                log.debug("get ping thread:{} id:{}", thread.getId(), id);
                InternalThreadLocalMap internalThreadLocalMap = thread.threadLocalMap();
                if (internalThreadLocalMap.isIndexedVariableSet(31)) {
                    UnsafeConnectionManager manager = (UnsafeConnectionManager) internalThreadLocalMap.indexedVariable(31);
                    ChannelConnectionInfo connection = manager.getConnection(id);
                    connection.resetExpired();
                    manager.updateConnection(id, connection);
                    ctx.channel().writeAndFlush(new TextWebSocketFrame(PONG_STRING));
                } else {
                    log.warn("not found threadLocal - thread:{}", thread.getId());
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
