package com.wentong.hugecache.storage;

import com.wentong.util.PathUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FileChannelStorageTest {

    @Test
    void put() {
        FileChannelStorage storage = new FileChannelStorage(PathUtil.getSystemDefaultPath() + "/data/filechannel", 1024 * 1024 * 1024);
        storage.put(0, "hello world".getBytes(StandardCharsets.UTF_8));
        byte[] retrieve = storage.retrieve(0, 1);
        assertEquals(1, retrieve.length);
        assertEquals("h", new String(retrieve));
        storage.put(0, "new world".getBytes(StandardCharsets.UTF_8));
        retrieve = storage.retrieve(0, 1);
        assertEquals(1, retrieve.length);
        assertEquals("n", new String(retrieve));
        storage.put(4, "dlrow".getBytes(StandardCharsets.UTF_8));
        retrieve = storage.retrieve(0, 11);
        assertEquals(11, retrieve.length);
        assertEquals("new dlrowld", new String(retrieve));

        retrieve = storage.retrieve(1000, 1);
        assertEquals(0, retrieve.length);
    }

    @Test
    void close() throws IOException { // NOSONAR
        // No need to assert
        FileChannelStorage storage = new FileChannelStorage(PathUtil.getSystemDefaultPath() + "/data/filechannel", 1024 * 1024 * 1024);
        storage.close();
    }

    @AfterEach
    void tearDown() throws Exception {
        File file = new File(PathUtil.getSystemDefaultPath() + "/data/filechannel");
        for (File listFile : file.listFiles()) {
            listFile.delete();
        }
    }
}