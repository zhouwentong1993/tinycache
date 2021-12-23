package com.wentong.hugecache.block;

import com.wentong.hugecache.Pointer;

/**
 * 存储块抽象
 */
public interface Block {

    /**
     * 新增数据
     * @param data 数据
     * @return 数据位置描述
     */
    Pointer put(byte[] data);

    /**
     * 按照数据指针获取数据
     * @param pointer 数据指针
     * @return 指针所在数据
     */
    byte[] retrieve(Pointer pointer);

    /**
     * 移除指针所在处数据，并增加 dirty 数
     * @param pointer 数据指针
     * @return 被移除的数据
     */
    byte[] remove(Pointer pointer);

    /**
     * 更新所在指针处的数据，变成 data。
     * @param pointer 数据指针
     * @param data 新数据
     * @return 新数据所在指针。如果能放下，则跟原来一样；否则新的地方。
     */
    Pointer update(Pointer pointer, byte[] data);

    /**
     * 脏页数据
     */
    int dirtyPage();

    /**
     * 未被使用页数据
     */
    int freePage();

    int getCapacity();

}
