package com.wentong.hugecache.storage;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DirectBufferStorageTest {

    @Test
    void putAndRetrieve() {

        DirectBufferStorage directBufferStorage = new DirectBufferStorage(1024);
        directBufferStorage.put(0, "hello".getBytes(StandardCharsets.UTF_8));
        assertEquals("hello", new String(directBufferStorage.retrieve(0, "hello".getBytes(StandardCharsets.UTF_8).length)));
        byte[] retrieve = directBufferStorage.retrieve(1, "ello".getBytes(StandardCharsets.UTF_8).length);
        assertEquals("ello", new String(retrieve));

    }

}