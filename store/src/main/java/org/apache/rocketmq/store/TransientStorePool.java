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

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import java.nio.ByteBuffer;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import org.apache.rocketmq.common.constant.LoggerName;
import org.apache.rocketmq.logging.InternalLogger;
import org.apache.rocketmq.logging.InternalLoggerFactory;
import org.apache.rocketmq.store.config.MessageStoreConfig;
import org.apache.rocketmq.store.util.LibC;
import sun.nio.ch.DirectBuffer;

/**
 * 内存池
 * RocketMQ单独创建一个内存缓冲池，用来临时存储数据，
 * 数据先写入该内存映射中，然后由Commit线程定时将数据从该内存复制到与目的物理文件对应的内存映射中
 *
 *
 * RocketMQ引入该机制的目的是：提供一种内存锁定，将当前堆外内存一直锁定在内存中，避免被进程将内存交换到磁盘
 */
public class TransientStorePool {
    private static final InternalLogger log = InternalLoggerFactory.getLogger(LoggerName.STORE_LOGGER_NAME);

    /**
     * availableBuffers的个数
     * 默认是5
     *
     * 可以通过broker中配置文件中设置 transientStorePoolSize
     */
    private final int poolSize;

    /**
     * 每个ByteBuffer的大小
     * 默认为mappedFileSizeCommitLog，默认为1G
     * 说明TransientStorePool是为commitlog文件服务
     */
    private final int fileSize;

    /**
     * 双端队列
     * ByteBuffer的容器
     */
    private final Deque<ByteBuffer> availableBuffers;
    private final MessageStoreConfig storeConfig;

    public TransientStorePool(final MessageStoreConfig storeConfig) {
        this.storeConfig = storeConfig;
        this.poolSize = storeConfig.getTransientStorePoolSize();
        this.fileSize = storeConfig.getMappedFileSizeCommitLog();
        this.availableBuffers = new ConcurrentLinkedDeque<>();
    }

    /**
     * It's a heavy init method.
     */
    public void init() {

        // 创建堆外内存
        for (int i = 0; i < poolSize; i++) {
            // DirectByteBuffer(堆外内存) + PageCache的两层架构方式，这样子可以实现读写消息分离，
            // 写入消息时候写到的是 DirectByteBuffer——堆外内存中,
            // 读消息走的是 PageCache(对于,DirectByteBuffer 是两步刷盘，一步是刷到 PageCache，还有一步是刷到磁盘文件中)，
            // 带来的好处就是，避免了内存操作的很多容易堵的地方，降低了时延，比如说缺页中断降低，内存加锁，污染页的回写。

            // 并将每个ByteBuffer对象转换为DirectBuffer
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(fileSize);
            // 获取其内存地址，然后使用LibC.INSTANCE.mlock()函数将这些内存地址锁定在内存中，以确保它们不会被交换到磁盘上。
            final long address = ((DirectBuffer) byteBuffer).address();

            // net.java.dev.jna 库
            Pointer pointer = new Pointer(address);

            // Java Native Access 本地库
            // 技巧：利用com.sun.jna.Library类库将该批内存锁定，避免被置换到交换区，提高存储性能
            LibC.INSTANCE.mlock(pointer, new NativeLong(fileSize));
            // 将这些ByteBuffer对象添加到availableBuffers队列中供后续使用
            availableBuffers.offer(byteBuffer);
        }
    }

    public void destroy() {
        for (ByteBuffer byteBuffer : availableBuffers) {
            final long address = ((DirectBuffer) byteBuffer).address();
            Pointer pointer = new Pointer(address);
            LibC.INSTANCE.munlock(pointer, new NativeLong(fileSize));
        }
    }

    /**
     * commit之后就可以重新利用
     * @param byteBuffer
     */
    public void returnBuffer(ByteBuffer byteBuffer) {
        byteBuffer.position(0);
        // CommitLog file size, default is 1G
        byteBuffer.limit(fileSize);
        this.availableBuffers.offerFirst(byteBuffer);
    }

    public ByteBuffer borrowBuffer() {
        ByteBuffer buffer = availableBuffers.pollFirst();
        // 5 * 0.4 = 2
        if (availableBuffers.size() < poolSize * 0.4) {
            log.warn("TransientStorePool only remain {} sheets.", availableBuffers.size());
        }
        return buffer;
    }

    public int availableBufferNums() {
        if (storeConfig.isTransientStorePoolEnable()) {
            return availableBuffers.size();
        }
        return Integer.MAX_VALUE;
    }
}
