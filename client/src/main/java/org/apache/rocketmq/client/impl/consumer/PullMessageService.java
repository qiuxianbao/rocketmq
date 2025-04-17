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
package org.apache.rocketmq.client.impl.consumer;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import org.apache.rocketmq.client.impl.factory.MQClientInstance;
import org.apache.rocketmq.client.log.ClientLogger;
import org.apache.rocketmq.common.ServiceThread;
import org.apache.rocketmq.logging.InternalLogger;
import org.apache.rocketmq.common.utils.ThreadUtils;

/**
 * 消息拉取
 *
 * 消息消费有两种模式：广播模式与集群模式，
 * 1.广播模式比较简单，每一个消费者需要去拉取订阅主题下所有消费队列的消息，
 * 2.集群模式
 * 在集群模式下，同一个消费组内有多个消息消费者，同一个主题存在多个消费队列，那么消费者如何进行消息队列负载呢？
 * 从上文启动流程也知道，每一个消费组内维护一个线程池来消费消息，那么这些线程又是如何分工合作的呢？
 *
 * 消息队列负载，通常的做法是一个消息队列在同一时间只允许被一个消息消费者消费，一个消息消费者可以同时消费多个消息队列，那么RocketMQ是如何实现的呢？
 * 从MQClientInstance的启动流程中可以看出，RocketMQ使用一个单独的线程 {@link MQClientInstance#start()}
 * PullMessageService来负责消息的拉取。
 */
public class PullMessageService extends ServiceThread {
    private final InternalLogger log = ClientLogger.getLog();

    private final LinkedBlockingQueue<PullRequest> pullRequestQueue = new LinkedBlockingQueue<PullRequest>();
    private final MQClientInstance mQClientFactory;
    private final ScheduledExecutorService scheduledExecutorService = Executors
        .newSingleThreadScheduledExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "PullMessageServiceScheduledThread");
            }
        });

    public PullMessageService(MQClientInstance mQClientFactory) {
        this.mQClientFactory = mQClientFactory;
    }

    /**
     * put时机
     * 延迟添加
     *
     * @see {@link DefaultMQPushConsumerImpl#pullMessage(PullRequest)}
     *
     * @param pullRequest
     * @param timeDelay
     */
    public void executePullRequestLater(final PullRequest pullRequest, final long timeDelay) {
        if (!isStopped()) {
            this.scheduledExecutorService.schedule(new Runnable() {
                @Override
                public void run() {
                    PullMessageService.this.executePullRequestImmediately(pullRequest);
                }
            }, timeDelay, TimeUnit.MILLISECONDS);
        } else {
            log.warn("PullMessageServiceScheduledThread has shutdown");
        }
    }

    /**
     * put时机
     * 立即添加
     *
     * put时机
     * {@link DefaultMQPushConsumerImpl#pullMessage(PullRequest)}
     * {@link RebalancePushImpl#dispatchPullRequest(List)}
     * @param pullRequest
     */
    public void executePullRequestImmediately(final PullRequest pullRequest) {
        try {
            this.pullRequestQueue.put(pullRequest);
        } catch (InterruptedException e) {
            log.error("executePullRequestImmediately pullRequestQueue.put", e);
        }
    }

    public void executeTaskLater(final Runnable r, final long timeDelay) {
        if (!isStopped()) {
            this.scheduledExecutorService.schedule(r, timeDelay, TimeUnit.MILLISECONDS);
        } else {
            log.warn("PullMessageServiceScheduledThread has shutdown");
        }
    }

    public ScheduledExecutorService getScheduledExecutorService() {
        return scheduledExecutorService;
    }

    /**
     * 消息拉取
     * @param pullRequest
     */
    private void pullMessage(final PullRequest pullRequest) {
        final MQConsumerInner consumer = this.mQClientFactory.selectConsumer(pullRequest.getConsumerGroup());
        if (consumer != null) {
            DefaultMQPushConsumerImpl impl = (DefaultMQPushConsumerImpl) consumer;
            impl.pullMessage(pullRequest);
        } else {
            log.warn("No matched consumer for the PullRequest {}, drop it", pullRequest);
        }
    }


    /**
     * 消息拉取调用入口
     * {@link MQClientInstance#start()}
     */
    @Override
    public void run() {
        log.info(this.getServiceName() + " service started");

        // 技巧：while (!this.isStopped()) 这是一种通用的设计技巧
        // 每执行一次业务逻辑检测一下其运行状态，可以通过其他线程将stopped设置为true，从而停止该线程
        while (!this.isStopped()) {
            try {
                /**
                 * 从队列中获取一个任务，阻塞，直到有任务加入队列
                 * pullRequestQueue#put时机 {@link PullMessageService#executePullRequestImmediately(PullRequest)}
                 */
                PullRequest pullRequest = this.pullRequestQueue.take();
                // 消息拉取
                this.pullMessage(pullRequest);
            } catch (InterruptedException ignored) {
            } catch (Exception e) {
                log.error("Pull Message Service Run Method exception", e);
            }
        }

        log.info(this.getServiceName() + " service end");
    }

    @Override
    public void shutdown(boolean interrupt) {
        super.shutdown(interrupt);
        ThreadUtils.shutdownGracefully(this.scheduledExecutorService, 1000, TimeUnit.MILLISECONDS);
    }

    @Override
    public String getServiceName() {
        return PullMessageService.class.getSimpleName();
    }

}
