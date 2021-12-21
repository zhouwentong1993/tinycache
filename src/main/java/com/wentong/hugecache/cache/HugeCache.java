package com.wentong.hugecache.cache;

import java.io.IOException;

public class HugeCache<K> implements Cache<K>{


    @Override
    public void put(K k, byte[] v) {

    }

    @Override
    public byte[] get(K k) {
        return new byte[0];
    }

    @Override
    public void delete(K k) {

    }

    @Override
    public void ttl(K k) {

    }

    @Override
    public void close() throws IOException {

    }
}
