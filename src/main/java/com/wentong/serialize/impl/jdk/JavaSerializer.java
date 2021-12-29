package com.wentong.serialize.impl.jdk;

import com.wentong.serialize.Serializer;
import lombok.SneakyThrows;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;

/**
 * Java 默认序列化机制
 * @param <V> 待序列化的类型
 */
public class JavaSerializer<V> implements Serializer<V> {

    @SneakyThrows
    @Override
    public byte[] serialize(V v) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (ObjectOutputStream output = new ObjectOutputStream(buffer)) {
            output.writeObject(v);
        }
        return buffer.toByteArray();
    }
}
