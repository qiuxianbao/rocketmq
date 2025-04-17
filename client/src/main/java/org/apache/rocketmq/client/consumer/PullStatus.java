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
package org.apache.rocketmq.client.consumer;


import org.apache.rocketmq.client.impl.MQClientAPIImpl;
import org.apache.rocketmq.common.protocol.ResponseCode;
import org.apache.rocketmq.remoting.protocol.RemotingCommand;
import org.apache.rocketmq.remoting.protocol.RemotingSysResponseCode;

/**
 * 消息拉取状态
 *
 * 1.服务端获取消息的返回状态 {@link org.apache.rocketmq.store.GetMessageStatus}，会先转换成响应码 {@link RemotingSysResponseCode}
 * 状态转换
 * @see org.apache.rocketmq.broker.processor.PullMessageProcessor#processRequest(io.netty.channel.Channel, org.apache.rocketmq.remoting.protocol.RemotingCommand, boolean)
 *
 * 2.然后客户端再将响应码再转换成拉取状态
 * 状态转换
 * @see MQClientAPIImpl#processPullResponse(RemotingCommand)
 */
public enum PullStatus {
    /**
     * Founded
     * 找到了消息
     * {@link RemotingSysResponseCode#SUCCESS}
     */
    FOUND,

    /**
     * No new message can be pull
     * 没有消息
     * {@link ResponseCode#PULL_NOT_FOUND}
     */
    NO_NEW_MSG,

    /**
     * Filtering results can not match
     * 没有匹配的消息
     * {@link ResponseCode#PULL_RETRY_IMMEDIATELY}
     */
    NO_MATCHED_MSG,

    /**
     * Illegal offset,may be too big or too small
     * 非法偏移量
     * {@link ResponseCode#PULL_OFFSET_MOVED}
     */
    OFFSET_ILLEGAL
}
