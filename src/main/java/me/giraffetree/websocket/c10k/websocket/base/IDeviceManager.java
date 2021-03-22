package me.giraffetree.websocket.c10k.websocket.base;

import me.giraffetree.websocket.c10k.websocket.base.impl.ConnectionInfo;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;

/**
 * @author GiraffeTree
 * @date 2021/3/22 16:59
 */
public interface IDeviceManager {

    /**
     * 检查过期连接信息
     */
    void checkExpiredConnection();

    /**
     * 更新过期时间
     *
     * @param sessionId websocket sessionId
     */
    void updateExpiredTime(String sessionId);

    /**
     * 列出连接信息列表
     *
     * @param skip  跳过个数
     * @param limit 最大列表长度
     * @return List
     */
    List<ConnectionInfo> getConnectionInfoList(int skip, int limit);

    /**
     * 根据 sessionId 获取连接信息
     *
     * @param sessionId sessionId
     * @return ConnectionInfoDTO
     */
    ConnectionInfo getConnectionInfo(String sessionId);

    /**
     * 根据 设备id 获取连接信息
     *
     * @param id 设备id
     * @return ConnectionInfoDTO
     */
    ConnectionInfo getConnectionInfoById(String id);

    /**
     * 是否存在 设备id 的 session 信息
     *
     * @param id 设备id
     * @return boolean
     */
    boolean contains(String id);

    /**
     * 添加连接信息
     *
     * @param id 设备id
     * @param session    websocket session
     * @return connectionInfo
     */
    ConnectionInfo addConnectionInfo(String id, WebSocketSession session);

    /**
     * 清除 设备id 对应的连接信息
     *
     * @param id 设备id
     * @return connectionInfo
     */
    ConnectionInfo removeConnectionInfoById(String id);

    /**
     * 清除 sessionId 对应的连接信息
     *
     * @param sessionId sessionId
     * @return connectionInfo
     */
    ConnectionInfo removeConnectionInfoBySessionId(String sessionId);

    /**
     * 获取连接数
     *
     * @return 连接数
     */
    int getConnectionSize();

}
