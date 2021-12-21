package com.wentong.hugecache.storage;

import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

public class FileMappedStorageTest {

    int _1MB = 1024 * 1024;

    @Test
    public void putAndRetrieve() throws Exception {
        FileMappedStorage fileMappedStorage = new FileMappedStorage("/Users/renmai/IdeaProjects/tinycache/data/mmap", _1MB);
        fileMappedStorage.put(0, "hello world".getBytes(StandardCharsets.UTF_8));
        byte[] retrieve = fileMappedStorage.retrieve(0, "ello world".getBytes(StandardCharsets.UTF_8).length);
        Assert.assertEquals(retrieve.length, "hello world".getBytes(StandardCharsets.UTF_8).length - 1);
        Assert.assertEquals("hello worl", new String(retrieve));
        fileMappedStorage.put(12, "hello world12".getBytes(StandardCharsets.UTF_8));
        retrieve = fileMappedStorage.retrieve(12, "hello world12".getBytes(StandardCharsets.UTF_8).length);
        Assert.assertEquals("hello world12", new String(retrieve));

    }

    @Test
    public void free() throws Exception { // NOSONAR
        FileMappedStorage fileMappedStorage = new FileMappedStorage("/Users/renmai/IdeaProjects/tinycache/data", _1MB);
        // do noting，can't assert
        fileMappedStorage.free();
    }

    @Test
    public void close() throws Exception { // NOSONAR
        FileMappedStorage fileMappedStorage = new FileMappedStorage("/Users/renmai/IdeaProjects/tinycache/data", _1MB);
        // do noting，can't assert
        fileMappedStorage.close();
    }
}