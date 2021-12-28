package com.wentong.hugecache;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CountDownLatch2Test {

    @Test
    void testCountDown() {
        CountDownLatch2 latch = new CountDownLatch2(10);
        for (int i = 0; i < 10; i++) {
            latch.countDown();
        }
        assertEquals(0, latch.getCount());
    }

    @Test
    void testReset() {
        CountDownLatch2 latch = new CountDownLatch2(10);
        for (int i = 0; i < 10; i++) {
            latch.countDown();
        }
        assertEquals(0, latch.getCount());
        latch.reset();
        assertEquals(10, latch.getCount());
    }

    @Test
    void testAwait() throws Exception{
        CountDownLatch2 latch = new CountDownLatch2(10);
        for (int i = 0; i < 10; i++) {
            latch.countDown();
        }
        latch.await();
        assertEquals(0, latch.getCount());
    }
}