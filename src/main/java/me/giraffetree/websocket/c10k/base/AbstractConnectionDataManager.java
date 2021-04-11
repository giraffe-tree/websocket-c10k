package me.giraffetree.websocket.c10k.base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 连接管理器抽象类, 非线程安全
 * 1. 包含一个 先进先出的队列, 但需要在必要时可以 O(1) 删除中间元素
 * 2. 包含一个 map, 可以在 O(1) 时间内添加元素, 查找元素
 *
 * @param <K> 一般是 sessionId
 * @param <V> 一般为连接信息的对象
 * @author GiraffeTree
 * @date 2020/12/4 17:14
 */
public abstract class AbstractConnectionDataManager<K, V extends IExpired> implements IConnectionManager<K, V> {

    /**
     * 链表表头元素
     * Invariant: (first == null && last == null) ||
     * (first.prev == null && first.item != null)
     */
    transient LinkedNode<K, V> first;
    /**
     * 链表表尾元素
     */
    transient LinkedNode<K, V> last;
    /**
     * 存储连接数据
     */
    protected HashMap<K, LinkedNode<K, V>> connectionMap;

    public AbstractConnectionDataManager() {
        this.connectionMap = new HashMap<>(1024);
    }

    public AbstractConnectionDataManager(int initialCapacity) {
        this.connectionMap = new HashMap<>(initialCapacity);
    }

    @Override
    public V getConnection(K key) {
        LinkedNode<K, V> node = connectionMap.get(key);
        return node == null ? null : node.item;
    }

    @Override
    public void addConnection(K key, V value) {
        if (key == null || value == null) {
            throw new NullPointerException();
        }
        LinkedNode<K, V> node = linkLast(key, value);
        connectionMap.put(key, node);
        checkLink();
    }

    private void checkLink() {
        boolean check = (first == null && last == null) || (first.prev == null && first.item != null);
        if (!check) {
            throw new RuntimeException("check error");
        }
    }

    @Override
    public V deleteConnection(K key) {
        LinkedNode<K, V> removed = connectionMap.remove(key);
        if (removed == null) {
            return null;
        }
        unlink(removed);
        checkLink();
        return removed.item;
    }

    @Override
    public List<V> getExpiredConnection(int max) {
        ArrayList<V> result = new ArrayList<>(Math.min(max, connectionMap.size()));
        LinkedNode<K, V> next = this.first;
        while (next != null && max > 0) {
            LinkedNode<K, V> cur = next;
            next = next.next;
            V item = cur.item;
            if (!item.checkExpired()) {
                break;
            }
            max--;
            result.add(item);
        }
        checkLink();
        return result;
    }

    @Override
    public List<V> deleteExpiredConnection(int max) {
        ArrayList<V> result = new ArrayList<>(Math.min(max, connectionMap.size()));
        LinkedNode<K, V> next = this.first;
        while (next != null && max > 0) {
            LinkedNode<K, V> cur = next;
            next = next.next;
            V item = cur.item;
            if (!item.checkExpired()) {
                break;
            }
            unlink(cur);
            max--;
            connectionMap.remove(cur.key);
            result.add(item);
        }
        checkLink();
        return result;
    }

    @Override
    public void updateConnection(K key, V value) {
        // 如果不存在, 则直接
        if (!connectionMap.containsKey(key)) {
            addConnection(key, value);
            return;
        }
        LinkedNode<K, V> node = connectionMap.get(key);
        if (node == last) {
            return;
        }
        // 直接比较对象指针
        if (node.item != value) {
            deleteConnection(key);
            addConnection(key, value);
            return;
        }
        moveToLast(node);
        checkLink();
    }


    /**
     * link non-null item
     */
    private LinkedNode<K, V> linkLast(K key, V connectionInfo) {
        LinkedNode<K, V> l = last;
        LinkedNode<K, V> newNode = new LinkedNode<>(l, key, connectionInfo, null);
        if (l == null) {
            first = newNode;
        } else {
            l.next = newNode;
        }
        last = newNode;
        return newNode;
    }

    /**
     * unlink non-null node
     */
    private void unlink(LinkedNode<K, V> node) {
        LinkedNode<K, V> prev = node.prev;
        LinkedNode<K, V> next = node.next;
        if (first == node) {
            // 在最前面
            if (next == null) {
                last = null;
                first = null;
            } else {
                first = next;
                first.prev = null;
            }
        } else if (last == node) {
            // 在最后
            prev.next = null;
            last = prev;
        } else {
            // 在中间
            prev.next = next;
            next.prev = prev;
        }
    }

    /**
     * 将一个元素移到队尾
     */
    private void moveToLast(LinkedNode<K, V> node) {
        LinkedNode<K, V> next = node.next;
        LinkedNode<K, V> prev = node.prev;
        if (prev != null) {
            prev.next = next;
        } else {
            first = next;
        }
        if (next == null) {
            return;
        }
        next.prev = prev;
        node.prev = last;
        last.next = node;
        node.next = null;
        last = node;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        LinkedNode<K, V> next = this.first;
        int max = 5;
        while (next != null && max > 0) {
            V item = next.item;
            sb.append("-> ").append(item.toString()).append(" ");
            next = next.next;
            max--;
        }
        return sb.toString();
    }

    @Override
    public List<V> getConnectionList(int skip, int limit) {
        return connectionMap.entrySet().stream().skip(skip).limit(limit).map(x -> x.getValue().item).collect(Collectors.toList());
    }

    @Override
    public int getConnectionSize() {
        return connectionMap.size();
    }

}
