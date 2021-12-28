package com.wentong.hugecache;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * 统计缓存指标
 */
@AllArgsConstructor
@Getter
@ToString
public class Statistics {
    private int getCounter;
    private int missCounter;
    private int hitCounter;
}
