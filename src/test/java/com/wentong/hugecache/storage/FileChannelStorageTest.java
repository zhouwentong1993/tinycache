package com.wentong.hugecache.storage;

import com.wentong.util.PathUtil;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class FileChannelStorageTest {

    @Test
    public void put() {
        FileChannelStorage storage = new FileChannelStorage(PathUtil.getSystemDefaultPath() + "/data/filechannel", 1024 * 1024 * 1024);
        storage.put(0, "hello world".getBytes(StandardCharsets.UTF_8));
        byte[] retrieve = storage.retrieve(0, 1);
        Assert.assertEquals(1, retrieve.length);
        Assert.assertEquals("h", new String(retrieve));
        storage.put(0, "new world".getBytes(StandardCharsets.UTF_8));
        retrieve = storage.retrieve(0, 1);
        Assert.assertEquals(1, retrieve.length);
        Assert.assertEquals("n", new String(retrieve));
        storage.put(4, "dlrow".getBytes(StandardCharsets.UTF_8));
        retrieve = storage.retrieve(0, 11);
        Assert.assertEquals(11, retrieve.length);
        Assert.assertEquals("new dlrowld", new String(retrieve));

        retrieve = storage.retrieve(1000, 1);
        Assert.assertEquals(0, retrieve.length);
    }

    @Test
    public void close() throws IOException { // NOSONAR
        // No need to assert
        FileChannelStorage storage = new FileChannelStorage(PathUtil.getSystemDefaultPath() + "/data/filechannel", 1024 * 1024 * 1024);
        storage.close();
    }
}