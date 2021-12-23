package com.wentong.hugecache.block;

import com.wentong.hugecache.Pointer;
import com.wentong.hugecache.StorageMode;
import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

public class BlockStorageTest {

    @Test
    public void put() {
        BlockStorage storage = new BlockStorage(StorageMode.MMAP, "/Users/renmai/IdeaProjects/tinycache/data/mmap", 1024);
        storage.put("hello world".getBytes(StandardCharsets.UTF_8));
        Assert.assertEquals(1024, storage.getCapacity());
        Assert.assertEquals(0, storage.dirtyPage());
        Assert.assertEquals(1024 - "hello world".getBytes(StandardCharsets.UTF_8).length, storage.freePage());
    }

    @Test
    public void retrieve() {
        BlockStorage storage = new BlockStorage(StorageMode.MMAP, "/Users/renmai/IdeaProjects/tinycache/data/mmap", 1024);
        Pointer pointer = storage.put("hello world".getBytes(StandardCharsets.UTF_8));
        byte[] retrieve = storage.retrieve(pointer);
        Assert.assertEquals("hello world", new String(retrieve));

        retrieve = storage.retrieve(new Pointer(1, 10));
        Assert.assertEquals("ello world", new String(retrieve));

        retrieve = storage.retrieve(new Pointer(100, 10));
        Assert.assertEquals(0, retrieve.length);
    }

    @Test
    public void remove() {
        BlockStorage storage = new BlockStorage(StorageMode.MMAP, "/Users/renmai/IdeaProjects/tinycache/data/mmap", 1024);
        Pointer pointer = storage.put("hello world".getBytes(StandardCharsets.UTF_8));
        byte[] remove = storage.remove(pointer);
        Assert.assertEquals("hello world", new String(remove));
        Assert.assertEquals("hello world".getBytes(StandardCharsets.UTF_8).length, storage.dirtyPage());
    }

    @Test
    public void updateWhenFitData() {
        BlockStorage storage = new BlockStorage(StorageMode.MMAP, "/Users/renmai/IdeaProjects/tinycache/data/mmap", 1024);
        Pointer pointer = storage.put("hello world".getBytes(StandardCharsets.UTF_8));
        Pointer updatePointer = storage.update(pointer, "new word".getBytes(StandardCharsets.UTF_8));
        Assert.assertEquals(pointer.getOffset(), updatePointer.getOffset());
        Assert.assertEquals(8, updatePointer.getLength());
        Assert.assertEquals("hello world".getBytes(StandardCharsets.UTF_8).length - "new word".getBytes(StandardCharsets.UTF_8).length, storage.dirtyPage());
    }

    @Test
    public void updateWhenNotFitData() {
        BlockStorage storage = new BlockStorage(StorageMode.MMAP, "/Users/renmai/IdeaProjects/tinycache/data/mmap", 1024);
        Pointer pointer = storage.put("word".getBytes(StandardCharsets.UTF_8));
        Pointer updatePointer = storage.update(pointer, "new word".getBytes(StandardCharsets.UTF_8));
        Assert.assertNotEquals(pointer.getOffset(), updatePointer.getOffset());
        Assert.assertEquals(4, updatePointer.getOffset());
        Assert.assertEquals(4, storage.dirtyPage());
        Assert.assertEquals(1024 - 8 - 4, storage.freePage());
    }
}