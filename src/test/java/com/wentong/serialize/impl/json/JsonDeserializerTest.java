package com.wentong.serialize.impl.json;

import com.wentong.serialize.DeSerializer;
import com.wentong.serialize.Serializer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JsonDeserializerTest {

    @Test
    void deserialize() {
        Serializer<String> serializer = new JsonSerializer<>();
        DeSerializer<String> deSerializer = new JsonDeserializer<>();
        byte[] serialize = serializer.serialize("hello world");
        String deserialize = deSerializer.deserialize(serialize);
        assertEquals("hello world", deserialize);

    }
}