package com.wentong.hugecache.storage;

import lombok.SneakyThrows;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * 通过 FileChannel 实现的存储
 */
public class FileChannelStorage implements Storage {

    private final FileChannel fileChannel;
    private final RandomAccessFile raf;

    @SneakyThrows
    public FileChannelStorage(String dir) {
        File file = new File(dir);
        if (!file.exists() || file.isFile()) {
            boolean ok = file.mkdir();
            if (!ok) {
                throw new IllegalArgumentException("This dir can't be created!");
            }
        }
        raf = new RandomAccessFile(dir + "/0", "rw");
        fileChannel = raf.getChannel();
    }

    @SneakyThrows
    @Override
    public void put(int position, byte[] data) {
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
    public void close() throws IOException {
        if (raf != null) {
            raf.close();
        }
    }
}
