package com.wentong.hugecache.storage;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import sun.nio.ch.DirectBuffer;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FileStorage implements Storage {

    private final MappedByteBuffer mbb;
    private final RandomAccessFile raf;
    private static final long _1GB = 1024 * 1024 * 1024L;
    private final ScheduledExecutorService ss = Executors.newScheduledThreadPool(1, new ThreadFactoryBuilder().setNameFormat("flush-thread").build());

    public FileStorage(String dir) throws IOException {
        File file = new File(dir);
        String absolutePath = file.getAbsolutePath();
        File dataFile = new File(absolutePath + "/0");
        if (!dataFile.exists()) {
            boolean created = dataFile.createNewFile();
            if (!created) {
                throw new IllegalArgumentException("This dir can't create file.");
            }
        }
        this.raf = new RandomAccessFile(dataFile, "rw");
        mbb = raf.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, _1GB);
        // 启动定时任务，每隔 10s force 一下，减少消息丢失
        ss.scheduleAtFixedRate(() -> {
            if (mbb != null) {
                mbb.force();
            }
        }, 10, 10, TimeUnit.SECONDS);
    }

    @Override
    public void put(int position, byte[] data) {
        mbb.put(data, position, data.length);
    }

    // todo
    @Override
    public byte[] retrieve(int position, int size) {
        ByteBuffer slice = mbb.slice();
        slice.position(position);
        ByteBuffer newByteBuffer = slice.slice();
        newByteBuffer.limit(size);
        byte[] bytes = new byte[size];
        newByteBuffer.get(bytes);
        return bytes;
    }

    @Override
    public void free() {
        ((DirectBuffer) mbb).cleaner().clean();
    }

    @Override
    public void close() throws IOException {
        if (raf != null) {
            raf.close();
        }
        ((DirectBuffer) mbb).cleaner().clean();
    }
}
