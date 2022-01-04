package com.wentong;

import com.wentong.config.CacheManagerConfig;
import com.wentong.hugecache.StorageMode;
import com.wentong.hugecache.cache.HugeCache;
import com.wentong.listener.EvictListener;
import com.wentong.lru.NBLRU;
import com.wentong.serialize.DeSerializer;
import com.wentong.serialize.Serializer;
import com.wentong.serialize.impl.jdk.JavaDeserializer;
import com.wentong.serialize.impl.jdk.JavaSerializer;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class CacheManager<K, V> {

    private final NBLRU<K, V> lruCache;
    private final HugeCache<K> persistenceCache;
    private final Serializer<V> serializer;
    private final DeSerializer<V> deserialize;

    public CacheManager(int lruCapacity) {
        this.lruCache = new NBLRU<>(lruCapacity, new EvictListenerImpl());
        this.persistenceCache = new HugeCache<>(CacheManagerConfig.DIR_INIT, CacheManagerConfig.MODE, CacheManagerConfig.WORK_DIR, CacheManagerConfig.CAPACITY);
        this.serializer = new JavaSerializer<>();
        this.deserialize = new JavaDeserializer<>();
    }

    public CacheManager(int lruCapacity, int initialization, StorageMode mode, String dir, int capacity) {
        this.lruCache = new NBLRU<>(lruCapacity, new EvictListenerImpl());
        this.persistenceCache = new HugeCache<>(initialization, mode, dir, capacity);
        this.serializer = new JavaSerializer<>();
        this.deserialize = new JavaDeserializer<>();
    }

    public void put(K k, V v) {
        lruCache.put(k, v);
    }

    public void put(K k, V v, long time, TimeUnit timeUnit) {

    }

    public V get(K k) {
        V v = lruCache.get(k);
        if (v != null) {
            return v;
        } else {
            byte[] bytes = persistenceCache.get(k);
            if (bytes.length != 0) {
                return deserialize.deserialize(bytes);
            } else {
                return null;
            }
        }
    }

    public V delete(K k) {
        V v = lruCache.remove(k);
        // 两种可能性，一是存的值为 null；二是确实没查着
        if (v == null) {
            byte[] bytes = persistenceCache.delete(k);
            if (bytes.length != 0) {
                return deserialize.deserialize(bytes);
            } else {
                return null;
            }
        } else {
            return v;
        }
    }

    public void ttl(K k, long time, TimeUnit timeUnit) {

    }

    public void close() throws IOException {

    }

    class EvictListenerImpl implements EvictListener<K, V> {

        @Override
        public void onEvict(K k, V v) {
            CacheManager.this.persistenceCache.put(k, serializer.serialize(v));
        }
    }
}
