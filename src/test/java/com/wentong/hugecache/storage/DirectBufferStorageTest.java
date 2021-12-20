package com.wentong.hugecache.storage;

import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

public class DirectBufferStorageTest {

    @Test
    public void put() {

        DirectBufferStorage directBufferStorage = new DirectBufferStorage(1024);
        directBufferStorage.put(0,"hello".getBytes(StandardCharsets.UTF_8));
        Assert.assertEquals("hello", new String(directBufferStorage.retrieve(0, "hello".getBytes(StandardCharsets.UTF_8).length)));
    }

    @Test
    public void retrieve() {
    }
}