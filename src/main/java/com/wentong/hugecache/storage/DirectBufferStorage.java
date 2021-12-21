package com.wentong.hugecache.storage;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 基于堆外内存的存储实现
 */
public class DirectBufferStorage implements Storage {

    private static final int DEFAULT_BUFFER_SIZE = 1024 * 1024;
    private final ByteBuffer buffer;
    private final AtomicInteger currPosition;

    public DirectBufferStorage(int bufferSize) {
        this.buffer = ByteBuffer.allocateDirect(bufferSize);
        currPosition = new AtomicInteger(0);
    }

    public DirectBufferStorage() {
        this(DEFAULT_BUFFER_SIZE);
    }

    @Override
    public void put(int position, byte[] data) {
        buffer.put(data, position, data.length);
        if (position > currPosition.intValue()) {
            this.currPosition.set(position);
        }
    }

    /*
     * 需要考虑并发问题，position 需要不停调换，或者考虑复制机制，返回 ByteBuffer。
     */
    @Override
    public byte[] retrieve(int position, int size) {
        byte[] bytes = new byte[size];
        int oldPosition = buffer.position();
        buffer.position(position);
        buffer.get(bytes, 0, size);
        buffer.position(oldPosition);
        return bytes;
    }

    @Override
    public void free() {
        // ignore
    }

    @Override
    public int position() {
        return currPosition.intValue();
    }

    @Override
    public void close() {
        // ignore
    }
}
