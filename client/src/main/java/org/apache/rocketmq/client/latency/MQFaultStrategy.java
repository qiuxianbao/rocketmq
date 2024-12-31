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

package org.apache.rocketmq.client.latency;

import org.apache.rocketmq.client.impl.factory.MQClientInstance;
import org.apache.rocketmq.client.impl.producer.TopicPublishInfo;
import org.apache.rocketmq.client.log.ClientLogger;
import org.apache.rocketmq.common.protocol.route.TopicRouteData;
import org.apache.rocketmq.logging.InternalLogger;
import org.apache.rocketmq.common.message.MessageQueue;

/**
 * Broker故障
 * 延迟策略
 *
 * 技巧：门面模式
 */
public class MQFaultStrategy {
    private final static InternalLogger log = ClientLogger.getLog();

    private final LatencyFaultTolerance<String> latencyFaultTolerance = new LatencyFaultToleranceImpl();

    /**
     * 故障延迟开关
     */
    private boolean sendLatencyFaultEnable = false;

    /**
     *
     */
    private long[] latencyMax = {50L, 100L, 550L, 1000L, 2000L, 3000L, 15000L};
    private long[] notAvailableDuration = {0L, 0L, 30000L, 60000L, 120000L, 180000L, 600000L};

    public long[] getNotAvailableDuration() {
        return notAvailableDuration;
    }

    public void setNotAvailableDuration(final long[] notAvailableDuration) {
        this.notAvailableDuration = notAvailableDuration;
    }

    public long[] getLatencyMax() {
        return latencyMax;
    }

    public void setLatencyMax(final long[] latencyMax) {
        this.latencyMax = latencyMax;
    }

    public boolean isSendLatencyFaultEnable() {
        return sendLatencyFaultEnable;
    }

    public void setSendLatencyFaultEnable(final boolean sendLatencyFaultEnable) {
        this.sendLatencyFaultEnable = sendLatencyFaultEnable;
    }

    /**
     * 选择消息队列
     *
     * @param tpInfo
     * @see MQClientInstance#topicRouteData2TopicPublishInfo(String, TopicRouteData)
     *
     * 示例：
     * 如果topicA在broker-a 和broker-b 上分别创建了4个队列，那么返回的核心信息为：
     * [
     * 	    {
     * 		"brokerName": "broker-a",
     * 		"queueId": "0"
     *    },
     *    {
     * 		"brokerName": "broker-a",
     * 		"queueId": "1"
     *    },
     *    {
     * 		"brokerName": "broker-a",
     * 		"queueId": "2"
     *    },
     *    {
     * 		"brokerName": "broker-a",
     * 		"queueId": "3"
     *    },
     *    {
     * 		"brokerName": "broker-b",
     * 		"queueId": "0"
     *    },
     *    {
     * 		"brokerName": "broker-b",
     * 		"queueId": "1"
     *    },
     *    {
     * 		"brokerName": "broker-b",
     * 		"queueId": "2"
     *    },
     *    {
     * 		"brokerName": "broker-b",
     * 		"queueId": "3"
     *    }
     * ]
     *
     * @param lastBrokerName 上一次选择的执行发送消息失败的broker
     * @return
     */
    public MessageQueue selectOneMessageQueue(final TopicPublishInfo tpInfo, final String lastBrokerName) {
        /**
         * 启用Broker故障规避机制
         * Broker规避就是在一次消息发送过程中发现错误，在某一段时间内，消息生产者不会选择该broker上的消息队列，提高发送消息的成功率
         */
        if (this.sendLatencyFaultEnable) {
            try {
                // 轮询，获取一个消息队列
                int index = tpInfo.getSendWhichQueue().getAndIncrement();

                // 优先从同一个 brokerName 中取（不同结点）
                for (int i = 0; i < tpInfo.getMessageQueueList().size(); i++) {
                    int pos = Math.abs(index++) % tpInfo.getMessageQueueList().size();
                    if (pos < 0)
                        pos = 0;
                    MessageQueue mq = tpInfo.getMessageQueueList().get(pos);
                    /**
                     * 验证该消息队列是否可用
                     *
                     * 可用的2种情况：
                     * （1）不在条目中
                     * （2）在条目中，但是已经过了延迟时间（已经恢复了）
                     */
                    if (latencyFaultTolerance.isAvailable(mq.getBrokerName())) {
                        // null == lastBrokerName 首次的场景
                        // lastBrokerName == mq.getBrokerName() 已经恢复了
                        if (null == lastBrokerName || mq.getBrokerName().equals(lastBrokerName))
                            return mq;
                    }
                }


                // 换一个 brokerName
                final String notBestBroker = latencyFaultTolerance.pickOneAtLeast();
                int writeQueueNums = tpInfo.getQueueIdByBroker(notBestBroker);
                if (writeQueueNums > 0) {
                    final MessageQueue mq = tpInfo.selectOneMessageQueue();
                    if (notBestBroker != null) {
                        mq.setBrokerName(notBestBroker);
                        mq.setQueueId(tpInfo.getSendWhichQueue().getAndIncrement() % writeQueueNums);
                    }
                    return mq;
                } else {
                    // 移除掉
                    latencyFaultTolerance.remove(notBestBroker);
                }
            } catch (Exception e) {
                log.error("Error occurred when selecting message queue", e);
            }

            return tpInfo.selectOneMessageQueue();
        }

        // 默认机制
        return tpInfo.selectOneMessageQueue(lastBrokerName);
    }

    /**
     * 更新失败条目
     *
     * @param brokerName    broker名称
     * @param currentLatency 本次消息发送的延迟时间
     * @param isolation      是否隔离
     */
    public void updateFaultItem(final String brokerName, final long currentLatency, boolean isolation) {
        if (this.sendLatencyFaultEnable) {
            // isolation = true, 默认30s, 规避时间是 10min
            long duration = computeNotAvailableDuration(isolation ? 30000 : currentLatency);
            this.latencyFaultTolerance.updateFaultItem(brokerName, currentLatency, duration);
        }
    }

    /**
     * 计算Broker故障的规避时长
     *
     * 根据 currentLatency 本次消息发送延迟，从 latencyMax 尾部向前找到第一个比 currentLatency 小的索引，如果没有找到，返回0
     * 然后根据这个索引，从 notAvailableDuration 中找到对应的时间，在这个时间内，Broker将不可用
     *
     * @param currentLatency
     * @return
     */
    private long computeNotAvailableDuration(final long currentLatency) {
        for (int i = latencyMax.length - 1; i >= 0; i--) {
            if (currentLatency >= latencyMax[i])
                return this.notAvailableDuration[i];
        }

        return 0;
    }
}
