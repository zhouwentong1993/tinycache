package com.wentong.listener;

public interface EvictListener<K, V> {
    void onEvict(K k, V v);
}
