package me.giraffetree.websocket.c10k.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolConfig;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.util.Attribute;
import io.netty.util.concurrent.FastThreadLocalThread;
import io.netty.util.internal.InternalThreadLocalMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

/**
 * @author GiraffeTree
 * @date 2021-04-03
 */
@Slf4j
public class WebsocketHandler extends WebSocketServerProtocolHandler {

    private final ChannelGroup channelGroup;

    public WebsocketHandler(WebSocketServerProtocolConfig serverConfig, ChannelGroup channelGroup) {
        super(serverConfig);
        this.channelGroup = channelGroup;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof ServerHandshakeStateEvent) {
            String name = ((ServerHandshakeStateEvent) evt).name();
            log.info("ServerHandshakeStateEvent - {}", name);
        }
        if (evt instanceof HandshakeComplete) {
            Channel channel = ctx.channel();
            String requestUri = ((HandshakeComplete) evt).requestUri();
            String[] split = StringUtils.split(requestUri, "?");
            if (split.length != 2) {
                ctx.close();
                return;
            }
            String[] pairs = split[1].split("&");
            HashMap<String, String> map = new HashMap<>();
            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                try {
                    map.put(URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8.name()),
                            URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8.name()));
                } catch (UnsupportedEncodingException e) {
                    // do nothing
                }
            }
            String id = map.get("id");
            if (id == null) {
                ctx.close();
                return;
            }
            Attribute<String> attr = channel.attr(Constants.ATTRIBUTE_KEY_ID);
            attr.set(id);
            channelGroup.add(channel);

            FastThreadLocalThread fastThreadLocalThread = (FastThreadLocalThread) Thread.currentThread();
            fastThreadLocalThread.setThreadLocalMap(InternalThreadLocalMap.get());
            InternalThreadLocalMap internalThreadLocalMap = fastThreadLocalThread.threadLocalMap();
            if (!internalThreadLocalMap.isIndexedVariableSet(31)) {
                boolean success = internalThreadLocalMap.setIndexedVariable(31, new UnsafeConnectionManager());
                log.debug("add connection manager success:{} thread:{}", success, Thread.currentThread().getId());
            }
            UnsafeConnectionManager manager = (UnsafeConnectionManager) internalThreadLocalMap.indexedVariable(31);
            manager.addConnection(id, new ChannelConnectionInfo(id, channel));

            log.info("add thread:{} id:{}", Thread.currentThread().getId(), id);
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }


    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        super.handlerRemoved(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Attribute<String> attr = ctx.channel().attr(Constants.ATTRIBUTE_KEY_ID);
        String id = attr.get();
        log.error("cause - id:{} msg:{}",id, cause.getLocalizedMessage());
        ctx.close();
    }
}
