package com.wentong.hugecache.cache;

import java.io.Closeable;

public interface Cache<K> extends Closeable {

    void put(K k, byte[] v);

    byte[] get(K k);

    void delete(K k);

    void ttl(K k);

}
