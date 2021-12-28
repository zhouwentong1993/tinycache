package com.wentong.hugecache.cache;

import java.io.Closeable;
import java.util.concurrent.TimeUnit;

public interface Cache<K> extends Closeable {

    void put(K k, byte[] v);

    void put(K k, byte[] v, long time, TimeUnit timeUnit);

    byte[] get(K k);

    byte[] delete(K k);

    void ttl(K k, long time, TimeUnit timeUnit);

}
