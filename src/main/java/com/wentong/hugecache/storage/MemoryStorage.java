package com.wentong.hugecache.storage;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 基于内存实现的存储
 */
public class MemoryStorage implements Storage {

    private final Map<Integer, byte[]> map;
    private final AtomicInteger position = new AtomicInteger(0);

    public MemoryStorage() {
        this.map = new ConcurrentHashMap<>();
    }

    @Override
    public void put(int position, byte[] data) {
        map.put(position, data);
        if (position > this.position.longValue()) {
            this.position.set(position);
        }
    }

    @Override
    public byte[] retrieve(int position, int size) {
        return map.get(position);
    }

    @Override
    public void free() {
        this.map.clear();
    }

    @Override
    public int position() {
        return this.position.intValue();
    }

    @Override
    public int capacity() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void close() throws IOException {
        // ignore
    }
}
