package com.wentong.hugecache.block;

import com.wentong.hugecache.Pointer;
import com.wentong.hugecache.StorageMode;
import com.wentong.util.PathUtil;
import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

public class BlockManagerTest {

    @Test
    public void testPutWhenDataNotFull() {
        BlockManager manager = new BlockManager(1, StorageMode.FILE_CHANNEL, PathUtil.getSystemDefaultPath() + "/data/filechannel", 100);
        Pointer pointer = manager.put("hello world".getBytes(StandardCharsets.UTF_8));
        Assert.assertEquals(0, pointer.getOffset());
        Assert.assertEquals(11, pointer.getLength());
    }

    @Test
    public void testPutWhenDataFull() {
        long start = System.currentTimeMillis();
        BlockManager manager = new BlockManager(1, StorageMode.FILE_CHANNEL, PathUtil.getSystemDefaultPath() + "/data/filechannel", 12);
        Pointer pointer1 = manager.put("hh".getBytes(StandardCharsets.UTF_8));
        Pointer pointer2 = manager.put("hello world".getBytes(StandardCharsets.UTF_8));
        // 扩容需要 3s，从阻塞队列等 3s。
        Assert.assertTrue(System.currentTimeMillis() - start > 3000);
        Assert.assertEquals(pointer1.getOffset(), pointer2.getOffset());
    }

    @Test
    public void testTwoThreadInvokePutWhenNotFull() throws Exception {
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
        Assert.assertEquals(0, ps[0].getOffset());
        Assert.assertEquals(2, ps[1].getOffset());
        Assert.assertEquals(13, ps[2].getOffset());
    }

    @Test

    public void testTwoThreadInvokePutWhenFull() throws Exception {
        BlockManager manager = new BlockManager(1, StorageMode.FILE_CHANNEL, PathUtil.getSystemDefaultPath() + "/data/filechannel", 12);
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
        Assert.assertEquals(0, ps[0].getOffset());
        Assert.assertEquals(0, ps[1].getOffset());
        Assert.assertEquals(0, ps[2].getOffset());
    }

    @Test
    public void retrieve() {
        BlockManager manager = new BlockManager(1, StorageMode.FILE_CHANNEL, PathUtil.getSystemDefaultPath() + "/data/filechannel", 12);
        Pointer pointer = manager.put("hello".getBytes(StandardCharsets.UTF_8));
        byte[] retrieve = manager.retrieve(pointer);
        Assert.assertEquals("hello", new String(retrieve));
    }

    @Test
    public void remove() {
        BlockManager manager = new BlockManager(1, StorageMode.FILE_CHANNEL, PathUtil.getSystemDefaultPath() + "/data/filechannel", 12);
        Pointer pointer = manager.put("hello".getBytes(StandardCharsets.UTF_8));
        byte[] remove = manager.remove(pointer);
        Assert.assertEquals("hello", new String(remove));
    }

    @Test
    public void update() {
        BlockManager manager = new BlockManager(1, StorageMode.FILE_CHANNEL, PathUtil.getSystemDefaultPath() + "/data/filechannel", 12);
        Pointer pointer = manager.put("hello".getBytes(StandardCharsets.UTF_8));
        Pointer updatePointer = manager.update(pointer, "olleh".getBytes(StandardCharsets.UTF_8));
        Assert.assertEquals(0, updatePointer.getOffset());
        Assert.assertEquals("olleh", new String(manager.remove(pointer)));
    }

    @Test
    public void dirtyPage() {
        BlockManager manager = new BlockManager(1, StorageMode.FILE_CHANNEL, PathUtil.getSystemDefaultPath() + "/data/filechannel", 12);
        Assert.assertEquals(0, manager.dirtyPage());
    }

    @Test
    public void freePage() {
        BlockManager manager = new BlockManager(1, StorageMode.FILE_CHANNEL, PathUtil.getSystemDefaultPath() + "/data/filechannel", 12);
        Assert.assertEquals(12, manager.freePage());
    }
}