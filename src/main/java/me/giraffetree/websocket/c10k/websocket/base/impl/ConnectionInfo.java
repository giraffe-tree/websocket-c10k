package me.giraffetree.websocket.c10k.websocket.base.impl;

import lombok.Data;
import me.giraffetree.websocket.c10k.websocket.base.IExpired;
import org.springframework.web.socket.WebSocketSession;

/**
 * @author GiraffeTree
 * @date 2021/3/22 16:26
 */
@Data
public class ConnectionInfo implements IExpired {

    private final static Long DELAY_MILLS = 10000L;
    /**
     * session
     */
    private WebSocketSession session;
    /**
     * 设备唯一 id
     */
    private String id;
    /**
     * 最大延迟时间
     */
    private Long maxDelayMills;
    /**
     * 连接时间
     */
    private Long connectedTime;
    /**
     * 最近一次消息时间
     */
    private Long latestMsgTime;

    @Override
    public boolean checkExpired() {
        return System.currentTimeMillis() > latestMsgTime + maxDelayMills;
    }

    public ConnectionInfo() {
        this.maxDelayMills = DELAY_MILLS;
    }

    public ConnectionInfo(WebSocketSession session, String id) {
        this.session = session;
        this.id = id;
        this.maxDelayMills = DELAY_MILLS;
        long now = System.currentTimeMillis();
        this.connectedTime = now;
        this.latestMsgTime = now;
    }

    public ConnectionInfo(WebSocketSession session, String id, Long delayMills) {
        this.session = session;
        this.id = id;
        this.maxDelayMills = delayMills;
        long now = System.currentTimeMillis();
        this.connectedTime = now;
        this.latestMsgTime = now;
    }
}
