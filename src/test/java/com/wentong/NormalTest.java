package com.wentong;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NormalTest {

    static final int CAPACITY = 10;

    @Test
    void testIntBufferPut() {
        IntBuffer buffer = IntBuffer.allocate(CAPACITY);
        // 写状态
        for (int i = 0; i < CAPACITY; i++) {
            buffer.put(i);
        }
        System.out.println(buffer);
        // 读状态
        buffer.flip();
        System.out.println(buffer);
        for (int i = 0; i < CAPACITY; i++) {
            buffer.get();
        }
        System.out.println(buffer);
        // 写状态
        buffer.flip();
        System.out.println(buffer);
        for (int i = 0; i < CAPACITY; i++) {
            assertEquals(i, buffer.get());
        }
    }

    @Test
    void testBytebufferPut() {
        String str = "2333";
        final ByteBuffer buf = ByteBuffer.allocateDirect(1024);
        int length = str.getBytes().length;
        buf.put(str.getBytes(), 0, length);
        buf.flip();
        byte[] dst = new byte[length];
        buf.get(dst, 0, dst.length);
        assertEquals("2333", new String(dst));
    }

}
