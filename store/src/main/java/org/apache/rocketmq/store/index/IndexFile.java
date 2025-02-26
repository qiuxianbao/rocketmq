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
package org.apache.rocketmq.store.index;

import org.apache.rocketmq.common.constant.LoggerName;
import org.apache.rocketmq.logging.InternalLogger;
import org.apache.rocketmq.logging.InternalLoggerFactory;
import org.apache.rocketmq.store.MappedFile;
import org.apache.rocketmq.store.config.MessageStoreConfig;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.List;

/**
 * 消息索引文件
 * 主要存储消息索引建与Offset的对应关系
 *
 * 设计：
 * RocketMQ引入Hash索引机制为消息建立索引，HashMap的设计包含2个基本点：
 * Hash槽与Hash冲突的链表结构
 *
 */
public class IndexFile {
    private static final InternalLogger log = InternalLoggerFactory.getLogger(LoggerName.STORE_LOGGER_NAME);

    /**
     * 单个槽位大小
     */
    private static int hashSlotSize = 4;

    /**
     * 单个索引大小
     */
    private static int indexSize = 20;

    /**
     * 无效的索引条目下标值
     */
    private static int invalidIndex = 0;

    /**
     * hash槽位，默认500W个Hash槽位
     * 每个Hash槽存储的是落在该Hash槽的hashcode最新的Index的索引
     *
     * @see MessageStoreConfig#maxHashSlotNum
     */
    private final int hashSlotNum;

    /**
     * Index条目列表，默认2000W
     *
     * 每一个Index条目结构如下：
     * 20=4+8+4+4
     * hashcode=4
     * phyOffset=8
     * timeDiff=4
     * preIndexNo=4
     *
     * @see MessageStoreConfig#maxIndexNum
     */
    private final int indexNum;

    private final MappedFile mappedFile;
    private final FileChannel fileChannel;
    private final MappedByteBuffer mappedByteBuffer;

    /**
     * 索引头部
     */
    private final IndexHeader indexHeader;

    /**
     * 构造索引文件
     *
     * indexNum 来源
     * @see org.apache.rocketmq.store.index.IndexService#IndexService(org.apache.rocketmq.store.DefaultMessageStore)
     *
     * @param fileName  文件名
     * @param hashSlotNum  哈希槽数量
     * @param indexNum      索引数量
     * @param endPhyOffset
     * @param endTimestamp
     * @throws IOException
     */
    public IndexFile(final String fileName, final int hashSlotNum, final int indexNum,
        final long endPhyOffset, final long endTimestamp) throws IOException {
        int fileTotalSize =
            IndexHeader.INDEX_HEADER_SIZE + (hashSlotNum * hashSlotSize) + (indexNum * indexSize);
        this.mappedFile = new MappedFile(fileName, fileTotalSize);
        this.fileChannel = this.mappedFile.getFileChannel();
        this.mappedByteBuffer = this.mappedFile.getMappedByteBuffer();
        this.hashSlotNum = hashSlotNum;
        this.indexNum = indexNum;

        ByteBuffer byteBuffer = this.mappedByteBuffer.slice();
        this.indexHeader = new IndexHeader(byteBuffer);

        if (endPhyOffset > 0) {
            this.indexHeader.setBeginPhyOffset(endPhyOffset);
            this.indexHeader.setEndPhyOffset(endPhyOffset);
        }

        if (endTimestamp > 0) {
            this.indexHeader.setBeginTimestamp(endTimestamp);
            this.indexHeader.setEndTimestamp(endTimestamp);
        }
    }

    public String getFileName() {
        return this.mappedFile.getFileName();
    }

    public void load() {
        this.indexHeader.load();
    }

    public void flush() {
        long beginTime = System.currentTimeMillis();
        if (this.mappedFile.hold()) {
            this.indexHeader.updateByteBuffer();
            this.mappedByteBuffer.force();
            this.mappedFile.release();
            log.info("flush index file elapsed time(ms) " + (System.currentTimeMillis() - beginTime));
        }
    }

    public boolean isWriteFull() {
        return this.indexHeader.getIndexCount() >= this.indexNum;
    }

    public boolean destroy(final long intervalForcibly) {
        return this.mappedFile.destroy(intervalForcibly);
    }


    /**
     * 将消息索引键与消息偏移映射关系写入到IndexFile
     * 需要解决hash冲突
     *
     * @param key   消息索引
     * @param phyOffset 消息物理偏移量
     * @param storeTimestamp 消息存储时间
     * @return false为已写满
     */
    public boolean putKey(final String key, final long phyOffset, final long storeTimestamp) {
        if (this.indexHeader.getIndexCount() < this.indexNum) {
            // 1.根据索引key求出hash
            int keyHash = indexKeyHashMethod(key);
            // 2.取余，找到槽位
            int slotPos = keyHash % this.hashSlotNum;
            // hash槽位的物理地址为（索引头部大小 + 槽位下标 * 单个槽位大小）
            int absSlotPos = IndexHeader.INDEX_HEADER_SIZE + slotPos * hashSlotSize;

            FileLock fileLock = null;

            try {

                // fileLock = this.fileChannel.lock(absSlotPos, hashSlotSize,
                // false);
                // 3.获取hash槽位上的值（槽位上存储的是索引条目的下标，根据下标就能找到索引条目，也就找到了偏移量）
                int slotValue = this.mappedByteBuffer.getInt(absSlotPos);
                // 如果hash槽存储的数据小于0或者大于当前索引文件中索引条目下标
                if (slotValue <= invalidIndex || slotValue > this.indexHeader.getIndexCount()) {
                    // 设置为0
                    slotValue = invalidIndex;
                }

                // 计算待存储消息的时间戳与第一条消息时间戳的差值，转换成秒
                long timeDiff = storeTimestamp - this.indexHeader.getBeginTimestamp();

                timeDiff = timeDiff / 1000;

                if (this.indexHeader.getBeginTimestamp() <= 0) {
                    timeDiff = 0;
                } else if (timeDiff > Integer.MAX_VALUE) {
                    timeDiff = Integer.MAX_VALUE;
                } else if (timeDiff < 0) {
                    timeDiff = 0;
                }

                // 4.计算新添加的条目的起始物理偏移量
                // 头部字节长度 + hash槽位数量 * 单个hash槽位大小 + 当前索引条目个数 * 单个索引条目大小
                int absIndexPos =
                    IndexHeader.INDEX_HEADER_SIZE + this.hashSlotNum * hashSlotSize
                        + this.indexHeader.getIndexCount() * indexSize;

                /**
                 * hashcode
                 * 设计：这里存储hashcode而不是key，是为了将index条目设计成定长结构，方便检索与定位条目
                 */
                this.mappedByteBuffer.putInt(absIndexPos, keyHash);
                // 消息物理偏移量
                this.mappedByteBuffer.putLong(absIndexPos + 4, phyOffset);
                // 消息存储时间戳与索引文件时间间戳的差值
                this.mappedByteBuffer.putInt(absIndexPos + 4 + 8, (int) timeDiff);

                // 技巧：Hash冲突的链式解决方案
                // 新的index条目最后4个字节，用来存储上一个条目的index下标
                this.mappedByteBuffer.putInt(absIndexPos + 4 + 8 + 4, slotValue);

                // 设置新的槽位值：将当前Index中包含的索引条目下标存入到Hash槽中
                // 覆盖原先的Hash槽的值
                this.mappedByteBuffer.putInt(absSlotPos, this.indexHeader.getIndexCount());

                // 5.更新索引头信息
                if (this.indexHeader.getIndexCount() <= 1) {
                    this.indexHeader.setBeginPhyOffset(phyOffset);
                    this.indexHeader.setBeginTimestamp(storeTimestamp);
                }

                this.indexHeader.incHashSlotCount();
                this.indexHeader.incIndexCount();
                this.indexHeader.setEndPhyOffset(phyOffset);
                this.indexHeader.setEndTimestamp(storeTimestamp);

                return true;
            } catch (Exception e) {
                log.error("putKey exception, Key: " + key + " KeyHashCode: " + key.hashCode(), e);
            } finally {
                if (fileLock != null) {
                    try {
                        fileLock.release();
                    } catch (IOException e) {
                        log.error("Failed to release the lock", e);
                    }
                }
            }
        } else {
            // 当前文件已写满
            log.warn("Over index file capacity: index count = " + this.indexHeader.getIndexCount()
                + "; index max num = " + this.indexNum);
        }

        return false;
    }

    public int indexKeyHashMethod(final String key) {
        int keyHash = key.hashCode();
        int keyHashPositive = Math.abs(keyHash);
        if (keyHashPositive < 0)
            keyHashPositive = 0;
        return keyHashPositive;
    }

    public long getBeginTimestamp() {
        return this.indexHeader.getBeginTimestamp();
    }

    public long getEndTimestamp() {
        return this.indexHeader.getEndTimestamp();
    }

    public long getEndPhyOffset() {
        return this.indexHeader.getEndPhyOffset();
    }

    public boolean isTimeMatched(final long begin, final long end) {
        boolean result = begin < this.indexHeader.getBeginTimestamp() && end > this.indexHeader.getEndTimestamp();
        result = result || (begin >= this.indexHeader.getBeginTimestamp() && begin <= this.indexHeader.getEndTimestamp());
        result = result || (end >= this.indexHeader.getBeginTimestamp() && end <= this.indexHeader.getEndTimestamp());
        return result;
    }

    /**
     * 根据索引key查找消息物理偏移量
     *
     * @param phyOffsets 查找到的消息物理偏移量
     * @param key 索引key
     * @param maxNum 本次查找最大消息条数
     * @param begin 开始时间戳 单位s
     * @param end 结束时间戳 单位s
     * @param lock
     */
    public void selectPhyOffset(final List<Long> phyOffsets, final String key, final int maxNum,
        final long begin, final long end, boolean lock) {
        if (this.mappedFile.hold()) {
            // 1.根据key算出hashcode
            int keyHash = indexKeyHashMethod(key);
            // 取余，找到hashcode对应的hash槽
            int slotPos = keyHash % this.hashSlotNum;
            int absSlotPos = IndexHeader.INDEX_HEADER_SIZE + slotPos * hashSlotSize;

            FileLock fileLock = null;
            try {
                if (lock) {
                    // fileLock = this.fileChannel.lock(absSlotPos,
                    // hashSlotSize, true);
                }

                // 2.获取槽位值，得到索引条目下标（最新的）
                int slotValue = this.mappedByteBuffer.getInt(absSlotPos);
                // if (fileLock != null) {
                // fileLock.release();
                // fileLock = null;
                // }

                // 3.索引条目不存在，直接返回
                if (slotValue <= invalidIndex || slotValue > this.indexHeader.getIndexCount()
                    || this.indexHeader.getIndexCount() <= 1) {
                } else {
                    for (int nextIndexToRead = slotValue; ; ) {
                        if (phyOffsets.size() >= maxNum) {
                            break;
                        }

                        // 4.计算出条目的物理偏移量
                        int absIndexPos =
                            IndexHeader.INDEX_HEADER_SIZE + this.hashSlotNum * hashSlotSize
                                + nextIndexToRead * indexSize;

                        // 取出hash
                        int keyHashRead = this.mappedByteBuffer.getInt(absIndexPos);
                        // 取出物理偏移量
                        long phyOffsetRead = this.mappedByteBuffer.getLong(absIndexPos + 4);
                        // 取出差值
                        long timeDiff = (long) this.mappedByteBuffer.getInt(absIndexPos + 4 + 8);
                        // 上一个索引条目下标
                        int prevIndexRead = this.mappedByteBuffer.getInt(absIndexPos + 4 + 8 + 4);

                        // 5.
                        if (timeDiff < 0) {
                            break;
                        }

                        timeDiff *= 1000L;

                        long timeRead = this.indexHeader.getBeginTimestamp() + timeDiff;
                        boolean timeMatched = (timeRead >= begin) && (timeRead <= end);

                        if (keyHash == keyHashRead && timeMatched) {
                            // 添加物理值
                            phyOffsets.add(phyOffsetRead);
                        }

                        // 验证前一条索引的index
                        if (prevIndexRead <= invalidIndex
                            || prevIndexRead > this.indexHeader.getIndexCount()
                            || prevIndexRead == nextIndexToRead || timeRead < begin) {
                            break;
                        }

                        // 继续查找（因为是链式结构）
                        nextIndexToRead = prevIndexRead;
                    }
                }
            } catch (Exception e) {
                log.error("selectPhyOffset exception ", e);
            } finally {
                if (fileLock != null) {
                    try {
                        fileLock.release();
                    } catch (IOException e) {
                        log.error("Failed to release the lock", e);
                    }
                }

                this.mappedFile.release();
            }
        }
    }
}
