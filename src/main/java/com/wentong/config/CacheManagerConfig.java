package com.wentong.config;

import com.wentong.hugecache.StorageMode;

public class CacheManagerConfig {

    private CacheManagerConfig(){}

    public static final StorageMode MODE = StorageMode.FILE_CHANNEL;

    public static final String WORK_DIR = "/Users/renmai/IdeaProjects/tinycache/data/filechannel";

    public static final int DIR_INIT = 10;

    public static final int CAPACITY = 1024 * 1024 * 1024;


}
