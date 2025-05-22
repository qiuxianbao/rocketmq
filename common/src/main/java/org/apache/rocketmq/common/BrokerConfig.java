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

import java.net.InetAddress;
import java.net.UnknownHostException;
import org.apache.rocketmq.common.annotation.ImportantField;
import org.apache.rocketmq.common.constant.LoggerName;
import org.apache.rocketmq.common.constant.PermName;
import org.apache.rocketmq.logging.InternalLogger;
import org.apache.rocketmq.logging.InternalLoggerFactory;
import org.apache.rocketmq.remoting.common.RemotingUtil;


/**
 * broker属性配置
 */
public class BrokerConfig {
    private static final InternalLogger log = InternalLoggerFactory.getLogger(LoggerName.COMMON_LOGGER_NAME);

    /**
     * RocketMQ主目录，默认为用户主目录
     * 环境变量 Environment variables
     */
    private String rocketmqHome = System.getProperty(MixAll.ROCKETMQ_HOME_PROPERTY, System.getenv(MixAll.ROCKETMQ_HOME_ENV));

    /**
     * namesrv地址
     */
    @ImportantField
    private String namesrvAddr = System.getProperty(MixAll.NAMESRV_ADDR_PROPERTY, System.getenv(MixAll.NAMESRV_ADDR_ENV));

    /**
     * broker服务地址
     */
    @ImportantField
    private String brokerIP1 = RemotingUtil.getLocalAddress();

    /**
     * broker HA IP地址，供slave同步消息的地址
     */
    private String brokerIP2 = RemotingUtil.getLocalAddress();

    /**
     * broker名称
     * 默认为服务器的hostname
     */
    @ImportantField
    private String brokerName = localHostName();

    /**
     * broker集群名称
     */
    @ImportantField
    private String brokerClusterName = "DefaultCluster";

    /**
     * brokerId，
     * 0表示主节点，大于0表示从节点
     */
    @ImportantField
    private long brokerId = MixAll.MASTER_ID;

    /**
     * broker权限
     * 默认为6，表示可读可写
     */
    private int brokerPermission = PermName.PERM_READ | PermName.PERM_WRITE;

    /**
     * 主题在一个broker上创建的队列数量
     * 默认为8
     */
    private int defaultTopicQueueNums = 8;

    /**
     * 是否自动创建主题
     * 默认为true
     */
    @ImportantField
    private boolean autoCreateTopicEnable = true;

    /**
     * 集群名称是否可用在主题使用
     */
    private boolean clusterTopicEnable = true;

    /**
     * broker名称是否用作主题使用
     */
    private boolean brokerTopicEnable = true;

    /**
     * 是否自动创建消费组/订阅组
     * 默认为为true
     * 表示第一次使用消费组配置信息时，如果不存在，则使用上述默认配置自动创建一个
     */
    @ImportantField
    private boolean autoCreateSubscriptionGroup = true;

    /**
     * 消息存储插件地址
     */
    private String messageStorePlugIn = "";

    @ImportantField
    private String msgTraceTopicName = MixAll.RMQ_SYS_TRACE_TOPIC;

    /**
     * 消息追踪
     * 默认为false
     */
    @ImportantField
    private boolean traceTopicEnable = false;

    /**
     * 服务端处理消息发送线程池线程数量
     * thread numbers for send message thread pool, since spin lock will be used by default since 4.0.x, the default
     * value is 1.
     */
    private int sendMessageThreadPoolNums = 1; //16 + Runtime.getRuntime().availableProcessors() * 4;

    /**
     * 服务端处理查询消息拉取线程线程池数量
     */
    private int pullMessageThreadPoolNums = 16 + Runtime.getRuntime().availableProcessors() * 2;
    private int processReplyMessageThreadPoolNums = 16 + Runtime.getRuntime().availableProcessors() * 2;

    /**
     * 服务端处理查询消息线程池线程数量
     */
    private int queryMessageThreadPoolNums = 8 + Runtime.getRuntime().availableProcessors();

    /**
     * 服务端处理控制台管理命令
     * 线程池线程数量
     */
    private int adminBrokerThreadPoolNums = 16;

    /**
     * 服务端处理客户端管理（心跳、注册、取消注册）线程池线程数量
     */
    private int clientManageThreadPoolNums = 32;

    /**
     * 服务端处理消费管理（获取消费者列表、更新消费进度、查询消费进度）线程池线程数量
     */
    private int consumerManageThreadPoolNums = 32;
    private int heartbeatThreadPoolNums = Math.min(32, Runtime.getRuntime().availableProcessors());

    /**
     * Thread numbers for EndTransactionProcessor
     */
    private int endTransactionThreadPoolNums = 8 + Runtime.getRuntime().availableProcessors() * 2;

    /**
     * 持久化消息消费进度（consumerOffset.json）文件的频率
     */
    private int flushConsumerOffsetInterval = 1000 * 5;

    private int flushConsumerOffsetHistoryInterval = 1000 * 60;

    /**
     * 是否拒绝事务消息
     * 默认为false
     */
    @ImportantField
    private boolean rejectTransactionMessage = false;

    /**
     * 是否支持从服务器获取namesrv地址
     */
    @ImportantField
    private boolean fetchNamesrvAddrByAddressServer = false;

    /**
     * 消息发送线程池任务队列初始化大小
     * 默认为1W
     */
    private int sendThreadPoolQueueCapacity = 10000;

    /**
     * 消息拉取线程池任务队列初始化大小
     */
    private int pullThreadPoolQueueCapacity = 100000;
    private int replyThreadPoolQueueCapacity = 10000;

    /**
     * 查询消息线程池队列初始化大小
     */
    private int queryThreadPoolQueueCapacity = 20000;

    /**
     * 客户端管理线程池队列初始化大小
     */
    private int clientManagerThreadPoolQueueCapacity = 1000000;

    /**
     * 消费管理线程池队列初始化大小
     */
    private int consumerManagerThreadPoolQueueCapacity = 1000000;
    private int heartbeatThreadPoolQueueCapacity = 50000;
    private int endTransactionPoolQueueCapacity = 100000;

    /**
     * broker服务器过滤服务器数量
     */
    private int filterServerNums = 0;

    /**
     * 是否开启长轮询
     * 默认为true
     *
     * 说明：
     * 什么是长轮询？
     * 长轮询（Long Polling）是一种用于实现实时通信的技术，它通过让客户端保持与服务器的连接，直到有新数据可用或超时为止长轮询（Long Polling）是一种用于实现实时通信的技术，
     * 它通过让客户端保持与服务器的连接，直到有新数据可用或超时为止
     *
     * 工作原理：
     * 客户端向服务器发送请求，询问是否有新数据。
     * 如果服务器没有新数据，它不会立即返回响应，而是保持连接打开，等待新数据的到来。
     * 一旦有新数据，服务器立即返回响应，客户端处理完数据后再次发起请求。
     *
     * 优点：
     * 相较于短轮询（Short Polling），长轮询减少了不必要的请求次数，从而降低了服务器负载和网络流量。
     * 提供了接近实时的数据更新体验。
     *
     * 缺点：
     * 长时间保持连接可能会占用较多的服务器资源。
     * 在高并发场景下，可能需要更多的线程或连接数来支持这些长时间挂起的请求。
     *
     * 在RocketMQ中，长轮询机制被用来优化消息拉取过程。
     * 如果消费者拉取消息时没有找到新消息，并且启用了长轮询，服务器会挂起该请求一段时间，期间如果有新消息到达，则立即返回给消费者；
     * 否则，直到超时才返回无消息的结果。这种方式可以有效减少【空轮询带来的性能开销】
     */
    private boolean longPollingEnable = true;

    /**
     * 短轮询等待时间
     * 默认为1s
     */
    private long shortPollingTimeMills = 1000;

    /**
     * 消费者数量变化后是否立即通知 rebalance 线程 {@link org.apache.rocketmq.client.impl.consumer.RebalanceImpl}
     * 以便进行消息队列重新负载
     */
    private boolean notifyConsumerIdsChangedEnable = true;

    private boolean highSpeedMode = false;

    private boolean commercialEnable = true;
    private int commercialTimerCount = 1;
    private int commercialTransCount = 1;
    private int commercialBigCount = 1;
    private int commercialBaseCount = 1;

    /**
     * 消息传输是否使用堆内存
     * 默认为true
     */
    private boolean transferMsgByHeap = true;
    private int maxDelayTime = 40;

    /**
     * 消息区域
     */
    private String regionId = MixAll.DEFAULT_TRACE_REGION_ID;

    /**
     * 注册Broker的超时时间，默认是6s
     */
    private int registerBrokerTimeoutMills = 6000;

    /**
     * 从节点是否可读
     * 默认为false
     */
    private boolean slaveReadEnable = false;

    /**
     * 如果消费组消息消费堆积
     * 是否禁用该消费组继续消费消息
     */
    private boolean disableConsumeIfConsumerReadSlowly = false;

    /**
     * 消息消费堆阈值
     * 默认为16G
     *
     * 在disableConsumeIfConsumerReadSlowly为true时生效
     */
    private long consumerFallbehindThreshold = 1024L * 1024 * 1024 * 16;

    /**
     * 是否支持broker快速失败
     * 默认为true
     *
     * 如果是true表示会立即清除消息发送线程池
     * 消息拉取线程池中排队任务，直接返回系统错误
     */
    private boolean brokerFastFailureEnable = true;

    /**
     * 清除发送线程池任务等待时间
     * 如果系统时间 - 任务放入队列中的时间 < waitTimeMillsInSendQueue，本次请求任务暂不移除
     * 默认为200ms
     */
    private long waitTimeMillsInSendQueue = 200;

    /**
     * 清除消息拉取线程池任务队列的等待时间
     * 如果系统时间 - 任务放入队列中的时间 < waitTimeMillsInPullQueue，本次请求任务暂不移除
     * 默认为5s
     */
    private long waitTimeMillsInPullQueue = 5 * 1000;
    private long waitTimeMillsInHeartbeatQueue = 31 * 1000;
    private long waitTimeMillsInTransactionQueue = 3 * 1000;

    private long startAcceptSendRequestTimeStamp = 0L;

    private boolean traceOn = true;

    // Switch of filter bit map calculation.
    // If switch on:
    // 1. Calculate filter bit map when construct queue.
    // 2. Filter bit map will be saved to consume queue extend file if allowed.
    private boolean enableCalcFilterBitMap = false;

    // Expect num of consumers will use filter.
    private int expectConsumerNumUseFilter = 32;

    // Error rate of bloom filter, 1~100.
    private int maxErrorRateOfBloomFilter = 20;

    /**
     * 清除过滤数据的时间间隔
     * 默认为24h
     */
    //how long to clean filter data after dead.Default: 24h
    private long filterDataCleanTimeSpan = 24 * 3600 * 1000;

    /**
     * 消息过滤是否支持重试
     */
    // whether do filter when retry.
    private boolean filterSupportRetry = false;

    /**
     * 是否支持根据属性过滤，默认为false
     * 如果使用基于标准SQL92模式过滤消息，需要设置该值为true
     */
    private boolean enablePropertyFilter = false;

    private boolean compressedRegister = false;

    private boolean forceRegister = true;

    /**
     * This configurable item defines interval of topics registration of broker to name server. Allowing values are
     * between 10, 000 and 60, 000 milliseconds.
     */
    private int registerNameServerPeriod = 1000 * 30;

    /**
     * The minimum time of the transactional message  to be checked firstly, one message only exceed this time interval
     * that can be checked.
     */
    @ImportantField
    private long transactionTimeOut = 6 * 1000;

    /**
     * The maximum number of times the message was checked, if exceed this value, this message will be discarded.
     */
    @ImportantField
    private int transactionCheckMax = 15;

    /**
     * Transaction message check interval.
     */
    @ImportantField
    private long transactionCheckInterval = 60 * 1000;

    /**
     * Acl feature switch
     */
    @ImportantField
    private boolean aclEnable = false;

    private boolean storeReplyMessageEnable = true;

    public static String localHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            log.error("Failed to obtain the host name", e);
        }

        return "DEFAULT_BROKER";
    }

    public boolean isTraceOn() {
        return traceOn;
    }

    public void setTraceOn(final boolean traceOn) {
        this.traceOn = traceOn;
    }

    public long getStartAcceptSendRequestTimeStamp() {
        return startAcceptSendRequestTimeStamp;
    }

    public void setStartAcceptSendRequestTimeStamp(final long startAcceptSendRequestTimeStamp) {
        this.startAcceptSendRequestTimeStamp = startAcceptSendRequestTimeStamp;
    }

    public long getWaitTimeMillsInSendQueue() {
        return waitTimeMillsInSendQueue;
    }

    public void setWaitTimeMillsInSendQueue(final long waitTimeMillsInSendQueue) {
        this.waitTimeMillsInSendQueue = waitTimeMillsInSendQueue;
    }

    public long getConsumerFallbehindThreshold() {
        return consumerFallbehindThreshold;
    }

    public void setConsumerFallbehindThreshold(final long consumerFallbehindThreshold) {
        this.consumerFallbehindThreshold = consumerFallbehindThreshold;
    }

    public boolean isBrokerFastFailureEnable() {
        return brokerFastFailureEnable;
    }

    public void setBrokerFastFailureEnable(final boolean brokerFastFailureEnable) {
        this.brokerFastFailureEnable = brokerFastFailureEnable;
    }

    public long getWaitTimeMillsInPullQueue() {
        return waitTimeMillsInPullQueue;
    }

    public void setWaitTimeMillsInPullQueue(final long waitTimeMillsInPullQueue) {
        this.waitTimeMillsInPullQueue = waitTimeMillsInPullQueue;
    }

    public boolean isDisableConsumeIfConsumerReadSlowly() {
        return disableConsumeIfConsumerReadSlowly;
    }

    public void setDisableConsumeIfConsumerReadSlowly(final boolean disableConsumeIfConsumerReadSlowly) {
        this.disableConsumeIfConsumerReadSlowly = disableConsumeIfConsumerReadSlowly;
    }

    public boolean isSlaveReadEnable() {
        return slaveReadEnable;
    }

    public void setSlaveReadEnable(final boolean slaveReadEnable) {
        this.slaveReadEnable = slaveReadEnable;
    }

    public int getRegisterBrokerTimeoutMills() {
        return registerBrokerTimeoutMills;
    }

    public void setRegisterBrokerTimeoutMills(final int registerBrokerTimeoutMills) {
        this.registerBrokerTimeoutMills = registerBrokerTimeoutMills;
    }

    public String getRegionId() {
        return regionId;
    }

    public void setRegionId(final String regionId) {
        this.regionId = regionId;
    }

    public boolean isTransferMsgByHeap() {
        return transferMsgByHeap;
    }

    public void setTransferMsgByHeap(final boolean transferMsgByHeap) {
        this.transferMsgByHeap = transferMsgByHeap;
    }

    public String getMessageStorePlugIn() {
        return messageStorePlugIn;
    }

    public void setMessageStorePlugIn(String messageStorePlugIn) {
        this.messageStorePlugIn = messageStorePlugIn;
    }

    public boolean isHighSpeedMode() {
        return highSpeedMode;
    }

    public void setHighSpeedMode(final boolean highSpeedMode) {
        this.highSpeedMode = highSpeedMode;
    }

    public String getRocketmqHome() {
        return rocketmqHome;
    }

    public void setRocketmqHome(String rocketmqHome) {
        this.rocketmqHome = rocketmqHome;
    }

    public String getBrokerName() {
        return brokerName;
    }

    public void setBrokerName(String brokerName) {
        this.brokerName = brokerName;
    }

    public int getBrokerPermission() {
        return brokerPermission;
    }

    public void setBrokerPermission(int brokerPermission) {
        this.brokerPermission = brokerPermission;
    }

    public int getDefaultTopicQueueNums() {
        return defaultTopicQueueNums;
    }

    public void setDefaultTopicQueueNums(int defaultTopicQueueNums) {
        this.defaultTopicQueueNums = defaultTopicQueueNums;
    }

    public boolean isAutoCreateTopicEnable() {
        return autoCreateTopicEnable;
    }

    public void setAutoCreateTopicEnable(boolean autoCreateTopic) {
        this.autoCreateTopicEnable = autoCreateTopic;
    }

    public String getBrokerClusterName() {
        return brokerClusterName;
    }

    public void setBrokerClusterName(String brokerClusterName) {
        this.brokerClusterName = brokerClusterName;
    }

    public String getBrokerIP1() {
        return brokerIP1;
    }

    public void setBrokerIP1(String brokerIP1) {
        this.brokerIP1 = brokerIP1;
    }

    public String getBrokerIP2() {
        return brokerIP2;
    }

    public void setBrokerIP2(String brokerIP2) {
        this.brokerIP2 = brokerIP2;
    }

    public int getSendMessageThreadPoolNums() {
        return sendMessageThreadPoolNums;
    }

    public void setSendMessageThreadPoolNums(int sendMessageThreadPoolNums) {
        this.sendMessageThreadPoolNums = sendMessageThreadPoolNums;
    }

    public int getPullMessageThreadPoolNums() {
        return pullMessageThreadPoolNums;
    }

    public void setPullMessageThreadPoolNums(int pullMessageThreadPoolNums) {
        this.pullMessageThreadPoolNums = pullMessageThreadPoolNums;
    }

    public int getProcessReplyMessageThreadPoolNums() {
        return processReplyMessageThreadPoolNums;
    }

    public void setProcessReplyMessageThreadPoolNums(int processReplyMessageThreadPoolNums) {
        this.processReplyMessageThreadPoolNums = processReplyMessageThreadPoolNums;
    }

    public int getQueryMessageThreadPoolNums() {
        return queryMessageThreadPoolNums;
    }

    public void setQueryMessageThreadPoolNums(final int queryMessageThreadPoolNums) {
        this.queryMessageThreadPoolNums = queryMessageThreadPoolNums;
    }

    public int getAdminBrokerThreadPoolNums() {
        return adminBrokerThreadPoolNums;
    }

    public void setAdminBrokerThreadPoolNums(int adminBrokerThreadPoolNums) {
        this.adminBrokerThreadPoolNums = adminBrokerThreadPoolNums;
    }

    public int getFlushConsumerOffsetInterval() {
        return flushConsumerOffsetInterval;
    }

    public void setFlushConsumerOffsetInterval(int flushConsumerOffsetInterval) {
        this.flushConsumerOffsetInterval = flushConsumerOffsetInterval;
    }

    public int getFlushConsumerOffsetHistoryInterval() {
        return flushConsumerOffsetHistoryInterval;
    }

    public void setFlushConsumerOffsetHistoryInterval(int flushConsumerOffsetHistoryInterval) {
        this.flushConsumerOffsetHistoryInterval = flushConsumerOffsetHistoryInterval;
    }

    public boolean isClusterTopicEnable() {
        return clusterTopicEnable;
    }

    public void setClusterTopicEnable(boolean clusterTopicEnable) {
        this.clusterTopicEnable = clusterTopicEnable;
    }

    public String getNamesrvAddr() {
        return namesrvAddr;
    }

    public void setNamesrvAddr(String namesrvAddr) {
        this.namesrvAddr = namesrvAddr;
    }

    public long getBrokerId() {
        return brokerId;
    }

    public void setBrokerId(long brokerId) {
        this.brokerId = brokerId;
    }

    public boolean isAutoCreateSubscriptionGroup() {
        return autoCreateSubscriptionGroup;
    }

    public void setAutoCreateSubscriptionGroup(boolean autoCreateSubscriptionGroup) {
        this.autoCreateSubscriptionGroup = autoCreateSubscriptionGroup;
    }

    public boolean isRejectTransactionMessage() {
        return rejectTransactionMessage;
    }

    public void setRejectTransactionMessage(boolean rejectTransactionMessage) {
        this.rejectTransactionMessage = rejectTransactionMessage;
    }

    public boolean isFetchNamesrvAddrByAddressServer() {
        return fetchNamesrvAddrByAddressServer;
    }

    public void setFetchNamesrvAddrByAddressServer(boolean fetchNamesrvAddrByAddressServer) {
        this.fetchNamesrvAddrByAddressServer = fetchNamesrvAddrByAddressServer;
    }

    public int getSendThreadPoolQueueCapacity() {
        return sendThreadPoolQueueCapacity;
    }

    public void setSendThreadPoolQueueCapacity(int sendThreadPoolQueueCapacity) {
        this.sendThreadPoolQueueCapacity = sendThreadPoolQueueCapacity;
    }

    public int getPullThreadPoolQueueCapacity() {
        return pullThreadPoolQueueCapacity;
    }

    public void setPullThreadPoolQueueCapacity(int pullThreadPoolQueueCapacity) {
        this.pullThreadPoolQueueCapacity = pullThreadPoolQueueCapacity;
    }

    public int getReplyThreadPoolQueueCapacity() {
        return replyThreadPoolQueueCapacity;
    }

    public void setReplyThreadPoolQueueCapacity(int replyThreadPoolQueueCapacity) {
        this.replyThreadPoolQueueCapacity = replyThreadPoolQueueCapacity;
    }

    public int getQueryThreadPoolQueueCapacity() {
        return queryThreadPoolQueueCapacity;
    }

    public void setQueryThreadPoolQueueCapacity(final int queryThreadPoolQueueCapacity) {
        this.queryThreadPoolQueueCapacity = queryThreadPoolQueueCapacity;
    }

    public boolean isBrokerTopicEnable() {
        return brokerTopicEnable;
    }

    public void setBrokerTopicEnable(boolean brokerTopicEnable) {
        this.brokerTopicEnable = brokerTopicEnable;
    }

    public int getFilterServerNums() {
        return filterServerNums;
    }

    public void setFilterServerNums(int filterServerNums) {
        this.filterServerNums = filterServerNums;
    }

    public boolean isLongPollingEnable() {
        return longPollingEnable;
    }

    public void setLongPollingEnable(boolean longPollingEnable) {
        this.longPollingEnable = longPollingEnable;
    }

    public boolean isNotifyConsumerIdsChangedEnable() {
        return notifyConsumerIdsChangedEnable;
    }

    public void setNotifyConsumerIdsChangedEnable(boolean notifyConsumerIdsChangedEnable) {
        this.notifyConsumerIdsChangedEnable = notifyConsumerIdsChangedEnable;
    }

    public long getShortPollingTimeMills() {
        return shortPollingTimeMills;
    }

    public void setShortPollingTimeMills(long shortPollingTimeMills) {
        this.shortPollingTimeMills = shortPollingTimeMills;
    }

    public int getClientManageThreadPoolNums() {
        return clientManageThreadPoolNums;
    }

    public void setClientManageThreadPoolNums(int clientManageThreadPoolNums) {
        this.clientManageThreadPoolNums = clientManageThreadPoolNums;
    }

    public boolean isCommercialEnable() {
        return commercialEnable;
    }

    public void setCommercialEnable(final boolean commercialEnable) {
        this.commercialEnable = commercialEnable;
    }

    public int getCommercialTimerCount() {
        return commercialTimerCount;
    }

    public void setCommercialTimerCount(final int commercialTimerCount) {
        this.commercialTimerCount = commercialTimerCount;
    }

    public int getCommercialTransCount() {
        return commercialTransCount;
    }

    public void setCommercialTransCount(final int commercialTransCount) {
        this.commercialTransCount = commercialTransCount;
    }

    public int getCommercialBigCount() {
        return commercialBigCount;
    }

    public void setCommercialBigCount(final int commercialBigCount) {
        this.commercialBigCount = commercialBigCount;
    }

    public int getMaxDelayTime() {
        return maxDelayTime;
    }

    public void setMaxDelayTime(final int maxDelayTime) {
        this.maxDelayTime = maxDelayTime;
    }

    public int getClientManagerThreadPoolQueueCapacity() {
        return clientManagerThreadPoolQueueCapacity;
    }

    public void setClientManagerThreadPoolQueueCapacity(int clientManagerThreadPoolQueueCapacity) {
        this.clientManagerThreadPoolQueueCapacity = clientManagerThreadPoolQueueCapacity;
    }

    public int getConsumerManagerThreadPoolQueueCapacity() {
        return consumerManagerThreadPoolQueueCapacity;
    }

    public void setConsumerManagerThreadPoolQueueCapacity(int consumerManagerThreadPoolQueueCapacity) {
        this.consumerManagerThreadPoolQueueCapacity = consumerManagerThreadPoolQueueCapacity;
    }

    public int getConsumerManageThreadPoolNums() {
        return consumerManageThreadPoolNums;
    }

    public void setConsumerManageThreadPoolNums(int consumerManageThreadPoolNums) {
        this.consumerManageThreadPoolNums = consumerManageThreadPoolNums;
    }

    public int getCommercialBaseCount() {
        return commercialBaseCount;
    }

    public void setCommercialBaseCount(int commercialBaseCount) {
        this.commercialBaseCount = commercialBaseCount;
    }

    public boolean isEnableCalcFilterBitMap() {
        return enableCalcFilterBitMap;
    }

    public void setEnableCalcFilterBitMap(boolean enableCalcFilterBitMap) {
        this.enableCalcFilterBitMap = enableCalcFilterBitMap;
    }

    public int getExpectConsumerNumUseFilter() {
        return expectConsumerNumUseFilter;
    }

    public void setExpectConsumerNumUseFilter(int expectConsumerNumUseFilter) {
        this.expectConsumerNumUseFilter = expectConsumerNumUseFilter;
    }

    public int getMaxErrorRateOfBloomFilter() {
        return maxErrorRateOfBloomFilter;
    }

    public void setMaxErrorRateOfBloomFilter(int maxErrorRateOfBloomFilter) {
        this.maxErrorRateOfBloomFilter = maxErrorRateOfBloomFilter;
    }

    public long getFilterDataCleanTimeSpan() {
        return filterDataCleanTimeSpan;
    }

    public void setFilterDataCleanTimeSpan(long filterDataCleanTimeSpan) {
        this.filterDataCleanTimeSpan = filterDataCleanTimeSpan;
    }

    public boolean isFilterSupportRetry() {
        return filterSupportRetry;
    }

    public void setFilterSupportRetry(boolean filterSupportRetry) {
        this.filterSupportRetry = filterSupportRetry;
    }

    public boolean isEnablePropertyFilter() {
        return enablePropertyFilter;
    }

    public void setEnablePropertyFilter(boolean enablePropertyFilter) {
        this.enablePropertyFilter = enablePropertyFilter;
    }

    public boolean isCompressedRegister() {
        return compressedRegister;
    }

    public void setCompressedRegister(boolean compressedRegister) {
        this.compressedRegister = compressedRegister;
    }

    public boolean isForceRegister() {
        return forceRegister;
    }

    public void setForceRegister(boolean forceRegister) {
        this.forceRegister = forceRegister;
    }

    public int getHeartbeatThreadPoolQueueCapacity() {
        return heartbeatThreadPoolQueueCapacity;
    }

    public void setHeartbeatThreadPoolQueueCapacity(int heartbeatThreadPoolQueueCapacity) {
        this.heartbeatThreadPoolQueueCapacity = heartbeatThreadPoolQueueCapacity;
    }

    public int getHeartbeatThreadPoolNums() {
        return heartbeatThreadPoolNums;
    }

    public void setHeartbeatThreadPoolNums(int heartbeatThreadPoolNums) {
        this.heartbeatThreadPoolNums = heartbeatThreadPoolNums;
    }

    public long getWaitTimeMillsInHeartbeatQueue() {
        return waitTimeMillsInHeartbeatQueue;
    }

    public void setWaitTimeMillsInHeartbeatQueue(long waitTimeMillsInHeartbeatQueue) {
        this.waitTimeMillsInHeartbeatQueue = waitTimeMillsInHeartbeatQueue;
    }

    public int getRegisterNameServerPeriod() {
        return registerNameServerPeriod;
    }

    public void setRegisterNameServerPeriod(int registerNameServerPeriod) {
        this.registerNameServerPeriod = registerNameServerPeriod;
    }

    public long getTransactionTimeOut() {
        return transactionTimeOut;
    }

    public void setTransactionTimeOut(long transactionTimeOut) {
        this.transactionTimeOut = transactionTimeOut;
    }

    public int getTransactionCheckMax() {
        return transactionCheckMax;
    }

    public void setTransactionCheckMax(int transactionCheckMax) {
        this.transactionCheckMax = transactionCheckMax;
    }

    public long getTransactionCheckInterval() {
        return transactionCheckInterval;
    }

    public void setTransactionCheckInterval(long transactionCheckInterval) {
        this.transactionCheckInterval = transactionCheckInterval;
    }

    public int getEndTransactionThreadPoolNums() {
        return endTransactionThreadPoolNums;
    }

    public void setEndTransactionThreadPoolNums(int endTransactionThreadPoolNums) {
        this.endTransactionThreadPoolNums = endTransactionThreadPoolNums;
    }

    public int getEndTransactionPoolQueueCapacity() {
        return endTransactionPoolQueueCapacity;
    }

    public void setEndTransactionPoolQueueCapacity(int endTransactionPoolQueueCapacity) {
        this.endTransactionPoolQueueCapacity = endTransactionPoolQueueCapacity;
    }

    public long getWaitTimeMillsInTransactionQueue() {
        return waitTimeMillsInTransactionQueue;
    }

    public void setWaitTimeMillsInTransactionQueue(long waitTimeMillsInTransactionQueue) {
        this.waitTimeMillsInTransactionQueue = waitTimeMillsInTransactionQueue;
    }

    public String getMsgTraceTopicName() {
        return msgTraceTopicName;
    }

    public void setMsgTraceTopicName(String msgTraceTopicName) {
        this.msgTraceTopicName = msgTraceTopicName;
    }

    public boolean isTraceTopicEnable() {
        return traceTopicEnable;
    }

    public void setTraceTopicEnable(boolean traceTopicEnable) {
        this.traceTopicEnable = traceTopicEnable;
    }

    public boolean isAclEnable() {
        return aclEnable;
    }

    public void setAclEnable(boolean aclEnable) {
        this.aclEnable = aclEnable;
    }

    public boolean isStoreReplyMessageEnable() {
        return storeReplyMessageEnable;
    }

    public void setStoreReplyMessageEnable(boolean storeReplyMessageEnable) {
        this.storeReplyMessageEnable = storeReplyMessageEnable;
    }
}
