package com.wentong.hugecache.storage;

import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

public class DirectBufferStorageTest {

    @Test
    public void putAndRetrieve() {

        DirectBufferStorage directBufferStorage = new DirectBufferStorage(1024);
        directBufferStorage.put(0, "hello".getBytes(StandardCharsets.UTF_8));
        Assert.assertEquals("hello", new String(directBufferStorage.retrieve(0, "hello".getBytes(StandardCharsets.UTF_8).length)));
        byte[] retrieve = directBufferStorage.retrieve(1, "ello".getBytes(StandardCharsets.UTF_8).length);
        Assert.assertEquals("ello", new String(retrieve));

    }

}