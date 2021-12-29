package com.wentong;

import com.wentong.hugecache.cache.HugeCache;
import com.wentong.listener.EvictListener;
import com.wentong.lru.NBLRU;
import com.wentong.serialize.DeSerializer;
import com.wentong.serialize.Serializer;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class CacheManager<K, V> {

    private NBLRU<K, V> lruCache;
    private HugeCache<K> persistenceCache;
    private Serializer<V> serializer;
    private DeSerializer<V> deserialize;

    public void put(K k, V v) {
        lruCache.put(k, v);
    }

    public void put(K k, byte[] v, long time, TimeUnit timeUnit) {

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
        // 两种可能性，一是存的值为 null；二是确实没差着
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
