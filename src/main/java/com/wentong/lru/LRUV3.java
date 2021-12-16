package com.wentong.lru;

/**
 * 实现并发安全且高并发的 LRU cache
 */
public class LRUV3<K, V> {

    private final LRUV2<K,V>[] segments;
    private final int concurrency;

    public LRUV3(int maxCapacity) {
        int nCPUS = Runtime.getRuntime().availableProcessors();
        this.concurrency = Math.max(nCPUS, 2);
        segments = new LRUV2[concurrency];
        for (int i = 0; i < concurrency; i++) {
            segments[i] = new LRUV2<>(maxCapacity / concurrency);
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
}
