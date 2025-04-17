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
package org.apache.rocketmq.store;

import org.apache.rocketmq.common.protocol.ResponseCode;
import org.apache.rocketmq.remoting.protocol.RemotingSysResponseCode;

/**
 * 查找消息的状态
 * 状态转换
 * {@link org.apache.rocketmq.broker.processor.PullMessageProcessor#processRequest(io.netty.channel.Channel, org.apache.rocketmq.remoting.protocol.RemotingCommand, boolean)}
 */
public enum GetMessageStatus {

    /**
     * {@link RemotingSysResponseCode#SUCCESS}
     */
    FOUND,

    /**
     * {@link ResponseCode#PULL_RETRY_IMMEDIATELY}
     */
    NO_MATCHED_MESSAGE,

    /**
     * {@link ResponseCode#PULL_RETRY_IMMEDIATELY}
     */
    MESSAGE_WAS_REMOVING,

    /**
     * {@link ResponseCode#PULL_NOT_FOUND}
     */
    OFFSET_FOUND_NULL,

    /**
     * {@link ResponseCode#PULL_OFFSET_MOVED}
     */
    OFFSET_OVERFLOW_BADLY,

    /**
     * {@link ResponseCode#PULL_NOT_FOUND}
     */
    OFFSET_OVERFLOW_ONE,

    /**
     * {@link ResponseCode#PULL_OFFSET_MOVED}
     */
    OFFSET_TOO_SMALL,

    /**
     * {@link ResponseCode#PULL_NOT_FOUND}
     * {@link ResponseCode#PULL_OFFSET_MOVED}
     */
    NO_MATCHED_LOGIC_QUEUE,

    /**
     * {@link ResponseCode#PULL_NOT_FOUND}
     * {@link ResponseCode#PULL_OFFSET_MOVED}
     */
    NO_MESSAGE_IN_QUEUE,
}
