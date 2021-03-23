package me.giraffetree.websocket.c10k.websocket.base.impl;

import lombok.extern.slf4j.Slf4j;
import me.giraffetree.websocket.c10k.websocket.base.IConnectionManager;
import me.giraffetree.websocket.c10k.websocket.base.IDeviceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author GiraffeTree
 * @date 2021/3/22 17:00
 */
@Slf4j
@Service
public class DeviceConnectionManager implements IDeviceManager {

    /**
     * 每隔几秒检查一次连接异常的 session
     */
    private final static int CHECK_DISCONNECTED_SESSION_INTERVAL_SECONDS = 1;

    private final static String CHECK_DISCONNECTED_SESSION_CRON = "*/" + CHECK_DISCONNECTED_SESSION_INTERVAL_SECONDS + " * * * * ?";

    private final IConnectionManager<String, ConnectionInfo> connectionManager;

    private final ConcurrentHashMap<String, ConnectionInfo> cacheMap;

    @Autowired
    public DeviceConnectionManager(IConnectionManager<String, ConnectionInfo> connectionManager) {
        this.connectionManager = connectionManager;
        this.cacheMap = new ConcurrentHashMap<>(1024);
    }

    @Override
    @Scheduled(cron = CHECK_DISCONNECTED_SESSION_CRON)
    public void checkExpiredConnection() {
        List<ConnectionInfo> connectionInfoList = getExpiredConnection(1024);
        if (connectionInfoList.isEmpty()) {
            return;
        }
        int successSize = 0;
        for (ConnectionInfo connectionInfo : connectionInfoList) {
            WebSocketSession session = connectionInfo.getSession();
            String sessionId = session.getId();
            String subSessionId = sessionId.substring(sessionId.length() - 4);
            try {
                if (session.isOpen()) {
                    synchronized (session) {
                        if (session.isOpen()) {
                            log.debug("try to close expired connection - id:{} sessionId:{}", connectionInfo.getId(), subSessionId);
                            session.close(CloseStatus.SESSION_NOT_RELIABLE);
                            successSize++;
                        }
                    }
                }
            } catch (IOException e) {
                log.warn("expired session close error - id:{} msg:{} id:{} ",
                        connectionInfo.getId(), e.getLocalizedMessage(), subSessionId);
            }
        }
        int size = connectionInfoList.size();
        log.info("check expired connection - expired:{} successClose:{}", size, successSize);
    }

    public synchronized List<ConnectionInfo> getExpiredConnection(int max) {
        return connectionManager.getExpiredConnection(max);
    }

    @Override
    public synchronized void updateExpiredTime(String sessionId) {
        ConnectionInfo connectionInfo = getConnectionInfo(sessionId);
        if (connectionInfo == null) {
            return;
        }
        connectionInfo.setLatestMsgTime(System.currentTimeMillis());
        // 更新连接
        connectionManager.updateConnection(sessionId, connectionInfo);
    }

    @Override
    public synchronized List<ConnectionInfo> getConnectionInfoList(int skip, int limit) {
        return connectionManager.getConnectionList(skip, limit);
    }

    @Override
    public synchronized ConnectionInfo getConnectionInfo(String sessionId) {
        return connectionManager.getConnection(sessionId);
    }

    @Override
    public synchronized ConnectionInfo getConnectionInfoById(String id) {
        ConnectionInfo connectionInfo = cacheMap.get(id);
        if (connectionInfo == null) {
            return null;
        }
        return connectionInfo;
    }

    @Override
    public synchronized boolean contains(String id) {
        return cacheMap.containsKey(id);
    }

    @Override
    public synchronized ConnectionInfo addConnectionInfo(String id, WebSocketSession session) {
        ConnectionInfo connectionInfo = new ConnectionInfo(session, id);
        connectionManager.addConnection(session.getId(), connectionInfo);
        cacheMap.put(id, connectionInfo);
        return connectionInfo;
    }

    @Override
    public synchronized ConnectionInfo removeConnectionInfoById(String id) {
        ConnectionInfo removed = cacheMap.remove(id);
        return connectionManager.deleteConnection(removed.getSession().getId());
    }

    @Override
    public synchronized ConnectionInfo removeConnectionInfoBySessionId(String sessionId) {
        ConnectionInfo connectionInfo = connectionManager.deleteConnection(sessionId);
        if (connectionInfo != null) {
            String id = connectionInfo.getId();
            cacheMap.remove(id);
        }
        return connectionInfo;
    }


    @Override
    public synchronized int getConnectionSize() {
        return 0;
    }
}
