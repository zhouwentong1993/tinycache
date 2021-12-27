package com.wentong.hugecache.storage;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MemoryStorageTest {

    @Test
    void put() {
        MemoryStorage memoryStorage = new MemoryStorage();
        memoryStorage.put(2, "hello world:2".getBytes(StandardCharsets.UTF_8));
        assertEquals("hello world:2", new String(memoryStorage.retrieve(2, "hello world:2".getBytes(StandardCharsets.UTF_8).length)));
    }

    @Test
    void retrieve() {
        MemoryStorage memoryStorage = new MemoryStorage();
        assertNull(memoryStorage.retrieve(0, 10));
        memoryStorage.put(2, "hello world:2".getBytes(StandardCharsets.UTF_8));
        assertEquals("hello world:2", new String(memoryStorage.retrieve(2, "hello world:2".getBytes(StandardCharsets.UTF_8).length)));
    }

    @Test
    void free() {
        MemoryStorage memoryStorage = new MemoryStorage();
        memoryStorage.put(2, "hello world:2".getBytes(StandardCharsets.UTF_8));
        memoryStorage.free();
        assertNull(memoryStorage.retrieve(2, "hello world:2".getBytes(StandardCharsets.UTF_8).length));
    }
}