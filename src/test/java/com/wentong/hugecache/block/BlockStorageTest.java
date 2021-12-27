package com.wentong.hugecache.block;

import com.wentong.hugecache.Pointer;
import com.wentong.hugecache.StorageMode;
import com.wentong.util.PathUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class BlockStorageTest {

    @Test
    void put() {
        BlockStorage storage = new BlockStorage(StorageMode.MMAP, PathUtil.getSystemDefaultPath() + "/data/mmap", 1024);
        storage.put("hello world".getBytes(StandardCharsets.UTF_8));
        assertEquals(1024, storage.getCapacity());
        assertEquals(0, storage.dirtyPage());
        assertEquals(1024 - "hello world".getBytes(StandardCharsets.UTF_8).length, storage.freePage());
    }

    @Test
    void retrieve() {
        BlockStorage storage = new BlockStorage(StorageMode.MMAP, PathUtil.getSystemDefaultPath() + "/data/mmap", 1024);
        Pointer pointer = storage.put("hello world".getBytes(StandardCharsets.UTF_8));
        byte[] retrieve = storage.retrieve(pointer);
        assertEquals("hello world", new String(retrieve));

        retrieve = storage.retrieve(new Pointer(1, 10));
        assertEquals("ello world", new String(retrieve));

        retrieve = storage.retrieve(new Pointer(100, 10));
        assertEquals(0, retrieve.length);
    }

    @Test
    void remove() {
        BlockStorage storage = new BlockStorage(StorageMode.MMAP, PathUtil.getSystemDefaultPath() + "/data/mmap", 1024);
        Pointer pointer = storage.put("hello world".getBytes(StandardCharsets.UTF_8));
        byte[] remove = storage.remove(pointer);
        assertEquals("hello world", new String(remove));
        assertEquals("hello world".getBytes(StandardCharsets.UTF_8).length, storage.dirtyPage());
    }

    @Test
    void updateWhenFitData() {
        BlockStorage storage = new BlockStorage(StorageMode.MMAP, PathUtil.getSystemDefaultPath() + "/data/mmap", 1024);
        Pointer pointer = storage.put("hello world".getBytes(StandardCharsets.UTF_8));
        Pointer updatePointer = storage.update(pointer, "new word".getBytes(StandardCharsets.UTF_8));
        assertEquals(pointer.getOffset(), updatePointer.getOffset());
        assertEquals(8, updatePointer.getLength());
        assertEquals("hello world".getBytes(StandardCharsets.UTF_8).length - "new word".getBytes(StandardCharsets.UTF_8).length, storage.dirtyPage());
    }

    @Test
    void updateWhenNotFitData() {
        BlockStorage storage = new BlockStorage(StorageMode.MMAP, PathUtil.getSystemDefaultPath() + "/data/mmap", 1024);
        Pointer pointer = storage.put("word".getBytes(StandardCharsets.UTF_8));
        Pointer updatePointer = storage.update(pointer, "new word".getBytes(StandardCharsets.UTF_8));
        assertNotEquals(pointer.getOffset(), updatePointer.getOffset());
        assertEquals(4, updatePointer.getOffset());
        assertEquals(4, storage.dirtyPage());
        assertEquals(1024 - 8 - 4, storage.freePage());
    }

    @AfterEach
    void tearDown() throws Exception {
        File file = new File(PathUtil.getSystemDefaultPath() + "/data/mmap");
        for (File listFile : file.listFiles()) {
            listFile.delete();
        }
    }
}