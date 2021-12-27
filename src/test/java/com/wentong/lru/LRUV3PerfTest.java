package com.wentong.lru;

import com.wentong.util.RandomUtil;
import org.junit.jupiter.api.Test;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 性能测试
 */
class LRUV3PerfTest {

    private static final int PRODUCER_COUNT = 8;
    private static final int CONSUMER_COUNT = 8;
    private static final int STR_LENGTH = 6;
    private static final int TOTAL_TEST_COUNT = 1000000;
    private static final int LOOP_COUNT = 5;

    private static final AtomicInteger produceCount = new AtomicInteger();
    private static final AtomicInteger consumeCount = new AtomicInteger();

    private static final BlockingQueue<Result> produceResultQueue = new LinkedBlockingQueue<>();
    private static final BlockingQueue<Result> consumeResultQueue = new LinkedBlockingQueue<>();

    private static final BlockingQueue<String> blockingQueue = new LinkedBlockingQueue<>();

    private static final LRUV3<String, String> lru = new LRUV3<>(TOTAL_TEST_COUNT * 2, "v2");

    @Test
    void testProduceThenConsume() throws Exception {
//        TimeUnit.SECONDS.sleep(20);
        long summaryProduceTime = 0;
        long summaryConsumeTime = 0;

        for (int j = 0; j < LOOP_COUNT; j++) {
            long producerTotalTime = 0;
            CountDownLatch producerLatch = new CountDownLatch(PRODUCER_COUNT);

            for (int i = 0; i < PRODUCER_COUNT; i++) {
                new Thread(new Producer(producerLatch, produceResultQueue), "producer:" + (i + 1)).start();
            }
            for (int i = 0; i < PRODUCER_COUNT; i++) {
                Result result = produceResultQueue.take();
                assertEquals(Status.SUCCESS, result.status);
                producerTotalTime += result.duration;
            }

//        System.out.println("-----------------------------------------------");
//        System.out.println("Producing test result:");
//        System.out.printf("Total test time = %d ns.\n", System.nanoTime() - start);
//        System.out.printf("Total item count = %d\n", TOTAL_TEST_COUNT);
//        System.out.printf("Producer thread number = %d\n", PRODUCER_COUNT);
//        System.out.printf("Item message length = %d bytes\n", STR_LENGTH);
            System.out.printf("Total producing time =  %d ns. %d ms\n", producerTotalTime, TimeUnit.MILLISECONDS.convert(producerTotalTime, TimeUnit.NANOSECONDS));
//        System.out.printf("Average producing time = %d ns.\n", producerTotalTime / PRODUCER_COUNT);
//        System.out.println("-----------------------------------------------");
            summaryProduceTime += producerTotalTime;
            long consumerTotalTime = 0;
            CountDownLatch consumerLatch = new CountDownLatch(CONSUMER_COUNT);

            for (int i = 0; i < CONSUMER_COUNT; i++) {
                new Thread(new Consumer(consumerLatch, consumeResultQueue), "consumer:" + i).start();
            }
            for (int i = 0; i < CONSUMER_COUNT; i++) {
                Result result = consumeResultQueue.take();
                assertEquals(Status.SUCCESS, result.status);
                consumerTotalTime += result.duration;
            }

            assertEquals(0, blockingQueue.size());
            summaryConsumeTime += consumerTotalTime;
//        System.out.println("Consuming test result:");
//        System.out.printf("Total test time = %d ns.\n", System.nanoTime() - start);
//        System.out.printf("Total item count = %d\n", TOTAL_TEST_COUNT);
//        System.out.printf("Consumer thread number = %d\n", CONSUMER_COUNT);
//        System.out.printf("Item message length = %d bytes\n", STR_LENGTH);
            System.out.printf("Total consuming time =  %d ns. %d ms\n", consumerTotalTime, TimeUnit.MILLISECONDS.convert(consumerTotalTime, TimeUnit.NANOSECONDS));
//        System.out.printf("Average consuming time = %d ns.\n", consumerTotalTime / CONSUMER_COUNT);
//        System.out.println("-----------------------------------------------");

            produceCount.set(0);
            consumeCount.set(0);
        }

        System.out.println("Total produce after " + LOOP_COUNT + " loop used: " + summaryProduceTime + " ns, average: " + summaryProduceTime / LOOP_COUNT);
        System.out.println("Total consume after " + LOOP_COUNT + " loop used: " + summaryConsumeTime + " ns, average: " + summaryConsumeTime / LOOP_COUNT);
    }

    @Test
    void testProduceMixConsume() throws Exception {
        long start = System.nanoTime();
        CountDownLatch latch = new CountDownLatch(PRODUCER_COUNT + CONSUMER_COUNT);

        long producerTotalTime = 0;
        for (int i = 0; i < PRODUCER_COUNT; i++) {
            new Thread(new Producer(latch, produceResultQueue), "producer:" + i).start();
        }

        long consumerTotalTime = 0;
        for (int i = 0; i < CONSUMER_COUNT; i++) {
            new Thread(new Consumer(latch, consumeResultQueue), "consumer:" + i).start();
        }


        for (int i = 0; i < PRODUCER_COUNT; i++) {
            Result result = produceResultQueue.take();
            assertEquals(Status.SUCCESS, result.status);
            producerTotalTime += result.duration;
        }

        System.out.println("-----------------------------------------------");
        System.out.println("Producing test result:");
        System.out.printf("Total test time = %d ns.\n", System.nanoTime() - start);
        System.out.printf("Total item count = %d\n", TOTAL_TEST_COUNT);
        System.out.printf("Producer thread number = %d\n", PRODUCER_COUNT);
        System.out.printf("Item message length = %d bytes\n", STR_LENGTH);
        System.out.printf("Total producing time =  %d ns.\n", producerTotalTime);
        System.out.printf("Average producing time = %d ns.\n", producerTotalTime / PRODUCER_COUNT);
        System.out.println("-----------------------------------------------");

        for (int i = 0; i < CONSUMER_COUNT; i++) {
            Result result = consumeResultQueue.take();
            assertEquals(Status.SUCCESS, result.status);
            consumerTotalTime += result.duration;
        }

        assertEquals(0, blockingQueue.size());

        System.out.println("Consuming test result:");
        System.out.printf("Total test time = %d ns.\n", System.nanoTime() - start);
        System.out.printf("Total item count = %d\n", TOTAL_TEST_COUNT);
        System.out.printf("Consumer thread number = %d\n", CONSUMER_COUNT);
        System.out.printf("Item message length = %d bytes\n", STR_LENGTH);
        System.out.printf("Total consuming time =  %d ns.\n", consumerTotalTime);
        System.out.printf("Average consuming time = %d ns.\n", consumerTotalTime / CONSUMER_COUNT);
        System.out.println("-----------------------------------------------");
    }

    static class Producer implements Runnable {

        private final CountDownLatch latch;
        private final BlockingQueue<Result> queue;

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

            while (produceCount.getAndIncrement() <= TOTAL_TEST_COUNT) {
                String str = RandomUtil.generateRandomString(STR_LENGTH);
                lru.put(str, str);
                blockingQueue.offer(str);
            }
            result.duration = System.nanoTime() - start;
            result.status = Status.SUCCESS;
            queue.offer(result);
        }
    }

    static class Consumer implements Runnable {

        private final CountDownLatch latch;
        private final BlockingQueue<Result> queue;

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

            while (consumeCount.getAndIncrement() <= TOTAL_TEST_COUNT) {
                try {
                    String key = blockingQueue.take();
                    String value = lru.get(key);
                    while (value == null) {
                        value = lru.get(key);
                    }
                    if (!key.equals(value)) {
                        throw new IllegalStateException();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
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
