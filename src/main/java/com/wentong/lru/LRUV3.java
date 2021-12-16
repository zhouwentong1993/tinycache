package com.wentong.lru;

import java.util.Objects;

/**
 * 实现并发安全且高并发的 LRU cache
 */
public class LRUV3<K, V> {

    private final LRU<K,V>[] segments;
    private final int concurrency;

    public LRUV3(int maxCapacity, String className) {
        Objects.requireNonNull(className);
        int nCPUS = Runtime.getRuntime().availableProcessors();
        this.concurrency = Math.max(nCPUS, 2);
        switch (className) {
            case "v2":
                segments = new LRUV2[concurrency];
                for (int i = 0; i < concurrency; i++) {
                    segments[i] = new LRUV2<>(maxCapacity / concurrency);
                }
                break;
            case "v21":
                segments = new LRUV21[concurrency];
                for (int i = 0; i < concurrency; i++) {
                    segments[i] = new LRUV21<>(maxCapacity / concurrency);
                }
                break;
            default:
                throw new IllegalArgumentException();
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
