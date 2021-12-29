package com.wentong.serialize;

public interface DeSerializer<V> {

    V deserialize(byte[] data);

}
