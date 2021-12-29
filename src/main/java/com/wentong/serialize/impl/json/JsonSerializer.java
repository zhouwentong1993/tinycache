package com.wentong.serialize.impl.json;

import com.alibaba.fastjson.JSON;
import com.wentong.serialize.Serializer;

public class JsonSerializer<V> implements Serializer<V> {

    @Override
    public byte[] serialize(V v) {
        return JSON.toJSONBytes(v);
    }
}
