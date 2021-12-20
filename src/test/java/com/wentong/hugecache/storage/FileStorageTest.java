package com.wentong.hugecache.storage;

import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

public class FileStorageTest {

    @Test
    public void put() throws Exception {
        FileStorage fileStorage = new FileStorage("/Users/renmai/IdeaProjects/tinycache/data");
        fileStorage.put(0, "hello world".getBytes(StandardCharsets.UTF_8));
        byte[] retrieve = fileStorage.retrieve(1, "ello world".getBytes(StandardCharsets.UTF_8).length);
        Assert.assertEquals(retrieve.length, "hello world".getBytes(StandardCharsets.UTF_8).length - 1);
        Assert.assertEquals("ello world", new String(retrieve));
    }

    @Test
    public void retrieve() {
    }

    @Test
    public void free() {
    }

    @Test
    public void close() {
    }
}