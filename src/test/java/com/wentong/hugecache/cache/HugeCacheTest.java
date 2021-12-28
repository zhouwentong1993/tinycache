package com.wentong.hugecache.cache;

import com.wentong.hugecache.StorageMode;
import com.wentong.hugecache.block.BlockManager;
import com.wentong.util.PathUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HugeCacheTest {

    private HugeCache<String> cache;

    @AfterEach
    void tearDown() {
        File file = new File(PathUtil.getSystemDefaultPath() + "/data/filechannel");
        for (File listFile : file.listFiles()) {
            listFile.delete();
        }
    }

    @BeforeEach
    void setup() {
        cache = new HugeCache<>(new BlockManager(1, StorageMode.FILE_CHANNEL, PathUtil.getSystemDefaultPath() + "/data/filechannel", 1024));
    }

    @Test
    void put() {
        cache.put("hello", "hello".getBytes(StandardCharsets.UTF_8));
        byte[] bytes = cache.get("hello");
        assertEquals("hello", new String(bytes));
        assertEquals(1, cache.getCacheStatistics().getGetCounter());
        assertEquals(1, cache.getCacheStatistics().getHitCounter());
        assertEquals(0, cache.getCacheStatistics().getMissCounter());
    }

    @Test
    void ttlOnExistKey() throws Exception {
        cache.put("hello", "hello".getBytes(StandardCharsets.UTF_8));
        byte[] bytes = cache.get("hello");
        assertEquals("hello", new String(bytes));
        cache.getTtlThread().stop();
        cache.ttl("hello", 1, TimeUnit.SECONDS);
        TimeUnit.SECONDS.sleep(1);
        bytes = cache.get("hello");
        assertEquals(0, bytes.length);
    }

    @Test
    void deleteWhenKeyNotExist() {
        byte[] nos = cache.delete("no");
        assertEquals(0, nos.length);
    }

    @Test
    void deleteWhenKeyExistNottl() {
        cache.put("hello", "hello".getBytes(StandardCharsets.UTF_8));
        byte[] bytes = cache.delete("hello");
        assertEquals("hello", new String(bytes));
        bytes = cache.get("hello");
        assertEquals(0, bytes.length);
        assertEquals(1, cache.getCacheStatistics().getMissCounter());
    }

    @Test
    void ttlWhenInvokeGet() throws Exception {
        cache.put("hello", "hello".getBytes(StandardCharsets.UTF_8), 1, TimeUnit.SECONDS);
        cache.getTtlThread().stop();
        TimeUnit.SECONDS.sleep(2);
        byte[] bytes = cache.get("hello");
        assertEquals(0, bytes.length);
        assertEquals(1, cache.getCacheStatistics().getMissCounter());
    }

    @Test
    void ttlWhenThreadScan() throws Exception {
        cache.put("hello", "hello".getBytes(StandardCharsets.UTF_8), 1, TimeUnit.SECONDS);
        TimeUnit.SECONDS.sleep(2);
        final byte[] bytes = cache.get("hello");
        assertEquals(0, bytes.length);
        assertEquals(1, cache.getCacheStatistics().getMissCounter());
    }

    @Test
    void close() {
    }
}