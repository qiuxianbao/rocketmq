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

package org.apache.rocketmq.common.protocol;

import io.netty.channel.ChannelHandlerContext;
import org.apache.rocketmq.remoting.protocol.RemotingCommand;

/**
 * Netty的指令码
 */
public class RequestCode {

    /**
     * 消息
     */
    // 发送消息
    public static final int SEND_MESSAGE = 10;

    // 拉取消息
    public static final int PULL_MESSAGE = 11;

    // 查询消息
    public static final int QUERY_MESSAGE = 12;


    /**
     * 偏移量
     */
    // 查询Broker偏移量
    public static final int QUERY_BROKER_OFFSET = 13;

    // 查询消费者偏移
    public static final int QUERY_CONSUMER_OFFSET = 14;

    // 更新消费者偏移量
    public static final int UPDATE_CONSUMER_OFFSET = 15;


    /**
     * 主题
     */
    // 更新并创建主
    public static final int UPDATE_AND_CREATE_TOPIC = 17;


    // 获取所有主题配置
    public static final int GET_ALL_TOPIC_CONFIG = 21;

    // 获取主题配置列表
    public static final int GET_TOPIC_CONFIG_LIST = 22;

    // 获取主题名称列
    public static final int GET_TOPIC_NAME_LIST = 23;

    /**
     * Broker
     */
    // 更新Broker配置
    public static final int UPDATE_BROKER_CONFIG = 25;

    // 获取Broker配置
    public static final int GET_BROKER_CONFIG = 26;

    /**
     * 文件管理
     */
    // 触发删除文件
    public static final int TRIGGER_DELETE_FILES = 27;

    /**
     * Broker
     */
    // 获取Broker运行时信息
    public static final int GET_BROKER_RUNTIME_INFO = 28;

    /**
     * 时间戳相关操作
     */
    // 根据时间戳搜索偏移量
    public static final int SEARCH_OFFSET_BY_TIMESTAMP = 29;

    // 获取最大偏移量
    public static final int GET_MAX_OFFSET = 30;

    // 获取最小偏移量
    public static final int GET_MIN_OFFSET = 31;


    /**
     * 消息存储时间相关操作
     */
    // 获取最早消息存储时间
    public static final int GET_EARLIEST_MSG_STORETIME = 32;

    // 通过ID查看消息
    public static final int VIEW_MESSAGE_BY_ID = 33;

    // 心跳
    public static final int HEART_BEAT = 34;

    // 注销客户端
    public static final int UNREGISTER_CLIENT = 35;

    // TODO-QIU: 2024年7月25日, 0025
    public static final int CONSUMER_SEND_MSG_BACK = 36;

    /**
     * 事务
     */
    // 结束事务
    public static final int END_TRANSACTION = 37;

    // 根据组获取消费者
    public static final int GET_CONSUMER_LIST_BY_GROUP = 38;

    /**
     * 事务
     */
    // 检查事务状态
    public static final int CHECK_TRANSACTION_STATE = 39;

    // 知消费者ID变更
    public static final int NOTIFY_CONSUMER_IDS_CHANGED = 40;


    /**
     * 队列
     */
    // 锁定
    public static final int LOCK_BATCH_MQ = 41;

    // 解锁
    public static final int UNLOCK_BATCH_MQ = 42;


    // 获取所有消费者偏移量
    public static final int GET_ALL_CONSUMER_OFFSET = 43;

    // 获取所有延迟队列偏移量
    public static final int GET_ALL_DELAY_OFFSET = 45;

    // 检查客户端配
    public static final int CHECK_CLIENT_CONFIG = 46;


    /**
     * ACL
     */
    // 更新并创建ACL配置
    public static final int UPDATE_AND_CREATE_ACL_CONFIG = 50;

    // 删除ACL配置
    public static final int DELETE_ACL_CONFIG = 51;

    // 获取Broker集群ACL信息
    public static final int GET_BROKER_CLUSTER_ACL_INFO = 52;

    // 更新全局白名单地址配
    public static final int UPDATE_GLOBAL_WHITE_ADDRS_CONFIG = 53;

    // 获取Broker集群ACL配置
    public static final int GET_BROKER_CLUSTER_ACL_CONFIG = 54;

    /**
     * KV
     */
    // 设置KV配置
    public static final int PUT_KV_CONFIG = 100;

    // 获取KV配置
    public static final int GET_KV_CONFIG = 101;

    // 删除KV配置
    public static final int DELETE_KV_CONFIG = 102;

    /**
     * borker
     * 注册Broker
     * @see org.apache.rocketmq.namesrv.processor.DefaultRequestProcessor#processRequest(ChannelHandlerContext, RemotingCommand)
     */
    public static final int REGISTER_BROKER = 103;

    /**
     * 注销Broker
     */
    public static final int UNREGISTER_BROKER = 104;

    /**
     * 路由发现
     * 根据主题获取路由信息
     */
    public static final int GET_ROUTEINTO_BY_TOPIC = 105;

    // 获取Broker集群信息
    public static final int GET_BROKER_CLUSTER_INFO = 106;

    /**
     * 订阅组
     */
    // 更新并创建订阅组
    public static final int UPDATE_AND_CREATE_SUBSCRIPTIONGROUP = 200;

    // 获取所有订阅组配置
    public static final int GET_ALL_SUBSCRIPTIONGROUP_CONFIG = 201;

    // 获取主题统计信息
    public static final int GET_TOPIC_STATS_INFO = 202;

    // 获取消费者连接列表
    public static final int GET_CONSUMER_CONNECTION_LIST = 203;

    // 获取生产者连接列表
    public static final int GET_PRODUCER_CONNECTION_LIST = 204;

    /**
     * broker
     */
    // 清除Broker写权限
    public static final int WIPE_WRITE_PERM_OF_BROKER = 205;

    // 从namesrv获取所有主题列
    public static final int GET_ALL_TOPIC_LIST_FROM_NAMESERVER = 206;

    // 删除订阅组
    public static final int DELETE_SUBSCRIPTIONGROUP = 207;

    // 获取消费统计
    public static final int GET_CONSUME_STATS = 208;

    // 暂停消费者
    public static final int SUSPEND_CONSUMER = 209;

    // 恢复消费者
    public static final int RESUME_CONSUMER = 210;

    // 重置消费者消费偏移量
    public static final int RESET_CONSUMER_OFFSET_IN_CONSUMER = 211;

    // 重置broker消费偏移量
    public static final int RESET_CONSUMER_OFFSET_IN_BROKER = 212;

    // 调整消费者线程池
    public static final int ADJUST_CONSUMER_THREAD_POOL = 213;

    public static final int WHO_CONSUME_THE_MESSAGE = 214;

    // 物理删除Broker上的主题数据
    public static final int DELETE_TOPIC_IN_BROKER = 215;

    // 在NameServer层面删除主题的元数据
    public static final int DELETE_TOPIC_IN_NAMESRV = 216;

    // 通过命名空间获取KV列表
    public static final int GET_KVLIST_BY_NAMESPACE = 219;

    // 重置客户端消费偏移量
    public static final int RESET_CONSUMER_CLIENT_OFFSET = 220;

    // 从客户端获取消费者状态
    public static final int GET_CONSUMER_STATUS_FROM_CLIENT = 221;

    //
    public static final int INVOKE_BROKER_TO_RESET_OFFSET = 222;

    public static final int INVOKE_BROKER_TO_GET_CONSUMER_STATUS = 223;

    public static final int QUERY_TOPIC_CONSUME_BY_WHO = 300;

    // 通过集群获取Topic信息
    public static final int GET_TOPICS_BY_CLUSTER = 224;

    public static final int REGISTER_FILTER_SERVER = 301;
    public static final int REGISTER_MESSAGE_FILTER_CLASS = 302;

    public static final int QUERY_CONSUME_TIME_SPAN = 303;

    // 通过namespace获取系统topic
    public static final int GET_SYSTEM_TOPIC_LIST_FROM_NS = 304;
    public static final int GET_SYSTEM_TOPIC_LIST_FROM_BROKER = 305;

    public static final int CLEAN_EXPIRED_CONSUMEQUEUE = 306;

    public static final int GET_CONSUMER_RUNNING_INFO = 307;

    public static final int QUERY_CORRECTION_OFFSET = 308;
    public static final int CONSUME_MESSAGE_DIRECTLY = 309;

    public static final int SEND_MESSAGE_V2 = 310;

    public static final int GET_UNIT_TOPIC_LIST = 311;

    public static final int GET_HAS_UNIT_SUB_TOPIC_LIST = 312;

    public static final int GET_HAS_UNIT_SUB_UNUNIT_TOPIC_LIST = 313;

    public static final int CLONE_GROUP_OFFSET = 314;

    public static final int VIEW_BROKER_STATS_DATA = 315;

    public static final int CLEAN_UNUSED_TOPIC = 316;

    public static final int GET_BROKER_CONSUME_STATS = 317;

    /**
     * update the config of name server
     * 更新namesrv配置信息
     */
    public static final int UPDATE_NAMESRV_CONFIG = 318;

    /**
     * get config from name server
     * 获取namesrv配置信息
     */
    public static final int GET_NAMESRV_CONFIG = 319;

    public static final int SEND_BATCH_MESSAGE = 320;

    public static final int QUERY_CONSUME_QUEUE = 321;

    // 查询数据版本
    public static final int QUERY_DATA_VERSION = 322;

    /**
     * resume logic of checking half messages that have been put in TRANS_CHECK_MAXTIME_TOPIC before
     */
    public static final int RESUME_CHECK_HALF_MESSAGE = 323;

    public static final int SEND_REPLY_MESSAGE = 324;

    public static final int SEND_REPLY_MESSAGE_V2 = 325;

    public static final int PUSH_REPLY_MESSAGE_TO_CLIENT = 326;
}
