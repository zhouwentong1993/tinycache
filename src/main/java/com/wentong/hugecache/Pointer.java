package com.wentong.hugecache;

import com.wentong.hugecache.storage.Storage;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Pointer {
    private int offset;
    private int length;
    private Storage storage;
}
