package com.wentong.hugecache.block;

import com.wentong.exception.FileFullException;
import com.wentong.hugecache.Pointer;
import com.wentong.hugecache.ServiceThread;
import com.wentong.hugecache.StorageMode;
import lombok.SneakyThrows;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class BlockManager implements Block {

    private final BlockingQueue<Block> queue = new LinkedBlockingQueue<>();
    private Block currentBlock;
    private final ReentrantReadWriteLock.ReadLock readLock;
    private final ReentrantReadWriteLock.WriteLock writeLock;
    private final String dir;
    private final StorageMode mode;
    private final int capacity;
    private static final int MIN_BLOCK = 5;
    private final MonitorThread thread;

    public BlockManager(int initialization, StorageMode mode, String dir, int capacity) {
        this.dir = dir;
        this.mode = mode;
        this.capacity = capacity;
        if (initialization < 1) {
            initialization = 10;
        }
        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        this.readLock = lock.readLock();
        this.writeLock = lock.writeLock();
        for (int i = 0; i < initialization; i++) {
            Block block = new BlockStorage(mode, dir, capacity);
            queue.add(block);
        }
        currentBlock = queue.poll();
        thread = new MonitorThread(10, TimeUnit.SECONDS);
        thread.start();
    }

    @SneakyThrows
    @Override
    public Pointer put(byte[] data) {
        this.writeLock.lock();
        try {
            return currentBlock.put(data);
        } catch (FileFullException ex) {
            // 存储空间满了，需要重新获取文件。从队列里获取或者重新创建。
            Block block = queue.poll(3, TimeUnit.SECONDS);
            if (block != null) {
                currentBlock = block;
            } else {
                currentBlock = new BlockStorage(mode, dir, capacity);
            }
            // 注意，这里只做一次 catch。如果新放入的文件再次触发 FileFullException，不再处理，交由上层处理。
            // 这种情况通常出现在 ① 文件特别大。② 文件写入极其频繁
            return currentBlock.put(data);
        } finally {
            // 在这里释放锁，如果在 finally 释放锁，writeLock 就不是原来的了。
            this.writeLock.unlock();
        }
    }

    @Override
    public byte[] retrieve(Pointer pointer) {
        readLock.lock();
        try {
            return currentBlock.retrieve(pointer);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public byte[] remove(Pointer pointer) {
        writeLock.lock();
        try {
            return currentBlock.remove(pointer);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public Pointer update(Pointer pointer, byte[] data) {
        writeLock.lock();
        try {
            return currentBlock.update(pointer, data);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public int dirtyPage() {
        return currentBlock.dirtyPage();
    }

    @Override
    public int freePage() {
        return currentBlock.freePage();
    }

    public int getCapacity() {
        return this.capacity;
    }

    public void stopRunning() {
        this.writeLock.lock();
    }

    public void continueRunning() {
        if (writeLock.tryLock()) {
            this.writeLock.unlock();
        }
    }

    class MonitorThread extends ServiceThread {

        public MonitorThread(int awaitTime, TimeUnit timeUnit) {
            super(awaitTime, timeUnit);
        }

        @Override
        public void cleanUp() {

        }

        @Override
        public void process() {
            if (BlockManager.this.queue.size() < MIN_BLOCK) {
                BlockStorage newBlock = new BlockStorage(mode, dir, capacity);
                queue.add(newBlock);
            }
        }

        @Override
        public String threadName() {
            return MonitorThread.class.getName();
        }
    }

    public void stopMonitor() {
        this.thread.stop();
    }

}
