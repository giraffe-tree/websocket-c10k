package me.giraffetree.websocket.c10k.base;

import java.util.List;

/**
 * 连接管理接口
 *
 * @author GiraffeTree
 * @date 2020/12/7 10:35
 */
public interface IConnectionManager<K, V extends IExpired> {

    /**
     * 获取连接信息
     *
     * @param key 键
     * @return 值
     */
    V getConnection(K key);

    /**
     * 添加连接
     *
     * @param key   键, 不能为空
     * @param value 值, 不能为空
     */
    void addConnection(K key, V value);

    /**
     * 删除连接
     *
     * @param key 键
     * @return 值
     */
    V deleteConnection(K key);

    /***
     * 获取过期的连接
     * @param max 每次获取的最大数量
     * @return 返回过期连接对应的 值
     */
    List<V> getExpiredConnection(int max);

    /**
     * 删除过期的连接
     *
     * @param max 每次最大删除的连接数
     * @return 返回删除后的过期连接对应的 值
     */
    List<V> deleteExpiredConnection(int max);

    /**
     * 更新连接
     * 主要用于更新最近心跳时间等
     *
     * @param key   键
     * @param value 键
     */
    void updateConnection(K key, V value);

    /**
     * 获取连接列表
     *
     * @param skip  跳过
     * @param limit 限制数
     * @return List
     */
    List<V> getConnectionList(int skip, int limit);

    /**
     * 获取连接总数
     *
     * @return long 总数
     */
    int getConnectionSize();

}
