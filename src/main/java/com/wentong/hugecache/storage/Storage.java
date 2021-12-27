package com.wentong.hugecache.storage;

import java.io.Closeable;

public interface Storage extends Closeable {

    /**
     * 写入数据
     * @param position 写入起始位置
     * @param data 待写入的数据
     */
    void put(int position, byte[] data);

    /**
     * 从指定位置获取多少数据
     * @param position 起始位置
     * @param size 获取的数据大小
     * @return 起始位置到起始位置 + size 的所有数据
     */
    byte[] retrieve(int position, int size);

    /**
     * 释放存储空间
     */
    void free();

    /**
     * 返回当前文件写入进度
     * @return 文件写入进度
     */
    int position();

    int capacity();

}
