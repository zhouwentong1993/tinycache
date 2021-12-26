package com.wentong.hugecache.storage;

import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

public class MemoryStorageTest {

    @Test
    public void put() {
        MemoryStorage memoryStorage = new MemoryStorage();
        memoryStorage.put(2, "hello world:2".getBytes(StandardCharsets.UTF_8));
        Assert.assertEquals("hello world:2", new String(memoryStorage.retrieve(2, "hello world:2".getBytes(StandardCharsets.UTF_8).length)));
    }

    @Test
    public void retrieve() {
        MemoryStorage memoryStorage = new MemoryStorage();
        Assert.assertNull(memoryStorage.retrieve(0, 10));
        memoryStorage.put(2, "hello world:2".getBytes(StandardCharsets.UTF_8));
        Assert.assertEquals("hello world:2", new String(memoryStorage.retrieve(2, "hello world:2".getBytes(StandardCharsets.UTF_8).length)));
    }

    @Test
    public void free() {
        MemoryStorage memoryStorage = new MemoryStorage();
        memoryStorage.put(2, "hello world:2".getBytes(StandardCharsets.UTF_8));
        memoryStorage.free();
        Assert.assertNull(memoryStorage.retrieve(2, "hello world:2".getBytes(StandardCharsets.UTF_8).length));
    }
}