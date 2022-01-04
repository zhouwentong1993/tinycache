package com.wentong.lru;

import com.wentong.listener.EvictListener;

/**
 * 实现并发安全且高并发的 LRU cache
 */
public class NBLRU<K, V> {

    private final LRU<K, V>[] segments;
    private final int concurrency;

    public NBLRU(int maxCapacity, EvictListener<K, V> listener) {
        int nCPUS = Runtime.getRuntime().availableProcessors();
        this.concurrency = Math.max(nCPUS, 2);
        segments = new LRUV2[concurrency];
        for (int i = 0; i < concurrency; i++) {
            segments[i] = new LRUV2<>(maxCapacity / concurrency, listener);
        }
    }

    private int indexOf(K k) {
        return Math.abs(k.hashCode() * 31) % concurrency;
    }

    public V get(K k) {
        return segments[indexOf(k)].get(k);
    }

    public void put(K k, V v) {
        segments[indexOf(k)].put(k, v);
    }

    public V remove(K k) {
        return segments[indexOf(k)].remove(k);
    }

}
