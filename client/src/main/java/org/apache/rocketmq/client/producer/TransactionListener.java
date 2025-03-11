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
package org.apache.rocketmq.client.producer;

import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;

/**
 * 事务监听器
 */
public interface TransactionListener {

    /**
     * half msg成功，执行本地事务
     * 与业务方代码在一个事务中，只要本地事务提交成功，该方法也会提交成功
     * 主要是向 t_message_transaction 表中添加一条记录 ，在事务回查时，如果该记录存在，就认为该消息需要提交，建议返回 LocalTransactionState.Unknown
     *
     * When send transactional prepare(half) message succeed, this method will be invoked to execute local transaction.
     *
     * @param msg Half(prepare) message
     * @param arg Custom business parameter
     * @return Transaction state
     */
    LocalTransactionState executeLocalTransaction(final Message msg, final Object arg);

    /**
     * 本地事务回查
     * 该方法主要告知RocketMQ消息是需要提交还是回滚
     * 如果本地事务表 t_message_transaction 存在记录，则认为提交，
     * 如果不存在，可以设置回查次数，如果超过次数，则回滚
     * 否则，返回未知
     *
     * When no response to prepare(half) message. broker will send check message to check the transaction status, and this
     * method will be invoked to get local transaction status.
     *
     * @param msg Check message
     * @return Transaction state
     */
    LocalTransactionState checkLocalTransaction(final MessageExt msg);
}