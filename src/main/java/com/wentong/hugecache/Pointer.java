package com.wentong.hugecache;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Pointer {
    private int offset;
    private int length;
}
