/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.rocketmq.common;

import org.apache.rocketmq.common.constant.LoggerName;
import org.apache.rocketmq.logging.InternalLogger;
import org.apache.rocketmq.logging.InternalLoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 封装线程
 * 调用start即调用子类的run方法
 */
public abstract class ServiceThread implements Runnable {

    // RocketmqCommon
    private static final InternalLogger log = InternalLoggerFactory.getLogger(LoggerName.COMMON_LOGGER_NAME);

    private static final long JOIN_TIME = 90 * 1000;

    /**
     * 定义一个线程成员变量
     */
    private Thread thread;

    /**
     * 自定义一个门锁
     * 既能定时执行任务，也能显式调用提前执行
     *
     * CountDownLatch 的实现原理比较简单，它主要依赖于 AQS(AbstractQueuedSynchronizer)
     * CountDownLatch 内部维护了一个计数器，该计数器初始值为 N，代表需要等待的线程数目，
     * 当一个线程完成了需要等待的任务后，就会调用 countDown() 方法将计数器减 1，当计数器的值为 0 时，等待的线程就会开始执行。
     */
    protected final CountDownLatch2 waitPoint = new CountDownLatch2(1);

    /**
     * 是否被通知
     */
    protected volatile AtomicBoolean hasNotified = new AtomicBoolean(false);

    /**
     * 是否已经停止
     */
    protected volatile boolean stopped = false;

    /**
     * 是否守护线程
     */
    protected boolean isDaemon = false;

    //Make it able to restart the thread
    private final AtomicBoolean started = new AtomicBoolean(false);

    public ServiceThread() {

    }

    /**
     * 由子类实现
     * 作为创建线程时的参数名
     * @return
     */
    public abstract String getServiceName();

    /**
     * 启动线程
     */
    public void start() {
        log.info("Try to start service thread:{} started:{} lastThread:{}", getServiceName(), started.get(), thread);
        // 保证只能start一次
        if (!started.compareAndSet(false, true)) {
            return;
        }
        stopped = false;
        // 新建线程，参数是子类本身this，是一个runnable
        // 不需要外部创建线程，只需要把子类实例传入即可
        this.thread = new Thread(this, getServiceName());
        this.thread.setDaemon(isDaemon);
        // 调用start，则会调用子类的run方法
        this.thread.start();
    }

    /**
     * 终止线程
     */
    public void shutdown() {
        this.shutdown(false);
    }

    /**
     * 终止线程
     * @param interrupt
     */
    public void shutdown(final boolean interrupt) {
        log.info("Try to shutdown service thread:{} started:{} lastThread:{}", getServiceName(), started.get(), thread);
        // 修改 started 为 false
        if (!started.compareAndSet(true, false)) {
            return;
        }
        // 停止
        this.stopped = true;
        log.info("shutdown thread " + this.getServiceName() + " interrupt " + interrupt);

        // 没有被通知，则通知
        // 运行中的线程不需要再等待
        if (hasNotified.compareAndSet(false, true)) {
            waitPoint.countDown(); // notify
        }

        try {
            if (interrupt) {
                // 中断, stop()和suspend()被废弃了（容易死锁）
                // 通常用于请求线程提前结束，或者在某些条件下取消线程的运行
                // 当一个线程被中断时，它会设置该线程的中断标志，这个标志可以通过 isInterrupted() 方法查询
                // 如果目标线程正在阻塞状态（如在 sleep(), wait(), 或 join() 等方法中），那么它会抛出 InterruptedException 并清除中断状态。
                // 如果线程没有处于阻塞状态，那么中断状态会被保留直到线程检测到中断或进入阻塞状态
                this.thread.interrupt();
            }

            long beginTime = System.currentTimeMillis();

            // 是非守护进程
            if (!this.thread.isDaemon()) {
                // join() 让“主线程”等待“子线程”结束之后才能继续运行， 参考 https://www.cnblogs.com/qlky/p/7373370.html
                // 等待子线程执行（有执行超时时间 90s）
                // 超时后执行下面的 long elapsedTime... 业务代码
                this.thread.join(this.getJointime());
            }
            long elapsedTime = System.currentTimeMillis() - beginTime;
            log.info("join thread " + this.getServiceName() + " elapsed time(ms) " + elapsedTime + " "
                + this.getJointime());
        } catch (InterruptedException e) {
            log.error("Interrupted", e);
        }
    }

    public long getJointime() {
        return JOIN_TIME;
    }

    @Deprecated
    public void stop() {
        this.stop(false);
    }

    @Deprecated
    public void stop(final boolean interrupt) {
        if (!started.get()) {
            return;
        }
        this.stopped = true;
        log.info("stop thread " + this.getServiceName() + " interrupt " + interrupt);

        if (hasNotified.compareAndSet(false, true)) {
            waitPoint.countDown(); // notify
        }

        if (interrupt) {
            this.thread.interrupt();
        }
    }

    /**
     * 停止线程
     */
    public void makeStop() {
        if (!started.get()) {
            return;
        }
        this.stopped = true;
        log.info("makestop thread " + this.getServiceName());
    }

    /**
     * 立即唤醒
     */
    public void wakeup() {
        if (hasNotified.compareAndSet(false, true)) {
            waitPoint.countDown(); // notify
        }
    }

    /**
     * 阻塞指定时间间隔后运行（protected表明是让子类调用）
     *
     * @param interval
     */
    protected void waitForRunning(long interval) {
        // 只要有一个线程将hasNotified=true，其他线程再设置就直接返回了
        if (hasNotified.compareAndSet(true, false)) {
            this.onWaitEnd();
            return;
        }

        //entry to wait
        waitPoint.reset();

        try {
            // 等待
            waitPoint.await(interval, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            log.error("Interrupted", e);
        } finally {
            // 等待通知
            hasNotified.set(false);
            this.onWaitEnd();
        }
    }

    protected void onWaitEnd() {
    }

    public boolean isStopped() {
        return stopped;
    }

    public boolean isDaemon() {
        return isDaemon;
    }

    public void setDaemon(boolean daemon) {
        isDaemon = daemon;
    }
}
