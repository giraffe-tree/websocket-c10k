package me.giraffetree.websocket.c10k.base;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.Attribute;
import io.netty.util.concurrent.FastThreadLocalThread;
import io.netty.util.internal.InternalThreadLocalMap;
import lombok.extern.slf4j.Slf4j;
import me.giraffetree.websocket.c10k.netty.ChannelConnectionInfo;
import me.giraffetree.websocket.c10k.netty.Constants;
import me.giraffetree.websocket.c10k.netty.UnsafeConnectionManager;

/**
 * @author GiraffeTree
 * @date 2021-04-13
 */
@Slf4j
public class ThreadLocalUtils {

    public static void removeConnection(String id) {
        if (id == null) {
            log.warn("removeConnection - id is null");
        }
        Thread cur = Thread.currentThread();
        if (cur instanceof FastThreadLocalThread) {
            FastThreadLocalThread thread = (FastThreadLocalThread) cur;
            InternalThreadLocalMap internalThreadLocalMap = thread.threadLocalMap();
            if (internalThreadLocalMap.isIndexedVariableSet(31)) {
                UnsafeConnectionManager manager = (UnsafeConnectionManager) internalThreadLocalMap.indexedVariable(31);
                log.info("delete connection - id:{}", id);
                manager.deleteConnection(id);
            } else {
                log.warn("not found threadLocal - thread:{}", thread.getId());
            }
        }
    }

    public static boolean flushConnection(String id) {
        Thread cur = Thread.currentThread();
        if (cur instanceof FastThreadLocalThread) {
            FastThreadLocalThread thread = (FastThreadLocalThread) Thread.currentThread();
            InternalThreadLocalMap internalThreadLocalMap = thread.threadLocalMap();
            if (internalThreadLocalMap.isIndexedVariableSet(31)) {
                UnsafeConnectionManager manager = (UnsafeConnectionManager) internalThreadLocalMap.indexedVariable(31);
                ChannelConnectionInfo connection = manager.getConnection(id);
                connection.resetExpired();
                manager.updateConnection(id, connection);
                return true;
            } else {
                log.warn("not found threadLocal - thread:{}", thread.getId());
                return false;
            }
        }
        log.warn("thread not match");
        return false;
    }

    public static boolean addConnection(String id, Channel channel) {
        Thread cur = Thread.currentThread();
        if (cur instanceof FastThreadLocalThread) {
            FastThreadLocalThread fastThreadLocalThread = (FastThreadLocalThread) cur;
            fastThreadLocalThread.setThreadLocalMap(InternalThreadLocalMap.get());
            InternalThreadLocalMap internalThreadLocalMap = fastThreadLocalThread.threadLocalMap();
            if (!internalThreadLocalMap.isIndexedVariableSet(31)) {
                boolean success = internalThreadLocalMap.setIndexedVariable(31, new UnsafeConnectionManager());
                log.debug("add connection manager success:{}", success);
            }
            UnsafeConnectionManager manager = (UnsafeConnectionManager) internalThreadLocalMap.indexedVariable(31);
            manager.addConnection(id, new ChannelConnectionInfo(id, channel));
            log.info("add connection id:{}", id);
            return true;
        }
        log.warn("thread not match");
        return false;
    }


}
