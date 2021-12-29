package com.wentong.serialize.impl.json;

import com.alibaba.fastjson.JSON;
import com.wentong.serialize.DeSerializer;

public class JsonDeserializer<V> implements DeSerializer<V> {

    @Override
    public V deserialize(byte[] data) {
        return (V) JSON.parse(data);
    }
}
