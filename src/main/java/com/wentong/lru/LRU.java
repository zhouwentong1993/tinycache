package com.wentong.lru;

public interface LRU<K, V> {

    void put(K k, V v);

    V get(K k);

    int size();

}
