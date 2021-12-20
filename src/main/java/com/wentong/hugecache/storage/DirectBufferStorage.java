package com.wentong.hugecache.storage;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * 基于堆外内存的存储实现
 */
public class DirectBufferStorage implements Storage {

    private int bufferSize;
    private ByteBuffer buffer;

    public DirectBufferStorage(int bufferSize) {
        this.bufferSize = bufferSize;
        this.buffer = ByteBuffer.allocateDirect(bufferSize);
    }

    @Override
    public void put(int position, byte[] data) {
        buffer.put(data, position, data.length);
    }

    /*
     * 需要考虑并发问题，position 需要不停调换，或者考虑复制机制，返回 ByteBuffer。
     */
    @Override
    public byte[] retrieve(int position, int size) {
        byte[] bytes = new byte[size];
        int oldPosition = buffer.position();
        buffer.position(position);
        buffer.get(bytes, position, size);
        buffer.position(oldPosition);
        return bytes;
    }

    @Override
    public void free() {
        // ignore
    }

    @Override
    public void close() throws IOException {
        // ignore
    }
}
