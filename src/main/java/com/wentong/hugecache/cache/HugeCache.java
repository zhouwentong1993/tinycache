package com.wentong.hugecache.cache;

import com.wentong.hugecache.Pointer;
import com.wentong.hugecache.ServiceThread;
import com.wentong.hugecache.block.BlockManager;
import com.wentong.hugecache.storage.Storage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class HugeCache<K> implements Cache<K> {

    private final BlockManager blockManager;
    private static final double MAX_DIRTY_RATE = 0.5;

    public HugeCache(BlockManager blockManager) {
        this.blockManager = blockManager;
        CleanThread cleanThread = new CleanThread(30, TimeUnit.SECONDS);
        cleanThread.start();
        TTLThread ttlThread = new TTLThread(5, TimeUnit.SECONDS);
        ttlThread.start();
    }

    private final Map<K, Pointer> kPointerMap = new ConcurrentHashMap<>();
    private final Map<K, Long> ttlMap = new ConcurrentHashMap<>();

    @Override

    public void put(K k, byte[] v) {
        ttlMap.remove(k);
        Pointer pointer = blockManager.put(v);
        kPointerMap.put(k, pointer);
    }

    @Override
    public void put(K k, byte[] v, long time, TimeUnit timeUnit) {
        if (time == -1) {
            ttlMap.remove(k);
        }
        put(k, v);
        ttlMap.put(k, timeUnit.toMillis(time) + System.currentTimeMillis());
    }

    @Override
    public byte[] get(K k) {
        Long time = ttlMap.getOrDefault(k, 0L);
        if (System.currentTimeMillis() - time < 0) {
            return new byte[0];
        }
        Pointer pointer = kPointerMap.get(k);
        return blockManager.retrieve(pointer);
    }

    @Override
    public void delete(K k) {
        Pointer pointer = kPointerMap.get(k);
        blockManager.remove(pointer);
        ttlMap.remove(k);
    }

    @Override
    public void ttl(K k, long time, TimeUnit timeUnit) {
        ttlMap.computeIfPresent(k, (key, value) -> timeUnit.toMillis(time));
    }

    @Override
    public void close() {

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
            // 脏页数据过多了，触发 clean 操作
            if (blockManager.dirtyPage() / (double) blockManager.getCapacity() > MAX_DIRTY_RATE) {
                HugeCache.this.blockManager.stopRunning();
                try {
                    Storage storage = blockManager.getCurrentStorage();
                    blockManager.copyStorage();
                    kPointerMap.forEach((k, v) -> {
                        if (v.getStorage() == storage) {
                            kPointerMap.put(k, blockManager.put(v.getStorage().retrieve(v.getOffset(), v.getLength())));
                        }
                    });
                } finally {
                    HugeCache.this.blockManager.continueRunning();
                }
            }
        }

        @Override
        public String threadName() {
            return this.getClass().getName();
        }
    }

    class TTLThread extends ServiceThread {

        public TTLThread(int awaitTime, TimeUnit timeUnit) {
            super(awaitTime, timeUnit);
        }

        @Override
        public void cleanUp() {
            // do noting
        }

        @Override
        public void process() {
            HugeCache.this.ttlMap.forEach((k, v) -> {
                long now = System.currentTimeMillis();
                if (now - v < 0) {
                    HugeCache.this.delete(k);
                    ttlMap.remove(k);
                }
            });
        }

        @Override
        public String threadName() {
            return this.getClass().getName();
        }
    }

}
