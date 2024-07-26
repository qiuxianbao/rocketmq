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
package org.apache.rocketmq.example.quickstart;

import java.util.List;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;

/**
 * This example shows how to subscribe and consume messages using providing {@link DefaultMQPushConsumer}.
 */
// TODO-QIU: 2024年4月16日, 0016
public class Consumer {

    public static void main(String[] args) throws InterruptedException, MQClientException {

        /*
         * Instantiate with specified consumer group name.
         */
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("please_rename_unique_group_name_4");

        /*
         * Specify name server addresses.
         * <p/>
         *
         * Alternatively, you may specify name server addresses via exporting environmental variable: NAMESRV_ADDR
         * <pre>
         * {@code
         * consumer.setNamesrvAddr("name-server1-ip:9876;name-server2-ip:9876");
         * }
         * </pre>
         */
        consumer.setNamesrvAddr("127.0.0.1:9876");

        // 消费模式，默认是集群消费
//        consumer.setMessageModel();

        /*
         * Specify where to start in case the specified consumer group is a brand new one.
         */
//        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);

        // CONSUME_FROM_MAX_OFFSET
        // 按照期望，应该是不会消费任何消息，只有等生产者再发送消息后，才会对消息进行消费，事实是这样吗？
        // 事实是，启动后就开始消费消息

        // 消费者组开始消费消息的位置取决于其创建时与待订阅消息的过期状态关系：
        // 1.新创建且待订阅最早消息未过期时，从最初始消息开始消费；
        // 2.待订阅最早消息已过期时，则从最新消息开始消费，忽略早于启动时刻的过期消息。
        //      一个新的消费组订阅一个已经存在比较久的 topic，设置 CONSUME_FROM_MAX_OFFSET 是符合预期的，
        //      即该主题的 consumequeue/{queueNum}/fileName，fileName 通常不会是 00000000000000000000，
        // 这样的设计兼顾了新业务的完整数据需求和已有业务对时效性数据的关注

        // 对于一个新的消费组，无论是集群模式还是广播模式都不会存储该消费组的消费进度，可以理解为-1,
        // 此时就需要根据 DefaultMQPushConsumer#consumeFromWhere 属性来决定其从何处开始消费

        // 我们知道，消息消费者从 Broker 服务器拉取消息时，需要进行消费队列的负载，即 RebalancePushImpl

        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);

        /*
         * Subscribe one more more topics to consume.
         */
        consumer.subscribe("TopicTest", "*");

        /*
         *  Register callback to execute on arrival of messages fetched from brokers.
         */
        consumer.registerMessageListener(new MessageListenerConcurrently() {

            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs,
                ConsumeConcurrentlyContext context) {

                System.out.println("MessageExt msg.getMsgId():" + msgs.get(0).getMsgId());

                System.out.println("-------------------分割线-----------------");

                // 2个MsgId不一样
                // 在客户端返回的 msg 信息，其最终返回的对象是 MessageClientExt

                // MsgId 对应的是mq-console中消息查询是的 Message ID
                // mq-console做了兼容，2种都可以查询到

//             MessageExt msg.getMsgId():FA291F9A52C018B4AAC264EBEB9B0000
//             System.out.println("-------------------分割线-----------------");

                // offsetId
//             ConsumeMessageThread_1 Receive New Messages: [
//                  MessageExt [
//                      brokerName=broker-a,
//                      queueId=0,
//                      storeSize=178,
//                      queueOffset=1,
//                      sysFlag=0,
//                      bornTimestamp=1713593982875,
//                      bornHost=/127.0.0.1:54584,
//                      storeTimestamp=1713593982879,
//                      storeHost=/127.0.0.1:10911,
//                      msgId=7F00000100002A9F0000000000000216,
//                      commitLogOffset=534,
//                      bodyCRC=613185359,
//                      reconsumeTimes=0,
//                      preparedTransactionOffset=0,
//                      toString()=Message{
//                                      topic='TopicTest',
//                                      flag=0,
//                                      properties={
//                                              MIN_OFFSET=0,
//                                              MAX_OFFSET=2,
//                                              CONSUME_START_TIME=1713593982896,
//                                              UNIQ_KEY=FA291F9A52C018B4AAC264EBEB9B0000,
//                                              WAIT=true,
//                                              TAGS=TagA
//                                      },
//                                      body=[72, 101, 108, 108, 111, 32, 82, 111, 99, 107, 101, 116, 77, 81, 32, 48],
//                                      transactionId='null'
//                        }
//                 ]
//             ]

                System.out.printf("%s Receive New Messages: %s %n", Thread.currentThread().getName(), msgs);
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });

        /*
         *  Launch the consumer instance.
         */
        consumer.start();

        System.out.printf("Consumer Started.%n");
    }
}
