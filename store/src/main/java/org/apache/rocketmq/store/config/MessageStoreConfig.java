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
package org.apache.rocketmq.store.config;

import java.io.File;
import org.apache.rocketmq.common.annotation.ImportantField;
import org.apache.rocketmq.store.CommitLog;
import org.apache.rocketmq.store.ConsumeQueue;

/**
 * 消息存储配置
 *
 * 创建
 * @see org.apache.rocketmq.broker.BrokerStartup#createBrokerController(String[])
 */
public class MessageStoreConfig {

    /**
     * broker存储目录
     * 默认为为用户主目录下的/store
     */
    //The root directory in which the log data is kept
    @ImportantField
    private String storePathRootDir = System.getProperty("user.home") + File.separator + "store";

    /**
     * commitlog的存储目录
     */
    //The directory in which the commitlog is kept
    @ImportantField
    private String storePathCommitLog = System.getProperty("user.home") + File.separator + "store"
        + File.separator + "commitlog";

    /**
     * 单个CommitLog文件，默认大小是1G
     * CommitLog file size,default is 1G
     */
    private int mappedFileSizeCommitLog = 1024 * 1024 * 1024;

    /**
     * 打个消费队列consumequeue文件的大小，默认是30W个条目
     */
    // ConsumeQueue file size,default is 30W
    private int mappedFileSizeConsumeQueue = 300000 * ConsumeQueue.CQ_STORE_UNIT_SIZE;

    /**
     * 是否启用ConsumeQueue扩展属性
     */
    // enable consume queue ext
    private boolean enableConsumeQueueExt = false;

    /**
     * consumequeue扩展属性文件大小，默认48M
     */
    // ConsumeQueue extend file size, 48M
    private int mappedFileSizeConsumeQueueExt = 48 * 1024 * 1024;

    /**
     * consumequeue扩展过滤bitmap大小，默认为为64
     */
    // Bit count of filter bit map.
    // this will be set by pipe of calculate filter bit map.
    private int bitMapLengthConsumeQueueExt = 64;

    /**
     * commitlog刷盘频率
     * {@link CommitLog.FlushRealTimeService} 线程任务运行间隔
     */
    // CommitLog flush interval
    // flush data to disk
    @ImportantField
    private int flushIntervalCommitLog = 500;

    /**
     * commitlog的提交频率
     * {@link CommitLog.CommitRealTimeService} 线程间隔时间
     * 将堆外内存的数据写入到FileChannel的间隔频率
     */
    // Only used if TransientStorePool enabled
    // flush data to FileChannel
    @ImportantField
    private int commitIntervalCommitLog = 200;

    /**
     * 消息存储到commitlog文件时获取锁类型
     * 如果是true，使用Reentrantlock
     * 否则，使用自旋锁
     *
     * 默认为false
     *
     * introduced since 4.0.x. Determine whether to use mutex reentrantLock when putting message.<br/>
     * By default it is set to false indicating using spin lock when putting message.
     */
    private boolean useReentrantLockWhenPutMessage = false;

    /**
     * 是否定时刷盘
     * 默认为false，表示使用await方法等待
     * 如果为true，表示使用Thread.sleep
     */
    // Whether schedule flush,default is real-time
    @ImportantField
    private boolean flushCommitLogTimed = false;

    /**
     * consumequeue文件刷盘频率
     * 默认1s
     */
    // ConsumeQueue flush interval
    private int flushIntervalConsumeQueue = 1000;

    /**
     * 清除过期文件线程调度频率
     * 默认为10s检查一下是否需要清除过期文件
     */
    // Resource reclaim interval
    private int cleanResourceInterval = 10000;

    /**
     * 删除commitlog文件的间隔时间
     * 删除一个文件后，等一下再删除下一个文件
     *
     * 默认为10ms
     */
    // CommitLog removal interval
    private int deleteCommitLogFilesInterval = 100;


    /**
     * 删除consumequeue文件的间隔时间
     * 默认为100ms
     */
    // ConsumeQueue removal interval
    private int deleteConsumeQueueFilesInterval = 100;

    /**
     * 销毁MappedFile被拒绝的最大存活时间，默认为12s
     * 在清除文件时，如果该文件被其他线程所占用（引用次数 > 0，比如读取消息），此时会阻止此次删除任务，同时在第一次试图删除该文件时，记录当前时间戳。
     * destroyMapedFileIntervalForcibly 表示第一次拒绝删除之后能保留的最大时间
     * 在此时间内，同样可以被拒绝删除，同时会将引用减少1000个，超过该时间间隔后，文件将被强制删除
     */
    private int destroyMapedFileIntervalForcibly = 1000 * 120;

    /**
     * 重试删除文件间隔
     * 默认为120s
     * 配合destroyMapedFileIntervalForcibly使用
     */
    private int redeleteHangedFileInterval = 1000 * 120;

    /**
     * 磁盘文件充足的情况下，默认每天什么时候执行删除过期文件
     */
    // When to delete,default is at 4 am
    @ImportantField
    private String deleteWhen = "04";

    /**
     * 表示commitlog，consumequeue文件所在磁盘分区的最大使用量
     * 如果超过该值，需要立即清除过期文件
     */
    private int diskMaxUsedSpaceRatio = 75;

    /**
     * 过期文件的保留时间，默认为72小时
     * 也就是从最后一次更新到现在，如果超过了该时间，则认为是过期文件，可以被删除
     */
    // The number of hours to keep a log file before deleting it (in hours)
    @ImportantField
    private int fileReservedTime = 72;

    // Flow control for ConsumeQueue
    private int putMsgIndexHightWater = 600000;

    /**
     * 默认允许的最大消息体
     */
    // The maximum size of message,default is 4M
    private int maxMessageSize = 1024 * 1024 * 4;

    /**
     * 文件恢复时，是否检查CRC
     * 默认为true
     */
    // Whether check the CRC32 of the records consumed.
    // This ensures no on-the-wire or on-disk corruption to the messages occurred.
    // This check adds some overhead,so it may be disabled in cases seeking extreme performance.
    private boolean checkCRCOnRecover = true;

    /**
     * 一次刷写任务至少需要脏页的数量，默认为4页
     * 如果待刷写数据不足，小于该参数配置的值，将忽略本次刷写任务
     */
    // How many pages are to be flushed when flush CommitLog
    private int flushCommitLogLeastPages = 4;

    /**
     * 一次提交任务至少包含脏页的数量，默认为4页
     * 如果待提交数据不足，小于该参数配置的值，将忽略本次提交任务
     */
    // How many pages are to be committed when commit data to file
    private int commitCommitLogLeastPages = 4;

    /**
     * 用字节0填充整个文件的，每多少页刷盘一次，默认为4096页
     */
    // Flush page size when the disk in warming state
    private int flushLeastPagesWhenWarmMapedFile = 1024 / 4 * 16;

    /**
     * 一次刷盘至少需要脏页的数量，默认为2页
     * 针对consume文件
     */
    // How many pages are to be flushed when flush ConsumeQueue
    private int flushConsumeQueueLeastPages = 2;

    /**
     * commitlog两次刷盘的最大间隔
     * 如果超过该间隔，将忽略flushConsumeQueueLeastPages要求直接执行刷盘操作
     */
    private int flushCommitLogThoroughInterval = 1000 * 10;

    /**
     * commitlog两次提交的最大间隔
     * 如果超过该间隔，将忽略flushCommitLogLeastPages要求直接执行刷盘操作
     */
    private int commitCommitLogThoroughInterval = 200;

    /**
     * consume两次刷盘的最大间隔
     */
    private int flushConsumeQueueThoroughInterval = 1000 * 60;

    /**
     * 一次服务端消息拉取
     * 消息在内存中传输允许的最大传输字节
     *
     * 默认为256K
     */
    @ImportantField
    private int maxTransferBytesOnMessageInMemory = 1024 * 256;

    /**
     * 一次服务端消息拉取
     * 消息在内存中传输运行的最大消息条数
     */
    @ImportantField
    private int maxTransferCountOnMessageInMemory = 32;

    /**
     * 一次服务端消息拉取
     * 消息在磁盘中传输允许的最大传输字节
     */
    @ImportantField
    private int maxTransferBytesOnMessageInDisk = 1024 * 64;

    /**
     * 一次服务端消息拉取
     * 消息在磁盘中传输中的最大消息条数
     */
    @ImportantField
    private int maxTransferCountOnMessageInDisk = 8;


    /**
     * 访问消息的最大比率，
     * 决定是从master上还是从slave上消费
     */
    @ImportantField
    private int accessMessageInMemoryMaxRatio = 40;
    @ImportantField
    private boolean messageIndexEnable = true;

    /**
     * 单个索引文件hash槽的个数
     * hash槽位
     *
     * 默认为500W
     */
    private int maxHashSlotNum = 5000000;

    /**
     * 单个索引文件索引条目个数
     * index条目
     */
    private int maxIndexNum = 5000000 * 4;

    /**
     * 一次查询消息最大返回消息条数
     */
    private int maxMsgsNumBatch = 64;

    /**
     * 消息索引是否安全
     * 默认为false
     *
     * 文件恢复时选择文件检测点（commitlog，consumequeue）的最小值与文件最后更新对比，
     * 如果为true，文件恢复时选择文件检测点保存的索引更新时间作为对比
     */
    @ImportantField
    private boolean messageIndexSafe = false;

    /**
     * master监听端口
     * 从服务器连接该端口
     */
    private int haListenPort = 10912;

    /**
     * master与slave心跳包发送间隔
     */
    private int haSendHeartbeatInterval = 1000 * 5;

    /**
     * master与slave长连接空闲时间
     * 超过该时间将关闭连接
     */
    private int haHousekeepingInterval = 1000 * 20;

    /**
     * 一次HA主从同步传输的最大字节长度
     */
    private int haTransferBatchSize = 1024 * 32;

    /**
     * master服务ip与端口号
     */
    @ImportantField
    private String haMasterAddress = null;

    /**
     * 允许从服务器落后的最大偏移字节数
     * 超过该值，表示slave不可用
     */
    private int haSlaveFallbehindMax = 1024 * 1024 * 256;

    /**
     * broker角色
     * 默认为异步
     */
    @ImportantField
    private BrokerRole brokerRole = BrokerRole.ASYNC_MASTER;

    /**
     * 刷盘方式
     * 默认为异步
     */
    @ImportantField
    private FlushDiskType flushDiskType = FlushDiskType.ASYNC_FLUSH;

    /**
     * 同步刷盘
     * 刷盘超时时间
     */
    private int syncFlushTimeout = 1000 * 5;

    /**
     * 延迟队列等级
     */
    private String messageDelayLevel = "1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h";

    /**
     * 延迟队列拉取进度刷盘间隔
     */
    private long flushDelayOffsetInterval = 1000 * 10;

    /**
     * 是否支持强制删除
     * 默认为true
     */
    @ImportantField
    private boolean cleanFileForciblyEnable = true;

    /**
     * 是否温和地使用MappedFile
     * 默认为false
     *
     * 如果为true，将不强制将内存映射文件锁定在内存中
     */
    private boolean warmMapedFileEnable = false;

    /**
     * 从服务器是否支持offset检测
     */
    private boolean offsetCheckInSlave = false;

    /**
     * 是否支持锁打印信息
     */
    private boolean debugLockEnable = false;

    /**
     * 是否允许重复复制，
     * 默认为false
     */
    private boolean duplicationEnable = false;

    /**
     * 是否统计磁盘的使用情况
     */
    private boolean diskFallRecorded = true;

    /**
     * pullMessage锁占用超过该时间
     * 表示PageCache忙，默认为1s
     */
    private long osPageCacheBusyTimeOutMills = 1000;

    /**
     * 查询消息默认返回条数
     */
    private int defaultQueryMaxNum = 32;

    /**
     * 是否开启内存池化
     * 默认为false
     */
    @ImportantField
    private boolean transientStorePoolEnable = false;

    /**
     * transientStorePool中缓存ByteBuffer个数
     * 默认为5个
     */
    private int transientStorePoolSize = 5;

    /**
     * 从transientStorePool中获取ByteBuffer
     * 是否支持快速失败
     */
    private boolean fastFailIfNoBufferInStorePool = false;

    private boolean enableDLegerCommitLog = false;
    private String dLegerGroup;
    private String dLegerPeers;
    private String dLegerSelfId;

    public boolean isDebugLockEnable() {
        return debugLockEnable;
    }

    public void setDebugLockEnable(final boolean debugLockEnable) {
        this.debugLockEnable = debugLockEnable;
    }

    public boolean isDuplicationEnable() {
        return duplicationEnable;
    }

    public void setDuplicationEnable(final boolean duplicationEnable) {
        this.duplicationEnable = duplicationEnable;
    }

    public long getOsPageCacheBusyTimeOutMills() {
        return osPageCacheBusyTimeOutMills;
    }

    public void setOsPageCacheBusyTimeOutMills(final long osPageCacheBusyTimeOutMills) {
        this.osPageCacheBusyTimeOutMills = osPageCacheBusyTimeOutMills;
    }

    public boolean isDiskFallRecorded() {
        return diskFallRecorded;
    }

    public void setDiskFallRecorded(final boolean diskFallRecorded) {
        this.diskFallRecorded = diskFallRecorded;
    }

    public boolean isWarmMapedFileEnable() {
        return warmMapedFileEnable;
    }

    public void setWarmMapedFileEnable(boolean warmMapedFileEnable) {
        this.warmMapedFileEnable = warmMapedFileEnable;
    }

    public int getMappedFileSizeCommitLog() {
        return mappedFileSizeCommitLog;
    }

    public void setMappedFileSizeCommitLog(int mappedFileSizeCommitLog) {
        this.mappedFileSizeCommitLog = mappedFileSizeCommitLog;
    }

    public int getMappedFileSizeConsumeQueue() {

        int factor = (int) Math.ceil(this.mappedFileSizeConsumeQueue / (ConsumeQueue.CQ_STORE_UNIT_SIZE * 1.0));
        return (int) (factor * ConsumeQueue.CQ_STORE_UNIT_SIZE);
    }

    public void setMappedFileSizeConsumeQueue(int mappedFileSizeConsumeQueue) {
        this.mappedFileSizeConsumeQueue = mappedFileSizeConsumeQueue;
    }

    public boolean isEnableConsumeQueueExt() {
        return enableConsumeQueueExt;
    }

    public void setEnableConsumeQueueExt(boolean enableConsumeQueueExt) {
        this.enableConsumeQueueExt = enableConsumeQueueExt;
    }

    public int getMappedFileSizeConsumeQueueExt() {
        return mappedFileSizeConsumeQueueExt;
    }

    public void setMappedFileSizeConsumeQueueExt(int mappedFileSizeConsumeQueueExt) {
        this.mappedFileSizeConsumeQueueExt = mappedFileSizeConsumeQueueExt;
    }

    public int getBitMapLengthConsumeQueueExt() {
        return bitMapLengthConsumeQueueExt;
    }

    public void setBitMapLengthConsumeQueueExt(int bitMapLengthConsumeQueueExt) {
        this.bitMapLengthConsumeQueueExt = bitMapLengthConsumeQueueExt;
    }

    public int getFlushIntervalCommitLog() {
        return flushIntervalCommitLog;
    }

    public void setFlushIntervalCommitLog(int flushIntervalCommitLog) {
        this.flushIntervalCommitLog = flushIntervalCommitLog;
    }

    public int getFlushIntervalConsumeQueue() {
        return flushIntervalConsumeQueue;
    }

    public void setFlushIntervalConsumeQueue(int flushIntervalConsumeQueue) {
        this.flushIntervalConsumeQueue = flushIntervalConsumeQueue;
    }

    public int getPutMsgIndexHightWater() {
        return putMsgIndexHightWater;
    }

    public void setPutMsgIndexHightWater(int putMsgIndexHightWater) {
        this.putMsgIndexHightWater = putMsgIndexHightWater;
    }

    public int getCleanResourceInterval() {
        return cleanResourceInterval;
    }

    public void setCleanResourceInterval(int cleanResourceInterval) {
        this.cleanResourceInterval = cleanResourceInterval;
    }

    public int getMaxMessageSize() {
        return maxMessageSize;
    }

    public void setMaxMessageSize(int maxMessageSize) {
        this.maxMessageSize = maxMessageSize;
    }

    public boolean isCheckCRCOnRecover() {
        return checkCRCOnRecover;
    }

    public boolean getCheckCRCOnRecover() {
        return checkCRCOnRecover;
    }

    public void setCheckCRCOnRecover(boolean checkCRCOnRecover) {
        this.checkCRCOnRecover = checkCRCOnRecover;
    }

    public String getStorePathCommitLog() {
        return storePathCommitLog;
    }

    public void setStorePathCommitLog(String storePathCommitLog) {
        this.storePathCommitLog = storePathCommitLog;
    }

    public String getDeleteWhen() {
        return deleteWhen;
    }

    public void setDeleteWhen(String deleteWhen) {
        this.deleteWhen = deleteWhen;
    }

    public int getDiskMaxUsedSpaceRatio() {
        if (this.diskMaxUsedSpaceRatio < 10)
            return 10;

        if (this.diskMaxUsedSpaceRatio > 95)
            return 95;

        return diskMaxUsedSpaceRatio;
    }

    public void setDiskMaxUsedSpaceRatio(int diskMaxUsedSpaceRatio) {
        this.diskMaxUsedSpaceRatio = diskMaxUsedSpaceRatio;
    }

    public int getDeleteCommitLogFilesInterval() {
        return deleteCommitLogFilesInterval;
    }

    public void setDeleteCommitLogFilesInterval(int deleteCommitLogFilesInterval) {
        this.deleteCommitLogFilesInterval = deleteCommitLogFilesInterval;
    }

    public int getDeleteConsumeQueueFilesInterval() {
        return deleteConsumeQueueFilesInterval;
    }

    public void setDeleteConsumeQueueFilesInterval(int deleteConsumeQueueFilesInterval) {
        this.deleteConsumeQueueFilesInterval = deleteConsumeQueueFilesInterval;
    }

    public int getMaxTransferBytesOnMessageInMemory() {
        return maxTransferBytesOnMessageInMemory;
    }

    public void setMaxTransferBytesOnMessageInMemory(int maxTransferBytesOnMessageInMemory) {
        this.maxTransferBytesOnMessageInMemory = maxTransferBytesOnMessageInMemory;
    }

    public int getMaxTransferCountOnMessageInMemory() {
        return maxTransferCountOnMessageInMemory;
    }

    public void setMaxTransferCountOnMessageInMemory(int maxTransferCountOnMessageInMemory) {
        this.maxTransferCountOnMessageInMemory = maxTransferCountOnMessageInMemory;
    }

    public int getMaxTransferBytesOnMessageInDisk() {
        return maxTransferBytesOnMessageInDisk;
    }

    public void setMaxTransferBytesOnMessageInDisk(int maxTransferBytesOnMessageInDisk) {
        this.maxTransferBytesOnMessageInDisk = maxTransferBytesOnMessageInDisk;
    }

    public int getMaxTransferCountOnMessageInDisk() {
        return maxTransferCountOnMessageInDisk;
    }

    public void setMaxTransferCountOnMessageInDisk(int maxTransferCountOnMessageInDisk) {
        this.maxTransferCountOnMessageInDisk = maxTransferCountOnMessageInDisk;
    }

    public int getFlushCommitLogLeastPages() {
        return flushCommitLogLeastPages;
    }

    public void setFlushCommitLogLeastPages(int flushCommitLogLeastPages) {
        this.flushCommitLogLeastPages = flushCommitLogLeastPages;
    }

    public int getFlushConsumeQueueLeastPages() {
        return flushConsumeQueueLeastPages;
    }

    public void setFlushConsumeQueueLeastPages(int flushConsumeQueueLeastPages) {
        this.flushConsumeQueueLeastPages = flushConsumeQueueLeastPages;
    }

    public int getFlushCommitLogThoroughInterval() {
        return flushCommitLogThoroughInterval;
    }

    public void setFlushCommitLogThoroughInterval(int flushCommitLogThoroughInterval) {
        this.flushCommitLogThoroughInterval = flushCommitLogThoroughInterval;
    }

    public int getFlushConsumeQueueThoroughInterval() {
        return flushConsumeQueueThoroughInterval;
    }

    public void setFlushConsumeQueueThoroughInterval(int flushConsumeQueueThoroughInterval) {
        this.flushConsumeQueueThoroughInterval = flushConsumeQueueThoroughInterval;
    }

    public int getDestroyMapedFileIntervalForcibly() {
        return destroyMapedFileIntervalForcibly;
    }

    public void setDestroyMapedFileIntervalForcibly(int destroyMapedFileIntervalForcibly) {
        this.destroyMapedFileIntervalForcibly = destroyMapedFileIntervalForcibly;
    }

    public int getFileReservedTime() {
        return fileReservedTime;
    }

    public void setFileReservedTime(int fileReservedTime) {
        this.fileReservedTime = fileReservedTime;
    }

    public int getRedeleteHangedFileInterval() {
        return redeleteHangedFileInterval;
    }

    public void setRedeleteHangedFileInterval(int redeleteHangedFileInterval) {
        this.redeleteHangedFileInterval = redeleteHangedFileInterval;
    }

    public int getAccessMessageInMemoryMaxRatio() {
        return accessMessageInMemoryMaxRatio;
    }

    public void setAccessMessageInMemoryMaxRatio(int accessMessageInMemoryMaxRatio) {
        this.accessMessageInMemoryMaxRatio = accessMessageInMemoryMaxRatio;
    }

    public boolean isMessageIndexEnable() {
        return messageIndexEnable;
    }

    public void setMessageIndexEnable(boolean messageIndexEnable) {
        this.messageIndexEnable = messageIndexEnable;
    }

    public int getMaxHashSlotNum() {
        return maxHashSlotNum;
    }

    public void setMaxHashSlotNum(int maxHashSlotNum) {
        this.maxHashSlotNum = maxHashSlotNum;
    }

    public int getMaxIndexNum() {
        return maxIndexNum;
    }

    public void setMaxIndexNum(int maxIndexNum) {
        this.maxIndexNum = maxIndexNum;
    }

    public int getMaxMsgsNumBatch() {
        return maxMsgsNumBatch;
    }

    public void setMaxMsgsNumBatch(int maxMsgsNumBatch) {
        this.maxMsgsNumBatch = maxMsgsNumBatch;
    }

    public int getHaListenPort() {
        return haListenPort;
    }

    public void setHaListenPort(int haListenPort) {
        this.haListenPort = haListenPort;
    }

    public int getHaSendHeartbeatInterval() {
        return haSendHeartbeatInterval;
    }

    public void setHaSendHeartbeatInterval(int haSendHeartbeatInterval) {
        this.haSendHeartbeatInterval = haSendHeartbeatInterval;
    }

    public int getHaHousekeepingInterval() {
        return haHousekeepingInterval;
    }

    public void setHaHousekeepingInterval(int haHousekeepingInterval) {
        this.haHousekeepingInterval = haHousekeepingInterval;
    }

    public BrokerRole getBrokerRole() {
        return brokerRole;
    }

    public void setBrokerRole(BrokerRole brokerRole) {
        this.brokerRole = brokerRole;
    }

    public void setBrokerRole(String brokerRole) {
        this.brokerRole = BrokerRole.valueOf(brokerRole);
    }

    public int getHaTransferBatchSize() {
        return haTransferBatchSize;
    }

    public void setHaTransferBatchSize(int haTransferBatchSize) {
        this.haTransferBatchSize = haTransferBatchSize;
    }

    public int getHaSlaveFallbehindMax() {
        return haSlaveFallbehindMax;
    }

    public void setHaSlaveFallbehindMax(int haSlaveFallbehindMax) {
        this.haSlaveFallbehindMax = haSlaveFallbehindMax;
    }

    public FlushDiskType getFlushDiskType() {
        return flushDiskType;
    }

    public void setFlushDiskType(FlushDiskType flushDiskType) {
        this.flushDiskType = flushDiskType;
    }

    public void setFlushDiskType(String type) {
        this.flushDiskType = FlushDiskType.valueOf(type);
    }

    public int getSyncFlushTimeout() {
        return syncFlushTimeout;
    }

    public void setSyncFlushTimeout(int syncFlushTimeout) {
        this.syncFlushTimeout = syncFlushTimeout;
    }

    public String getHaMasterAddress() {
        return haMasterAddress;
    }

    public void setHaMasterAddress(String haMasterAddress) {
        this.haMasterAddress = haMasterAddress;
    }

    public String getMessageDelayLevel() {
        return messageDelayLevel;
    }

    public void setMessageDelayLevel(String messageDelayLevel) {
        this.messageDelayLevel = messageDelayLevel;
    }

    public long getFlushDelayOffsetInterval() {
        return flushDelayOffsetInterval;
    }

    public void setFlushDelayOffsetInterval(long flushDelayOffsetInterval) {
        this.flushDelayOffsetInterval = flushDelayOffsetInterval;
    }

    public boolean isCleanFileForciblyEnable() {
        return cleanFileForciblyEnable;
    }

    public void setCleanFileForciblyEnable(boolean cleanFileForciblyEnable) {
        this.cleanFileForciblyEnable = cleanFileForciblyEnable;
    }

    public boolean isMessageIndexSafe() {
        return messageIndexSafe;
    }

    public void setMessageIndexSafe(boolean messageIndexSafe) {
        this.messageIndexSafe = messageIndexSafe;
    }

    public boolean isFlushCommitLogTimed() {
        return flushCommitLogTimed;
    }

    public void setFlushCommitLogTimed(boolean flushCommitLogTimed) {
        this.flushCommitLogTimed = flushCommitLogTimed;
    }

    public String getStorePathRootDir() {
        return storePathRootDir;
    }

    public void setStorePathRootDir(String storePathRootDir) {
        this.storePathRootDir = storePathRootDir;
    }

    public int getFlushLeastPagesWhenWarmMapedFile() {
        return flushLeastPagesWhenWarmMapedFile;
    }

    public void setFlushLeastPagesWhenWarmMapedFile(int flushLeastPagesWhenWarmMapedFile) {
        this.flushLeastPagesWhenWarmMapedFile = flushLeastPagesWhenWarmMapedFile;
    }

    public boolean isOffsetCheckInSlave() {
        return offsetCheckInSlave;
    }

    public void setOffsetCheckInSlave(boolean offsetCheckInSlave) {
        this.offsetCheckInSlave = offsetCheckInSlave;
    }

    public int getDefaultQueryMaxNum() {
        return defaultQueryMaxNum;
    }

    public void setDefaultQueryMaxNum(int defaultQueryMaxNum) {
        this.defaultQueryMaxNum = defaultQueryMaxNum;
    }

    /**
     * 内存池
     * Enable transient commitLog store pool only if transientStorePoolEnable is true and the FlushDiskType is
     * ASYNC_FLUSH
     *
     * @return <tt>true</tt> or <tt>false</tt>
     */
    public boolean isTransientStorePoolEnable() {
        // transientStorePool NIO内存映射机制，
        // 提供了将文件系统中的文件映射到内存机制，实现对文件的操作转换对内存地址的操作，极大的提高了 IO 特性，
        // 但这部分内存并不是常驻内存，可以被置换到交换内存(虚拟内存)，
        // RocketMQ 为了提高消息发送的性能，引入了内存锁定机制，
        // 即将最近需要操作的 commitlog 文件映射到内存，并提供内存锁定功能，确保这些文件始终存在内存中，该机制的控制参数就是 transientStorePoolEnable。
        return transientStorePoolEnable && FlushDiskType.ASYNC_FLUSH == getFlushDiskType()
            && BrokerRole.SLAVE != getBrokerRole();
    }

    public void setTransientStorePoolEnable(final boolean transientStorePoolEnable) {
        this.transientStorePoolEnable = transientStorePoolEnable;
    }

    public int getTransientStorePoolSize() {
        return transientStorePoolSize;
    }

    public void setTransientStorePoolSize(final int transientStorePoolSize) {
        this.transientStorePoolSize = transientStorePoolSize;
    }

    public int getCommitIntervalCommitLog() {
        return commitIntervalCommitLog;
    }

    public void setCommitIntervalCommitLog(final int commitIntervalCommitLog) {
        this.commitIntervalCommitLog = commitIntervalCommitLog;
    }

    public boolean isFastFailIfNoBufferInStorePool() {
        return fastFailIfNoBufferInStorePool;
    }

    public void setFastFailIfNoBufferInStorePool(final boolean fastFailIfNoBufferInStorePool) {
        this.fastFailIfNoBufferInStorePool = fastFailIfNoBufferInStorePool;
    }

    public boolean isUseReentrantLockWhenPutMessage() {
        return useReentrantLockWhenPutMessage;
    }

    public void setUseReentrantLockWhenPutMessage(final boolean useReentrantLockWhenPutMessage) {
        this.useReentrantLockWhenPutMessage = useReentrantLockWhenPutMessage;
    }

    public int getCommitCommitLogLeastPages() {
        return commitCommitLogLeastPages;
    }

    public void setCommitCommitLogLeastPages(final int commitCommitLogLeastPages) {
        this.commitCommitLogLeastPages = commitCommitLogLeastPages;
    }

    public int getCommitCommitLogThoroughInterval() {
        return commitCommitLogThoroughInterval;
    }

    public void setCommitCommitLogThoroughInterval(final int commitCommitLogThoroughInterval) {
        this.commitCommitLogThoroughInterval = commitCommitLogThoroughInterval;
    }

    public String getdLegerGroup() {
        return dLegerGroup;
    }

    public void setdLegerGroup(String dLegerGroup) {
        this.dLegerGroup = dLegerGroup;
    }

    public String getdLegerPeers() {
        return dLegerPeers;
    }

    public void setdLegerPeers(String dLegerPeers) {
        this.dLegerPeers = dLegerPeers;
    }

    public String getdLegerSelfId() {
        return dLegerSelfId;
    }

    public void setdLegerSelfId(String dLegerSelfId) {
        this.dLegerSelfId = dLegerSelfId;
    }

    public boolean isEnableDLegerCommitLog() {
        return enableDLegerCommitLog;
    }

    public void setEnableDLegerCommitLog(boolean enableDLegerCommitLog) {
        this.enableDLegerCommitLog = enableDLegerCommitLog;
    }
}
