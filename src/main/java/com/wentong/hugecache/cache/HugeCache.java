package com.wentong.hugecache.cache;

import com.wentong.hugecache.Pointer;
import com.wentong.hugecache.ServiceThread;
import com.wentong.hugecache.Statistics;
import com.wentong.hugecache.block.BlockManager;
import com.wentong.hugecache.storage.Storage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class HugeCache<K> implements Cache<K> {

    private final BlockManager blockManager;
    private static final double MAX_DIRTY_RATE = 0.5;
    private final ServiceThread cleanThread;
    private final ServiceThread ttlThread;

    public HugeCache(BlockManager blockManager) {
        this.blockManager = blockManager;
        this.cleanThread = new CleanThread(30, TimeUnit.SECONDS);
        cleanThread.start();
        this.ttlThread = new TTLThread(500, TimeUnit.MILLISECONDS);
        ttlThread.start();
    }

    private final Map<K, Pointer> kPointerMap = new ConcurrentHashMap<>();
    private final Map<K, Long> ttlMap = new ConcurrentHashMap<>();

    private final AtomicInteger getCounter = new AtomicInteger(0);
    private final AtomicInteger hitCounter = new AtomicInteger(0);
    private final AtomicInteger missCounter = new AtomicInteger(0);


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
        getCounter.incrementAndGet();
        Long time = ttlMap.getOrDefault(k, -1L);
        if (time != -1 && System.currentTimeMillis() - time >= 0) {
            missCounter.incrementAndGet();
            return new byte[0];
        }

        Pointer pointer = kPointerMap.get(k);

        if (pointer == null) {
            missCounter.incrementAndGet();
            return new byte[0];
        } else {
            byte[] retrieve = blockManager.retrieve(pointer);
            if (retrieve.length > 0) {
                hitCounter.incrementAndGet();
            } else {
                missCounter.incrementAndGet();
            }
            return retrieve;
        }
    }

    @Override
    public byte[] delete(K k) {
        Pointer pointer = kPointerMap.remove(k);
        if (pointer != null) {
            byte[] removedData = blockManager.remove(pointer);
            ttlMap.remove(k);
            return removedData;
        } else {
            return new byte[0];
        }
    }

    @Override
    public void ttl(K k, long time, TimeUnit timeUnit) {
        ttlMap.put(k, System.currentTimeMillis() + timeUnit.toMillis(time));
    }

    @Override
    public void close() {

    }

    public Statistics getCacheStatistics() {
        return new Statistics(getCounter.intValue(), missCounter.intValue(), hitCounter.intValue());
    }

    public ServiceThread getCleanThread() {
        return cleanThread;
    }

    public ServiceThread getTtlThread() {
        return ttlThread;
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
                if (now >= v) {
                    HugeCache.this.delete(k);
                    kPointerMap.remove(k);
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
