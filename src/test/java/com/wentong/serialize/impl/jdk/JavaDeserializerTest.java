package com.wentong.serialize.impl.jdk;

import com.wentong.serialize.DeSerializer;
import com.wentong.serialize.Serializer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JavaDeserializerTest {

    @Test
    void deserialize() {
        DeSerializer<String> deserialize = new JavaDeserializer<>();
        Serializer<String> serializer = new JavaSerializer<>();
        String result = deserialize.deserialize(serializer.serialize("hello"));
        assertEquals("hello",result);
    }
}