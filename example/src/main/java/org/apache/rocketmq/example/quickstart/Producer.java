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

import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.common.RemotingHelper;

/**
 * This class demonstrates how to send messages to brokers using provided {@link DefaultMQProducer}.
 */
// TODO-QIU: 2024年3月29日, 0029
public class Producer {
    public static void main(String[] args) throws MQClientException, InterruptedException {

        /*
         * Instantiate with a producer group name.
         */
        DefaultMQProducer producer = new DefaultMQProducer("please_rename_unique_group_name");

        /*
         * Specify name server addresses.
         * <p/>
         *
         * Alternatively, you may specify name server addresses via exporting environmental variable: NAMESRV_ADDR
         * <pre>
         * {@code
         * producer.setNamesrvAddr("name-server1-ip:9876;name-server2-ip:9876");
         * }
         * </pre>
         */
        producer.setNamesrvAddr("127.0.0.1:9876");

        /*
         * Launch the instance.
         */
        producer.start();

        for (int i = 0; i < 1; i++) {
            try {

                /*
                 * Create a message instance, specifying topic, tag and message body.
                 */
                Message msg = new Message("TopicTest" /* Topic */,
                    "TagA" /* Tag */,
                    ("Hello RocketMQ " + i).getBytes(RemotingHelper.DEFAULT_CHARSET) /* Message body */
                );

                /*
                 * Call send message to deliver message to one of brokers.
                 */
                // 消息发送、消息消费、RocketMQ queryMsgById 命令以及 rocketmq-console 等使用场景中究竟是用的哪一个 ID？

                // msgId含义
                // 该 ID 是消息发送者在消息发送时会首先在客户端生成，全局唯一，
                // 在RocketMQ 中该 ID 还有另外的一个叫法：uniqId，无不体现其全局唯一性

                // offsetMsgId含义
                // 消息偏移 ID，该 ID 记录了消息所在集群的物理地址，
                // 主要包含所存储Broker 服务器的地址(IP 与端口号)以及所在 commitlog 文件的物理偏移量。

//             SendResult [
//                  sendStatus=SEND_OK,
//                  msgId=FA291F9A52C018B4AAC264EBEB9B0000,
//                  offsetMsgId=7F00000100002A9F0000000000000216,
//                  messageQueue=MessageQueue [topic=TopicTest, brokerName=broker-a, queueId=0],
//                  queueOffset=1
//              ]
                SendResult sendResult = producer.send(msg);

                System.out.printf("%s%n", sendResult);
            } catch (Exception e) {
                e.printStackTrace();
                Thread.sleep(1000);
            }
        }

        /*
         * Shut down once the producer instance is not longer in use.
         */
        producer.shutdown();
    }
}
