package com.wentong.hugecache.cache;

import com.wentong.hugecache.Pointer;
import com.wentong.hugecache.ServiceThread;
import com.wentong.hugecache.block.BlockManager;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class HugeCache<K> implements Cache<K> {

    private BlockManager blockManager;
    private static final double MAX_DIRTY_RATE = 0.5;

    private final Map<K, Pointer> map = new ConcurrentHashMap<>();

    @Override
    public void put(K k, byte[] v) {
        Pointer pointer = blockManager.put(v);
        map.put(k, pointer);
    }

    @Override
    public byte[] get(K k) {
        Pointer pointer = map.get(k);
        return blockManager.retrieve(pointer);
    }

    @Override
    public void delete(K k) {
        Pointer pointer = map.get(k);
        blockManager.remove(pointer);
    }

    @Override
    public void ttl(K k) {

    }

    @Override
    public void close() throws IOException {

    }

    public Pointer getByK(K k) {
        return map.get(k);
    }

    public void put(K k, Pointer pointer) {

    }

    class CleanThread extends ServiceThread {

        public CleanThread(int awaitTime, TimeUnit timeUnit) {
            super(awaitTime, timeUnit);
        }

        @Override
        public void cleanUp() {
            // do noting
        }

        @Override
        public void process() {
            if (blockManager.dirtyPage() / (double) blockManager.getCapacity() > MAX_DIRTY_RATE) {

            }
        }

        @Override
        public String threadName() {
            return "cleanThread";
        }
    }
}
