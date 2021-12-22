package com.wentong.hugecache;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Slf4j
public abstract class ServiceThread implements Runnable {
    private volatile boolean stop = false;
    private final CountDownLatch latch = new CountDownLatch(1);
    private final int awaitTime;
    private final TimeUnit timeUnit;
    private Thread thread;

    public ServiceThread(int awaitTime, TimeUnit timeUnit) {
        this.awaitTime = awaitTime;
        this.timeUnit = timeUnit;
    }

    @SneakyThrows
    @Override
    public void run() {
        log.info("thread:{} started!", threadName());
        while (!stop) {
            try {
                latch.await(awaitTime, timeUnit);
                process();
            } finally {
                reset();
            }
        }
        cleanUp();
        log.info("thread:{} stopped!", threadName());
    }

    public void start() {
        this.thread = new Thread(this, threadName());
        thread.setDaemon(true);
        thread.start();
    }

    public abstract void cleanUp();

    public abstract void process();

    public abstract String threadName();

    public void runNow() {
        this.latch.countDown();
    }

    private void reset() {
        int count = (int) latch.getCount();
        if (count == 1) {
            latch.countDown();
        }
    }

    public void stop() {
        this.stop = true;
    }

}
