package com.wentong.hugecache.storage;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 基于内存实现的存储
 */
public class MemoryStorage implements Storage {

    private final Map<Integer, byte[]> map;

    public MemoryStorage() {
        this.map = new ConcurrentHashMap<>();
    }

    @Override
    public void put(int position, byte[] data) {
        map.put(position, data);
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
    public void close() throws IOException {
        // ignore
    }
}
