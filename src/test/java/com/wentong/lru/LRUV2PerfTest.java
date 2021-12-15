package com.wentong.lru;

import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 性能测试
 */
public class LRUV2PerfTest {

    private static final int PRODUCER_COUNT = 4;
    private static final int CONSUMER_COUNT = 4;
    private static final int STR_LENGTH = 6;
    private static final int TOTAL_TEST_COUNT = 1000000;

    private static AtomicInteger produceCount = new AtomicInteger();
    private static AtomicInteger consumeCount = new AtomicInteger();

    private static BlockingQueue<Result> produceResultQueue = new LinkedBlockingQueue<>();
    private static BlockingQueue<Result> consumeResultQueue = new LinkedBlockingQueue<>();

    private static BlockingQueue<String> blockingQueue = new LinkedBlockingQueue<>();

    private static LRUV2<String, String> lru = new LRUV2<>(TOTAL_TEST_COUNT / 100);


    static class Producer implements Runnable {

        private CountDownLatch latch;
        private BlockingQueue<Result> queue;

        public Producer(CountDownLatch latch, BlockingQueue<Result> queue) {
            this.latch = latch;
            this.queue = queue;
        }

        @Override
        public void run() {
            Result result = new Result();
            latch.countDown();
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            long start = System.nanoTime();

            while (produceCount.get() <= TOTAL_TEST_COUNT) {
                String str = UUID.randomUUID().toString().substring(0, STR_LENGTH);
                lru.put(str, str);
                produceCount.incrementAndGet();
                blockingQueue.offer(str);
            }
            result.duration = System.nanoTime() - start;
            result.status = Status.SUCCESS;
            queue.offer(result);
        }
    }

    static class Consumer implements Runnable {

        private CountDownLatch latch;
        private BlockingQueue<Result> queue;

        public Consumer(CountDownLatch latch, BlockingQueue<Result> queue) {
            this.latch = latch;
            this.queue = queue;
        }

        @Override
        public void run() {
            latch.countDown();
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Result result = new Result();
            long start = System.nanoTime();

            while (consumeCount.get() < TOTAL_TEST_COUNT) {
                while (blockingQueue.isEmpty()) {
                    try {
                        TimeUnit.MILLISECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                String key = blockingQueue.poll();
                String value = lru.get(key);
                while (value == null) {
                    value = lru.get(key);
                }
                if (!key.equals(value)) {
                    throw new IllegalStateException();
                }
                consumeCount.incrementAndGet();
            }
            result.status = Status.SUCCESS;
            result.duration = System.nanoTime() - start;
            queue.offer(result);
        }
    }


    static class Result {
        Status status;
        long duration;
    }

    enum Status {
        SUCCESS, FAIL
    }


}
