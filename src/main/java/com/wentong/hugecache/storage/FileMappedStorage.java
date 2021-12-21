package com.wentong.hugecache.storage;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import sun.nio.ch.DirectBuffer; // NOSONAR

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 通过内存映射实现的存储
 */
@Slf4j
public class FileMappedStorage implements Storage {

    private final MappedByteBuffer mbb;
    private final RandomAccessFile raf;
    private static final long _1MB = 1024 * 1024L; // NOSONAR
    private final ScheduledExecutorService ss = Executors.newScheduledThreadPool(1, new ThreadFactoryBuilder().setNameFormat("flush-thread").build());

    public FileMappedStorage(String dir) throws IOException {
        File file = new File(dir);
        String absolutePath = file.getAbsolutePath();
        File dataFile = new File(absolutePath + "/0");
        if (!dataFile.exists()) {
            boolean created = dataFile.createNewFile();
            if (!created) {
                throw new IllegalArgumentException("This dir can't create new file.");
            }
        }
        this.raf = new RandomAccessFile(dataFile, "rw");
        mbb = raf.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, _1MB);
        // 启动定时任务，每隔 10s force 一下，减少消息丢失
        ss.scheduleAtFixedRate(() -> {
            if (mbb != null) {
                mbb.force();
            }
        }, 10, 10, TimeUnit.SECONDS);
    }

    @Override
    public void put(int position, byte[] data) {
        if (position + data.length > _1MB) {
            log.warn("File exceed the limit, need to create new file, ignore now.");
            throw new IllegalStateException();
        }
        this.mbb.position(position);
        this.mbb.put(data, 0, data.length);
    }

    @Override
    public byte[] retrieve(int position, int size) {
        if (position < 0 || position + size > _1MB) {
            return new byte[0];
        }
        int oldPosition = this.mbb.position();
        this.mbb.position(position);
        byte[] bytes = new byte[size];
        this.mbb.get(bytes);
        this.mbb.position(oldPosition);
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
