package me.giraffetree.websocket.c10k.base;

/**
 * @author GiraffeTree
 * @date 2020/12/4 16:31
 */
public class LinkedNode<K,V> {
    K key;
    V item;
    LinkedNode<K,V> next;
    LinkedNode<K,V> prev;

    public LinkedNode(LinkedNode<K,V> prev, K key, V element, LinkedNode<K,V> next) {
        this.key = key;
        this.item = element;
        this.next = next;
        this.prev = prev;
    }

}