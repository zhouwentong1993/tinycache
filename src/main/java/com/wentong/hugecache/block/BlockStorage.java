package com.wentong.hugecache.block;

import com.wentong.hugecache.Pointer;
import com.wentong.hugecache.StorageMode;
import com.wentong.hugecache.storage.*;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class BlockStorage implements Block, Closeable {

    private final Storage storage;

    private final AtomicInteger currentPosition;
    private final AtomicInteger dirtySize; // 不太好判断，当故障重启时怎么办？先暂定为 0 吧。
    private final AtomicInteger freeSize;
    private final int capacity;

    public BlockStorage(StorageMode storageMode, String dir, int capacity) {
        this.capacity = capacity;
        switch (storageMode) {
            case MEMORY:
                storage = new MemoryStorage();
                break;
            case DIRECT_BUFFER:
                storage = new DirectBufferStorage(capacity);
                break;
            case MMAP:
                storage = new FileMappedStorage(dir, capacity);
                break;
            case FILE_CHANNEL:
                storage = new FileChannelStorage(dir, capacity);
                break;
            default:
                throw new IllegalArgumentException("Unsupported storage mode: " + storageMode);
        }
        this.currentPosition = new AtomicInteger(this.storage.position());
        this.freeSize = new AtomicInteger(this.capacity - currentPosition.intValue());
        this.dirtySize = new AtomicInteger(0);
    }

    @Override
    public Pointer put(byte[] data) {
        int position = currentPosition.intValue();
        storage.put(position, data);
        currentPosition.set(data.length + position);
        freeSize.addAndGet(-data.length);
        return new Pointer(position, data.length);
    }

    @Override
    public byte[] retrieve(Pointer pointer) {
        return storage.retrieve(pointer.getOffset(), pointer.getLength());
    }

    /**
     * 不会直接删除，标记删除，等待后台回收线程
     *
     * @param pointer 数据指针
     * @return 数据指针所在数据
     */
    @Override
    public byte[] remove(Pointer pointer) {
        byte[] retrieve = storage.retrieve(pointer.getOffset(), pointer.getLength());
        dirtySize.addAndGet(pointer.getLength());
        return retrieve;
    }

    @Override
    public Pointer update(Pointer pointer, byte[] data) {
        byte[] sourceData = storage.retrieve(pointer.getOffset(), pointer.getLength());
        if (data.length <= sourceData.length) {
            storage.put(pointer.getOffset(), data);
            pointer.setLength(data.length);
            dirtySize.addAndGet(sourceData.length - data.length);
            return pointer;
        } else {
            Pointer newPointer = put(data);
            dirtySize.addAndGet(sourceData.length);
            return newPointer;
        }
    }

    @Override
    public int dirtyPage() {
        return this.dirtySize.intValue();
    }

    @Override
    public int freePage() {
        return this.freeSize.intValue();
    }

    @Override
    public int getCapacity() {
        return this.capacity;
    }


    @Override
    public void close() throws IOException {
        this.storage.close();
    }


}
