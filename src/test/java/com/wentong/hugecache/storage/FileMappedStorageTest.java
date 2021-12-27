package com.wentong.hugecache.storage;

import com.wentong.util.PathUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FileMappedStorageTest {

    int _1MB = 1024 * 1024;

    @Test
    void putAndRetrieve() throws Exception {
        FileMappedStorage fileMappedStorage = new FileMappedStorage(PathUtil.getSystemDefaultPath() + "/data/mmap", _1MB);
        fileMappedStorage.put(0, "hello world".getBytes(StandardCharsets.UTF_8));
        byte[] retrieve = fileMappedStorage.retrieve(0, "ello world".getBytes(StandardCharsets.UTF_8).length);
        assertEquals(retrieve.length, "hello world".getBytes(StandardCharsets.UTF_8).length - 1);
        assertEquals("hello worl", new String(retrieve));
        fileMappedStorage.put(12, "hello world12".getBytes(StandardCharsets.UTF_8));
        retrieve = fileMappedStorage.retrieve(12, "hello world12".getBytes(StandardCharsets.UTF_8).length);
        assertEquals("hello world12", new String(retrieve));

    }

    @Test
    void free() throws Exception { // NOSONAR
        FileMappedStorage fileMappedStorage = new FileMappedStorage(PathUtil.getSystemDefaultPath() + "/data/mmap", _1MB);
        // do noting，can't assert
        fileMappedStorage.free();
    }

    @Test
    void close() throws Exception { // NOSONAR
        FileMappedStorage fileMappedStorage = new FileMappedStorage(PathUtil.getSystemDefaultPath() + "/data/mmap", _1MB);
        // do noting，can't assert
        fileMappedStorage.close();
    }

    @AfterEach
    void tearDown() throws Exception {
        File file = new File(PathUtil.getSystemDefaultPath() + "/data/mmap");
        for (File listFile : file.listFiles()) {
            listFile.delete();
        }
    }
}