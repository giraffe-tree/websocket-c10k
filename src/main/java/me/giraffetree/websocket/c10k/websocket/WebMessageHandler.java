package me.giraffetree.websocket.c10k.websocket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import me.giraffetree.websocket.c10k.websocket.base.IDeviceManager;
import me.giraffetree.websocket.c10k.websocket.base.impl.ConnectionInfo;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;

import java.util.Map;
import java.util.concurrent.atomic.LongAdder;

/**
 * @author GiraffeTree
 * @date 2020/11/4 13:56
 */
@Slf4j
@Component
public class WebMessageHandler extends AbstractWebSocketHandler implements DisposableBean {

    /**
     * 发送超时时间
     */
    private final static int DEFAULT_WEBSOCKET_SEND_TIME_LIMIT_MILLS = 500;
    /**
     * 缓存大小
     */
    private final static int DEFAULT_WEBSOCKET_BUFFER_SIZE_LIMIT_MILLS = 256;

    private final IDeviceManager deviceManager;

    /**
     * {"seq":"0","cmd":"pong","response":{"code":200}}
     */
    private final TextMessage PONG = new TextMessage("{\"seq\":\"0\",\"cmd\":\"pong\",\"response\":{\"code\":200}}");

    private final static String PING_CMD = "ping";
    private final static String CMD = "cmd";
    private final LongAdder longAdder = new LongAdder();

    @Autowired
    public WebMessageHandler(IDeviceManager deviceManager) {
        this.deviceManager = deviceManager;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Map<String, Object> map = session.getAttributes();
        String id = (String) map.get("id");
        if (id == null || id.length() == 0) {
            session.close();
            return;
        }
        ConcurrentWebSocketSessionDecorator concurrentWebSocketSessionDecorator = new ConcurrentWebSocketSessionDecorator(
                session, DEFAULT_WEBSOCKET_SEND_TIME_LIMIT_MILLS, DEFAULT_WEBSOCKET_BUFFER_SIZE_LIMIT_MILLS);
        deviceManager.addConnectionInfo(id, concurrentWebSocketSessionDecorator);
        longAdder.increment();
        long cur = longAdder.longValue();
        if (cur % 100L == 0L) {
            log.info("connection add - id:{} size:{} ip:{}", id, cur, session.getRemoteAddress());
        }
    }


    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) throws Exception {
        String sessionId = session.getId();
        ConnectionInfo connectionInfo = deviceManager.removeConnectionInfoBySessionId(sessionId);
        if (connectionInfo == null) {
            log.info("connection info not found and close - {}", sessionId);
            return;
        }
        longAdder.decrement();
        long cur = longAdder.longValue();
        if (cur % 100L == 0L) {
            log.info("connection close - id:{} size:{}", connectionInfo.getId(), cur);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        JSONObject jsonObject = JSON.parseObject(payload);
        String cmd = jsonObject.getString(CMD);

        if (PING_CMD.equals(cmd)) {
            synchronized (session) {
                if (session.isOpen()) {
                    session.sendMessage(PONG);
                }
            }
            deviceManager.updateExpiredTime(session.getId());
        }
    }

    @Override
    public void destroy() {

    }

}
