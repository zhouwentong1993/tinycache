package com.wentong.lru;

import com.wentong.listener.EvictListener;

import java.util.*;


/**
 * 实现并发安全的 LRU cache
 *
 * @param <K>
 * @param <V>
 */
public class LRUV2<K, V> implements LRU<K, V> {

    private final Map<K, Node<K, V>> map;
    private int maxCapacity = 4096;
    private Node<K, V> tail;
    private Node<K, V> head;
    private EvictListener<K,V> listener;

    public void setMaxCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    static class Node<K, V> {
        K k;
        V v;

        public Node(K k, V v) {
            this.k = k;
            this.v = v;
        }

        Node<K, V> next;
        Node<K, V> prev;
    }

    public LRUV2() {
        this.map = new HashMap<>();
    }

    public LRUV2(int maxCapacity) {
        this.maxCapacity = maxCapacity;
        this.map = new HashMap<>();
    }

    public LRUV2(int maxCapacity, EvictListener<K, V> listener) {
        this.maxCapacity = maxCapacity;
        this.map = new HashMap<>();
        this.listener = listener;
    }

    @Override
    public synchronized V get(K k) {
        if (map.containsKey(k)) {
            Node<K, V> node = map.get(k);
            removeNode(node);
            offerNode(node);
            return node.v;
        } else {
            return null;
        }
    }

    @Override
    public synchronized void put(K k, V v) {
        if (map.containsKey(k)) {
            Node<K, V> node = map.get(k);
            node.v = v;
            removeNode(node);
            offerNode(node);
        } else {
            Node<K, V> node = new Node<>(k, v);
            if (size() == maxCapacity) {
                removeTail();
            }
            offerNode(node);
            map.put(k, node);
        }
    }

    @Override
    public synchronized int size() {
        return map.size();
    }

    @Override
    public V remove(K k) {
        Node<K, V> node = map.remove(k);
        removeNode(node);
        return node.v;
    }

    public synchronized List<K> keyOrders() {
        if (head == null) {
            return Collections.emptyList();
        }
        Node<K, V> tmp = head;
        List<K> keys = new ArrayList<>(size());
        while (tmp != null) {
            keys.add(tmp.k);
            tmp = tmp.next;
        }
        return keys;
    }

    private void removeNode(Node<K, V> node) {
        if (node == null) {
            return;
        }
        if (node.prev != null) {
            node.prev.next = node.next;
        } else {
            head = node.next;
        }

        if (node.next != null) {
            node.next.prev = node.prev;
        } else {
            tail = node.prev;
        }

    }

    private void offerNode(Node<K, V> node) {
        if (node == head) {
            return;
        }
        if (node == tail) {
            removeTail();
        }
        if (head != null) {
            head.prev = node;
            node.next = head;
            head = node;
        } else {
            node.next = null;
            node.prev = null;
            head = node;
            tail = node;
        }
    }

    private void removeTail() {
        Node<K,V> evictNode = tail;
        if (tail.prev != null) {
            tail.prev.next = null;
            tail = tail.prev;
        }
        if (listener != null) {
            listener.onEvict(evictNode.k, evictNode.v);
        }
    }

}
