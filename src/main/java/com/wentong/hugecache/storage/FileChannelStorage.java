package com.wentong.hugecache.storage;

import com.wentong.exception.FileFullException;
import lombok.SneakyThrows;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 通过 FileChannel 实现的存储
 */
public class FileChannelStorage implements Storage {

    private final FileChannel fileChannel;
    private final RandomAccessFile raf;
    private final AtomicInteger currPosition;
    private final int capacity;

    @SneakyThrows
    public FileChannelStorage(String dir, int capacity) {
        this.capacity = capacity;
        File file = new File(dir);
        if (!file.exists() || file.isFile()) {
            boolean ok = file.mkdir();
            if (!ok) {
                throw new IllegalArgumentException("This dir can't be created!");
            }
        }
        // 暂时不采用读取旧文件的方式。考虑如下：1. 现有设计会导致单元测试过不去（主要原因）2. 如果要更正，需要改底层存储，不光存消息，也要存描述信息（元数据）
        raf = new RandomAccessFile(dir + "/" + System.currentTimeMillis(), "rw");
        fileChannel = raf.getChannel();
        this.currPosition = new AtomicInteger(0);
    }

    @SneakyThrows
    @Override
    public void put(int position, byte[] data) {
        if (position + data.length > capacity) {
            throw new FileFullException();
        }
        fileChannel.write(ByteBuffer.wrap(data), position);
    }

    @SneakyThrows
    @Override
    public byte[] retrieve(int position, int size) {
        ByteBuffer buffer = ByteBuffer.allocateDirect(size);
        int read = fileChannel.read(buffer, position);
        if (read == -1) {
            return new byte[0];
        }
        buffer.flip();
        byte[] bytes = new byte[read];
        buffer.get(bytes, 0, bytes.length);
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
    public int capacity() {
        return this.capacity;
    }

    @Override
    public void close() throws IOException {
        if (raf != null) {
            raf.close();
        }
    }
}
