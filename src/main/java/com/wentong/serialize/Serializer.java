package com.wentong.serialize;

public interface Serializer<V> {

    byte[] serialize(V v);

}
