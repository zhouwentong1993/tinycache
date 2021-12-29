package com.wentong.hugecache.block;

import com.wentong.hugecache.Pointer;
import com.wentong.hugecache.StorageMode;
import com.wentong.util.PathUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BlockManagerTest {

    BlockManager manager;

    @BeforeEach
    void setup() {
        this.manager = new BlockManager(1, StorageMode.FILE_CHANNEL, PathUtil.getSystemDefaultPath() + "/data/filechannel", 12);
    }


    @Test
    void testPutWhenDataNotFull() {
        BlockManager manager = new BlockManager(1, StorageMode.FILE_CHANNEL, PathUtil.getSystemDefaultPath() + "/data/filechannel", 100);
        Pointer pointer = manager.put("hello world".getBytes(StandardCharsets.UTF_8));
        assertEquals(0, pointer.getOffset());
        assertEquals(11, pointer.getLength());
    }

    @Test
    void testPutWhenDataFull() {
        long start = System.currentTimeMillis();

        manager.getMonitorThread().stop();
        Pointer pointer1 = manager.put("hh".getBytes(StandardCharsets.UTF_8));
        Pointer pointer2 = manager.put("hello world".getBytes(StandardCharsets.UTF_8));
        // 扩容需要 3s，从阻塞队列等 3s。
        assertTrue(System.currentTimeMillis() - start > 3000);
        assertEquals(pointer1.getOffset(), pointer2.getOffset());
    }

    @Test
    void testTwoThreadInvokePutWhenNotFull() throws Exception {
        BlockManager manager = new BlockManager(1, StorageMode.FILE_CHANNEL, PathUtil.getSystemDefaultPath() + "/data/filechannel", 120);
        final Pointer[] ps = {null, null, null};
        Thread t1 = new Thread(() -> {
            ps[0] = manager.put("hh".getBytes(StandardCharsets.UTF_8));
            ps[1] = manager.put("hello world".getBytes(StandardCharsets.UTF_8));
        });
        t1.start();
        // to let t1 start first
        TimeUnit.MILLISECONDS.sleep(1);
        Thread t2 = new Thread(() -> ps[2] = manager.put("hello world".getBytes(StandardCharsets.UTF_8)));
        t2.start();
        t1.join();
        t2.join();
        // 扩容需要 3s，从阻塞队列等 3s。
        assertEquals(0, ps[0].getOffset());
        assertEquals(2, ps[1].getOffset());
        assertEquals(13, ps[2].getOffset());
    }

    @Test
    void testTwoThreadInvokePutWhenFull() throws Exception {

        final Pointer[] ps = {null, null, null};
        Thread t1 = new Thread(() -> {
            ps[0] = manager.put("hh".getBytes(StandardCharsets.UTF_8));
            ps[1] = manager.put("hello world".getBytes(StandardCharsets.UTF_8));
        });
        t1.setName("t1");
        t1.start();
        // to let t1 start first
        TimeUnit.MILLISECONDS.sleep(1);
        Thread t2 = new Thread(() -> ps[2] = manager.put("hello world".getBytes(StandardCharsets.UTF_8)));
        t2.setName("t2");
        t2.start();
        t1.join();
        t2.join();
        // 扩容需要 3s，从阻塞队列等 3s。
        assertEquals(0, ps[0].getOffset());
        assertEquals(0, ps[1].getOffset());
        assertEquals(0, ps[2].getOffset());
    }

    @Test
    void retrieve() {

        Pointer pointer = manager.put("hello".getBytes(StandardCharsets.UTF_8));
        byte[] retrieve = manager.retrieve(pointer);
        assertEquals("hello", new String(retrieve));
    }

    @Test
    void remove() {

        Pointer pointer = manager.put("hello".getBytes(StandardCharsets.UTF_8));
        byte[] remove = manager.remove(pointer);
        assertEquals("hello", new String(remove));
    }

    @Test
    void update() {

        Pointer pointer = manager.put("hello".getBytes(StandardCharsets.UTF_8));
        Pointer updatePointer = manager.update(pointer, "olleh".getBytes(StandardCharsets.UTF_8));
        assertEquals(0, updatePointer.getOffset());
        assertEquals("olleh", new String(manager.remove(pointer)));
    }

    @Test
    void dirtyPage() {
        assertEquals(0, manager.dirtyPage());
    }

    @Test
    void freePage() {
        assertEquals(12, manager.freePage());
    }

    @Test
    void monitorThread() throws Exception {
        manager.getMonitorThread().runNow();
        TimeUnit.MILLISECONDS.sleep(10);
        assertEquals(1, manager.queueSize());
    }

    @AfterEach
    void tearDown() {
        File file = new File(PathUtil.getSystemDefaultPath() + "/data/filechannel");
        for (File listFile : file.listFiles()) {
            listFile.delete();
        }
    }
}