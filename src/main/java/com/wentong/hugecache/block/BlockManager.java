package com.wentong.hugecache.block;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.wentong.exception.FileFullException;
import com.wentong.hugecache.Pointer;
import com.wentong.hugecache.StorageMode;
import lombok.SneakyThrows;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class BlockManager implements Block {

    private final BlockingQueue<Block> queue = new LinkedBlockingQueue<>();
    private Block currentBlock;
    private ReentrantReadWriteLock.ReadLock readLock;
    private ReentrantReadWriteLock.WriteLock writeLock;
    private final Map<Block, ReentrantReadWriteLock> locks = new HashMap<>();
    private final String dir;
    private final StorageMode mode;
    private final int capacity;
    private static final int MIN_BLOCK = 5;
    private ScheduledExecutorService monitorBlockThread = Executors.newScheduledThreadPool(1, new ThreadFactoryBuilder().setNameFormat("monitor-block-thread").build());

    public BlockManager(int initialization, StorageMode mode, String dir, int capacity) {
        this.dir = dir;
        this.mode = mode;
        this.capacity = capacity;
        if (initialization < 1) {
            initialization = 10;
        }
        for (int i = 0; i < initialization; i++) {
            Block block = new BlockStorage(mode, dir, capacity);
            queue.add(block);
            locks.put(block, new ReentrantReadWriteLock());
        }
        currentBlock = queue.poll();
        this.writeLock = locks.get(currentBlock).writeLock();
        this.readLock = locks.get(currentBlock).readLock();

        monitorBlockThread.scheduleAtFixedRate(() -> {
            if (queue.size() < MIN_BLOCK) {
                BlockStorage newBlock = new BlockStorage(mode, dir, capacity);
                ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
                queue.add(newBlock);
                locks.put(newBlock, lock);
            }
        }, 10, 10, TimeUnit.SECONDS);
    }

    @SneakyThrows
    @Override
    public Pointer put(byte[] data) {
        this.writeLock.lock();
        try {
            return currentBlock.put(data);
        } catch (FileFullException ex) {
            this.locks.remove(currentBlock);
            // 存储空间满了，需要重新获取文件。从队列里获取或者重新创建。
            Block block = queue.poll(3, TimeUnit.SECONDS);
            if (block != null) {
                currentBlock = block;
            } else {
                currentBlock = new BlockStorage(mode, dir, capacity);
            }
            ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
            this.writeLock = lock.writeLock();
            this.readLock = lock.readLock();
            return currentBlock.put(data);
        } finally {
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

    private void copy(Block source, Block target) {

    }

    private void lockAll() {
        this.readLock.lock();
        this.writeLock.lock();
    }


}
