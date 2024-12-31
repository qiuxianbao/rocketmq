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
package org.apache.rocketmq.client.impl;

/**
 * 消息发送的方式
 */
public enum CommunicationMode {

    /**
     * 同步发送
     * 发送者向MQ执行发送消息API时，同步等待，知道消息服务器返回发送结果
     */
    SYNC,

    /**
     * 异步发送
     * 发送者向MQ执行发送消息API时，指定消息发送成功后的回调函数，然后调用消息发送API后，立即返回
     * 消息发送者线程不阻塞，知道运行结束，消息发送成功或失败的回调任务在一个新的线程中执行
     */
    ASYNC,

    /**
     * 单项发送
     * 发送者向MQ执行发送消息API时，直接返回，不等待消息服务器的结果，也不注册回调函数
     * 简单地说，就是只管发，不在乎消息是否成功存储在消息服务器上
     */
    ONEWAY,
}
