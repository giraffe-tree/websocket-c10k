package me.giraffetree.websocket.c10k.websocket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import me.giraffetree.websocket.c10k.websocket.base.impl.ConnectionInfo;
import me.giraffetree.websocket.c10k.websocket.base.IDeviceManager;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;

import java.util.Map;

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
        log.info("connection add - id:{}", id);
    }


    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) throws Exception {
        String sessionId = session.getId();
        ConnectionInfo connectionInfo = deviceManager.removeConnectionInfoBySessionId(sessionId);
        if (connectionInfo == null) {
            log.info("connection info not found and close - {}", sessionId);
            return;
        }
        log.info("connection close - id:{}", connectionInfo.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        JSONObject jsonObject = JSON.parseObject(payload);
        String cmd = jsonObject.getString("cmd");
        if ("ping".equals(cmd)) {
            String seq = jsonObject.getString("seq");
            // {"seq":"","cmd":"ping","response":{"code":200}}
            session.sendMessage(new TextMessage("{\"seq\":\"" + seq + "\",\"cmd\":\"pong\",\"response\":{\"code\":200}}"));
            deviceManager.updateExpiredTime(session.getId());
        }
    }

    @Override
    public void destroy() {

    }

}
