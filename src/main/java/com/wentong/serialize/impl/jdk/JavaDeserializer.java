package com.wentong.serialize.impl.jdk;

import com.wentong.serialize.DeSerializer;
import lombok.SneakyThrows;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

public class JavaDeserializer<V> implements DeSerializer<V> {

    @SneakyThrows
    @Override
    public V deserialize(byte[] data) {
        try (ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(data))) {
            return (V) input.readObject();
        }
    }
}
