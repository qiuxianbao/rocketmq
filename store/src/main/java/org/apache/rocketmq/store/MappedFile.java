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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.rocketmq.common.UtilAll;
import org.apache.rocketmq.common.constant.LoggerName;
import org.apache.rocketmq.logging.InternalLogger;
import org.apache.rocketmq.logging.InternalLoggerFactory;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.message.MessageExtBatch;
import org.apache.rocketmq.store.config.FlushDiskType;
import org.apache.rocketmq.store.config.MessageStoreConfig;
import org.apache.rocketmq.store.util.LibC;
import sun.nio.ch.DirectBuffer;

/**
 * 内存映射文件
 * 封装了File
 */
public class MappedFile extends ReferenceResource {

    /**
     * 操作系统每页大小
     * 4KB
     */
    public static final int OS_PAGE_SIZE = 1024 * 4;
    protected static final InternalLogger log = InternalLoggerFactory.getLogger(LoggerName.STORE_LOGGER_NAME);

    private static final AtomicLong TOTAL_MAPPED_VIRTUAL_MEMORY = new AtomicLong(0);

    /**
     * 当前JVM实例中映射文件个数
     */
    private static final AtomicInteger TOTAL_MAPPED_FILES = new AtomicInteger(0);

    /**
     * 当前该文件的写指针（内存映射文件中的写指针）
     */
    protected final AtomicInteger wrotePosition = new AtomicInteger(0);

    /**
     * 当前文件的提交指针
     * 如果开启了 TransientStorePool，则数据会存储在transientStorePool中，然后提交到内存映射ByteBuffer中，再刷写到磁盘
     */
    protected final AtomicInteger committedPosition = new AtomicInteger(0);

    /**
     * 刷写到磁盘指针
     * 该指针之前的数据持久化到磁盘中
     */
    private final AtomicInteger flushedPosition = new AtomicInteger(0);

    /**
     * 文件大小
     */
    protected int fileSize;

    /**
     * 文件通道
     * 用于实现文件和内存之间的数据传输
     */
    protected FileChannel fileChannel;

    /**
     * 堆外内存ByteBuffer
     * 如果不为空，数据首先将存入到writeBuffer，然后put到FileChannel中
     * Message will put to here first, and then reput to FileChannel if writeBuffer is not null.
     */
    protected ByteBuffer writeBuffer = null;

    /**
     * 内存池
     * transientStorePoolEnable=ture时启用
     * @see MessageStoreConfig#isTransientStorePoolEnable()
     */
    protected TransientStorePool transientStorePool = null;

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 文件的初始偏移量
     * this.fileFromOffset = Long.parseLong(this.file.getName());
     */
    private long fileFromOffset;

    /**
     * 物理文件
     * 对应的就是${ROCKET_HOME}/store/commitlog/文件夹下的文件
     */
    private File file;

    /**
     * 物理文件对应的内存映射
     * @see MappedFile#init(String, int)
     */
    private MappedByteBuffer mappedByteBuffer;

    /**
     * 文件最后一次内容写入时间
     */
    private volatile long storeTimestamp = 0;

    /**
     * 是否是MappedFileQueue中第一个文件
     */
    private boolean firstCreateInQueue = false;

    public MappedFile() {
    }

    /**
     * 构造方法，初始化
     *
     * @param fileName
     * @param fileSize
     * @throws IOException
     */
    public MappedFile(final String fileName, final int fileSize) throws IOException {
        init(fileName, fileSize);
    }

    /**
     * 初始化
     *
     * transientStorePoolEnable=ture 时表示内容先存储到堆外内存，
     * 然后通过Commit线程将数据提交到内存映射Buffer中，
     * 再通过Flush线程将内存映射Buffer中的数据持久化到磁盘中
     *
     * @param fileName
     * @param fileSize
     * @param transientStorePool
     * @throws IOException
     */
    public MappedFile(final String fileName, final int fileSize,
        final TransientStorePool transientStorePool) throws IOException {
        init(fileName, fileSize, transientStorePool);
    }

    public static void ensureDirOK(final String dirName) {
        if (dirName != null) {
            File f = new File(dirName);
            if (!f.exists()) {
                boolean result = f.mkdirs();
                log.info(dirName + " mkdir " + (result ? "OK" : "Failed"));
            }
        }
    }

    public static void clean(final ByteBuffer buffer) {
        if (buffer == null || !buffer.isDirect() || buffer.capacity() == 0)
            return;
        invoke(invoke(viewed(buffer), "cleaner"), "clean");
    }

    private static Object invoke(final Object target, final String methodName, final Class<?>... args) {
        return AccessController.doPrivileged(new PrivilegedAction<Object>() {
            public Object run() {
                try {
                    Method method = method(target, methodName, args);
                    method.setAccessible(true);
                    return method.invoke(target);
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            }
        });
    }

    private static Method method(Object target, String methodName, Class<?>[] args)
        throws NoSuchMethodException {
        try {
            return target.getClass().getMethod(methodName, args);
        } catch (NoSuchMethodException e) {
            return target.getClass().getDeclaredMethod(methodName, args);
        }
    }

    private static ByteBuffer viewed(ByteBuffer buffer) {
        String methodName = "viewedBuffer";
        Method[] methods = buffer.getClass().getMethods();
        for (int i = 0; i < methods.length; i++) {
            if (methods[i].getName().equals("attachment")) {
                methodName = "attachment";
                break;
            }
        }

        // TODO-QIU: 2025年1月3日, 0003
        ByteBuffer viewedBuffer = (ByteBuffer) invoke(buffer, methodName);
        if (viewedBuffer == null)
            return buffer;
        else
            return viewed(viewedBuffer);
    }

    public static int getTotalMappedFiles() {
        return TOTAL_MAPPED_FILES.get();
    }

    public static long getTotalMappedVirtualMemory() {
        return TOTAL_MAPPED_VIRTUAL_MEMORY.get();
    }

    public void init(final String fileName, final int fileSize,
        final TransientStorePool transientStorePool) throws IOException {
        // 初始化 mappedByteBuffer
        init(fileName, fileSize);
        // 初始化 writeBuffer
        this.writeBuffer = transientStorePool.borrowBuffer();
        this.transientStorePool = transientStorePool;
    }

    private void init(final String fileName, final int fileSize) throws IOException {
        this.fileName = fileName;
        this.fileSize = fileSize;
        // 根据文件名初始化文件
        this.file = new File(fileName);
        // 文件名代表文件的起始偏移量
        this.fileFromOffset = Long.parseLong(this.file.getName());
        boolean ok = false;

        // 创建文件夹
        ensureDirOK(this.file.getParent());

        try {
            // 根据物理文件file，创建读写文件通道
            this.fileChannel = new RandomAccessFile(this.file, "rw").getChannel();
            // 通过通道，将文件映射到内存中
            // 即真正意义上的 PageCache
            this.mappedByteBuffer = this.fileChannel.map(MapMode.READ_WRITE, 0, fileSize);
            TOTAL_MAPPED_VIRTUAL_MEMORY.addAndGet(fileSize);
            TOTAL_MAPPED_FILES.incrementAndGet();
            ok = true;
        } catch (FileNotFoundException e) {
            log.error("Failed to create file " + this.fileName, e);
            throw e;
        } catch (IOException e) {
            log.error("Failed to map file " + this.fileName, e);
            throw e;
        } finally {
            if (!ok && this.fileChannel != null) {
                this.fileChannel.close();
            }
        }
    }

    /**
     * 获取文件的最后修改时间
     * 技巧：对File进行封装，对外提供
     *
     * @return
     */
    public long getLastModifiedTimestamp() {
        return this.file.lastModified();
    }

    public int getFileSize() {
        return fileSize;
    }

    public FileChannel getFileChannel() {
        return fileChannel;
    }

    /**
     * 追加消息
     *
     * caller
     * @see CommitLog#putMessage(MessageExtBrokerInner)
     *
     * @param msg
     * @param cb
     * @return
     */
    public AppendMessageResult appendMessage(final MessageExtBrokerInner msg, final AppendMessageCallback cb) {
        return appendMessagesInner(msg, cb);
    }

    public AppendMessageResult appendMessages(final MessageExtBatch messageExtBatch, final AppendMessageCallback cb) {
        return appendMessagesInner(messageExtBatch, cb);
    }

    /**
     * 追加数据
     *
     * @param messageExt
     * @param cb
     * @return
     */
    public AppendMessageResult appendMessagesInner(final MessageExt messageExt, final AppendMessageCallback cb) {
        // VM -ea
        assert messageExt != null;
        assert cb != null;

        // 6.获取MappedFile当前写指针
        int currentPos = this.wrotePosition.get();

        /**
         * 如果currentPos >= this.fileSize 标识已经满了
         * @see org.apache.rocketmq.store.MappedFile#isFull()         *
         */
        if (currentPos < this.fileSize) {

            // TODO-QIU: 2025年1月3日, 0003

            /**
             * writeBuffer != null 说明开启了transientStorePoolEnable 机制
             * 则消息首先写入 writerBuffer 中，如果其为空，则写入 mappedByteBuffer 中
             * this.mappedByteBuffer.slice() 创建一个与MappedFile的共享内存区，并设置position为当前写指针
             */
            ByteBuffer byteBuffer = writeBuffer != null ? writeBuffer.slice() : this.mappedByteBuffer.slice();
            byteBuffer.position(currentPos);

            AppendMessageResult result;
            if (messageExt instanceof MessageExtBrokerInner) {
                // 通过回调函数追加
                result = cb.doAppend(this.getFileFromOffset(), byteBuffer, this.fileSize - currentPos, (MessageExtBrokerInner) messageExt);
            } else if (messageExt instanceof MessageExtBatch) {
                result = cb.doAppend(this.getFileFromOffset(), byteBuffer, this.fileSize - currentPos, (MessageExtBatch) messageExt);
            } else {
                return new AppendMessageResult(AppendMessageStatus.UNKNOWN_ERROR);
            }

            // 写位置类+
            this.wrotePosition.addAndGet(result.getWroteBytes());
            this.storeTimestamp = result.getStoreTimestamp();
            return result;
        }
        log.error("MappedFile.appendMessage return null, wrotePosition: {} fileSize: {}", currentPos, this.fileSize);
        return new AppendMessageResult(AppendMessageStatus.UNKNOWN_ERROR);
    }

    public long getFileFromOffset() {
        return this.fileFromOffset;
    }

    public boolean appendMessage(final byte[] data) {
        int currentPos = this.wrotePosition.get();

        if ((currentPos + data.length) <= this.fileSize) {
            try {
                this.fileChannel.position(currentPos);
                this.fileChannel.write(ByteBuffer.wrap(data));
            } catch (Throwable e) {
                log.error("Error occurred when append message to mappedFile.", e);
            }
            this.wrotePosition.addAndGet(data.length);
            return true;
        }

        return false;
    }

    /**
     * Content of data from offset to offset + length will be wrote to file.
     *
     * @param offset The offset of the subarray to be used.
     * @param length The length of the subarray to be used.
     */
    public boolean appendMessage(final byte[] data, final int offset, final int length) {
        int currentPos = this.wrotePosition.get();

        if ((currentPos + length) <= this.fileSize) {
            try {
                this.fileChannel.position(currentPos);
                this.fileChannel.write(ByteBuffer.wrap(data, offset, length));
            } catch (Throwable e) {
                log.error("Error occurred when append message to mappedFile.", e);
            }
            this.wrotePosition.addAndGet(length);
            return true;
        }

        return false;
    }

    /**
     * 刷盘
     * 是将内存中的数据刷写到磁盘，永久存储到磁盘中
     *
     * @return The current flushed position
     */
    public int flush(final int flushLeastPages) {
        if (this.isAbleToFlush(flushLeastPages)) {
            if (this.hold()) {
                int value = getReadPosition();

                try {
                    // 直接调用#force()将内存中的数据持久化到磁盘上
                    //We only append data to fileChannel or mappedByteBuffer, never both.
                    if (writeBuffer != null || this.fileChannel.position() != 0) {
                        this.fileChannel.force(false);
                    } else {
                        // 如果writeBuffer为空，数据是直接进入到mappedByteBuffer中
                        this.mappedByteBuffer.force();
                    }
                } catch (Throwable e) {
                    log.error("Error occurred when force data to disk.", e);
                }

                // flushedPosition 等于 MappedByteBuffer中的写指针
                this.flushedPosition.set(value);
                this.release();
            } else {
                log.warn("in flush, hold failed, flush offset = " + this.flushedPosition.get());
                this.flushedPosition.set(getReadPosition());
            }
        }
        return this.getFlushedPosition();
    }

    /**
     * 执行提交操作
     *
     * @param commitLeastPages  本次提交最小的页数，如果待提交数据不满commitLeastPages，则不执行本次提交操作，待下一次提交
     * @return
     */
    public int commit(final int commitLeastPages) {
        // 说明commit的操作主题是writeBuffer
        if (writeBuffer == null) {
            //no need to commit data to file channel, so just regard wrotePosition as committedPosition.
            return this.wrotePosition.get();
        }
        // 判断是否可以commit
        if (this.isAbleToCommit(commitLeastPages)) {
            if (this.hold()) {
                commit0(commitLeastPages);
                this.release();
            } else {
                log.warn("in commit, hold failed, commit offset = " + this.committedPosition.get());
            }
        }

        // All dirty data has been committed to FileChannel.
        if (writeBuffer != null && this.transientStorePool != null && this.fileSize == this.committedPosition.get()) {
            this.transientStorePool.returnBuffer(writeBuffer);
            this.writeBuffer = null;
        }

        return this.committedPosition.get();
    }

    /**
     * 将writeBuffer的数据写入到FileChannel通道中
     * @param commitLeastPages
     */
    protected void commit0(final int commitLeastPages) {
        int writePos = this.wrotePosition.get();
        int lastCommittedPosition = this.committedPosition.get();

        if (writePos - this.committedPosition.get() > 0) {
            try {
                // 创建writeBuffer的共享缓冲区
                ByteBuffer byteBuffer = writeBuffer.slice();
                byteBuffer.position(lastCommittedPosition);
                byteBuffer.limit(writePos);

                // 把CommittedPosition 到 wrotePosition 的数据复制写入到FileChannel
                this.fileChannel.position(lastCommittedPosition);
                this.fileChannel.write(byteBuffer);

                // 更新CommittedPosition
                this.committedPosition.set(writePos);
            } catch (Throwable e) {
                log.error("Error occurred when commit data to FileChannel.", e);
            }
        }
    }

    private boolean isAbleToFlush(final int flushLeastPages) {
        int flush = this.flushedPosition.get();
        int write = getReadPosition();

        if (this.isFull()) {
            return true;
        }

        if (flushLeastPages > 0) {
            return ((write / OS_PAGE_SIZE) - (flush / OS_PAGE_SIZE)) >= flushLeastPages;
        }

        return write > flush;
    }

    /**
     * 判断是否可以commit
     *
     * 扩展：脏页
     * 脏页（Dirty Page）是指在操作系统或数据库管理系统中，已经被修改但尚未写回到磁盘或其他持久存储介质的内存页面。
     *
     * 1、定义：
     * 内存页面：操作系统将物理内存划分为固定大小的块，称为页面（Page）。每个页面通常为 4KB 或更大
     * 脏页：当一个内存页面中的数据被修改后，该页面就被称为脏页。脏页中的数据与磁盘上的原始数据不同步，需要最终写回到磁盘以确保数据的一致性。
     *
     * 2、脏页的产生
     * 文件系统缓存：操作系统为了提高 I/O 性能，会将文件数据缓存在内存中。当应用程序对文件进行读写操作时，这些数据首先在内存中处理，导致内存页面变为脏页。
     * 数据库缓冲区：数据库管理系统（如 MySQL、PostgreSQL 等）使用缓冲区来缓存频繁访问的数据。当事务对数据进行修改时，这些修改首先反映在内存中的缓冲区页面上，使其成为脏页。
     * 虚拟内存管理：在虚拟内存系统中，当进程修改其映射到内存中的页面时，这些页面也会变成脏页
     *
     * 3、脏页的处理方式
     * 操作系统和数据库管理系统通过以下机制处理脏页：
     * 后台刷新（Flush）：定期将脏页写回到磁盘，以减少脏页的数量并释放内存空间。这个过程通常由专门的后台线程或守护进程完成。
     * 检查点（Checkpoint）：数据库系统会在特定时间点创建检查点，将所有脏页写回磁盘，并记录当前的状态。这有助于在系统崩溃时快速恢复。
     * 同步写入（Sync Write）：某些情况下，应用程序可能会显式请求将脏页立即写回到磁盘，例如调用 fsync 或 sync 系统调用。
     *
     * 4、脏页的影响
     * 性能影响：过多的脏页会导致频繁的磁盘 I/O 操作，从而降低系统的整体性能。因此，操作系统和数据库管理系统通常会限制脏页的数量，并优化写回策略。
     * 数据一致性：如果系统突然崩溃或断电，未写回磁盘的脏页会导致数据丢失或不一致。因此，及时处理脏页对于保证数据完整性非常重要。
     *
     * 5. 相关配置参数
     * 操作系统和数据库管理系统提供了多种配置参数来控制脏页的行为：
     * Linux：
     * vm.dirty_background_ratio：指定脏页占总内存的比例，超过此比例时，内核会启动【后台刷新】。
     * vm.dirty_ratio：指定脏页的最大比例，超过此比例时，所有写操作都会阻塞，直到脏页数量减少。
     * vm.dirty_expire_centisecs：指定脏页的有效期（以百秒为单位），超过此时间的脏页会被优先写回磁盘。
     *
     * MySQL：
     * innodb_max_dirty_pages_pct：设置 InnoDB 缓冲池中脏页的最大百分比，默认值为 75%。
     * innodb_flush_log_at_trx_commit：控制日志写回磁盘的频率，影响脏页的刷新行为。
     *
     * @param commitLeastPages
     * @return
     */
    protected boolean isAbleToCommit(final int commitLeastPages) {
        int flush = this.committedPosition.get();
        int write = this.wrotePosition.get();

        // 文件已满
        if (this.isFull()) {
            return true;
        }

        if (commitLeastPages > 0) {
            // （当前写指针）与（上一次提交的指针）的差值 除以 页大小，得到脏页的数量
            // 如果大于commitLeastPages则返回true
            return ((write / OS_PAGE_SIZE) - (flush / OS_PAGE_SIZE)) >= commitLeastPages;
        }

        // commitLeastPages <= 0，表示只要存在脏页就提交
        return write > flush;
    }

    public int getFlushedPosition() {
        return flushedPosition.get();
    }

    public void setFlushedPosition(int pos) {
        this.flushedPosition.set(pos);
    }

    /**
     * 判断是否写满数据
     *
     * @return
     */
    public boolean isFull() {
        return this.fileSize == this.wrotePosition.get();
    }

    /**
     * 查找pos到当前最大可读之间的数据
     *
     * @param pos
     * @param size
     * @return
     */
    public SelectMappedBufferResult selectMappedBuffer(int pos, int size) {
        int readPosition = getReadPosition();
        if ((pos + size) <= readPosition) {
            if (this.hold()) {
                // 由于整个写入期间都未曾改变mappedByteBuffer的指针
                // 返回的是整个MappedFile
                ByteBuffer byteBuffer = this.mappedByteBuffer.slice();
                byteBuffer.position(pos);
                ByteBuffer byteBufferNew = byteBuffer.slice();
                byteBufferNew.limit(size);
                return new SelectMappedBufferResult(this.fileFromOffset + pos, byteBufferNew, size, this);
            } else {
                log.warn("matched, but hold failed, request pos: " + pos + ", fileFromOffset: "
                    + this.fileFromOffset);
            }
        } else {
            log.warn("selectMappedBuffer request pos invalid, request pos: " + pos + ", size: " + size
                + ", fileFromOffset: " + this.fileFromOffset);
        }

        return null;
    }

    // 读取消息
    public SelectMappedBufferResult selectMappedBuffer(int pos) {
        int readPosition = getReadPosition();
        if (pos < readPosition && pos >= 0) {
            if (this.hold()) {
                ByteBuffer byteBuffer = this.mappedByteBuffer.slice();
                byteBuffer.position(pos);
                int size = readPosition - pos;
                ByteBuffer byteBufferNew = byteBuffer.slice();
                byteBufferNew.limit(size);
                return new SelectMappedBufferResult(this.fileFromOffset + pos, byteBufferNew, size, this);
            }
        }

        return null;
    }

    @Override
    public boolean cleanup(final long currentRef) {
        // true, 标识MappedFile当前可用，无须清理
        if (this.isAvailable()) {
            log.error("this file[REF:" + currentRef + "] " + this.fileName
                + " have not shutdown, stop unmapping.");
            return false;
        }

        // 判断是否清理完成
        if (this.isCleanupOver()) {
            log.error("this file[REF:" + currentRef + "] " + this.fileName
                + " have cleanup, do not do it again.");
            return true;
        }

        // 堆外内存，清除
        clean(this.mappedByteBuffer);
        // 维护
        TOTAL_MAPPED_VIRTUAL_MEMORY.addAndGet(this.fileSize * (-1));
        TOTAL_MAPPED_FILES.decrementAndGet();
        log.info("unmap file[REF:" + currentRef + "] " + this.fileName + " OK");
        return true;
    }

    /**
     * 关闭文件通道，删除物理文件
     *
     * @param intervalForcibly
     * @return
     */
    public boolean destroy(final long intervalForcibly) {
        this.shutdown(intervalForcibly);

        if (this.isCleanupOver()) {
            try {
                this.fileChannel.close();
                log.info("close file channel " + this.fileName + " OK");

                long beginTime = System.currentTimeMillis();
                boolean result = this.file.delete();
                log.info("delete file[REF:" + this.getRefCount() + "] " + this.fileName
                    + (result ? " OK, " : " Failed, ") + "W:" + this.getWrotePosition() + " M:"
                    + this.getFlushedPosition() + ", "
                    + UtilAll.computeElapsedTimeMilliseconds(beginTime));
            } catch (Exception e) {
                log.warn("close file channel " + this.fileName + " Failed. ", e);
            }

            return true;
        } else {
            log.warn("destroy mapped file[REF:" + this.getRefCount() + "] " + this.fileName
                + " Failed. cleanupOver: " + this.cleanupOver);
        }

        return false;
    }

    public int getWrotePosition() {
        return wrotePosition.get();
    }

    public void setWrotePosition(int pos) {
        this.wrotePosition.set(pos);
    }

    /**
     * 获取当前文件最大的读指针
     *
     * RocketMQ文件的一个组织方式是内存映射文件，预先申请一块连续的固定大小的内存，需要一套指针标识当前最大的有效数据位置
     * 只有提交了的数据（写入到MappedByteBuffer或者FileChannel中的数据）才是安全的数据
     *
     * @return The max position which have valid data
     */
    public int getReadPosition() {
        return this.writeBuffer == null ? this.wrotePosition.get() : this.committedPosition.get();
    }

    public void setCommittedPosition(int pos) {
        this.committedPosition.set(pos);
    }

    public void warmMappedFile(FlushDiskType type, int pages) {
        long beginTime = System.currentTimeMillis();
        ByteBuffer byteBuffer = this.mappedByteBuffer.slice();
        int flush = 0;
        long time = System.currentTimeMillis();
        for (int i = 0, j = 0; i < this.fileSize; i += MappedFile.OS_PAGE_SIZE, j++) {
            byteBuffer.put(i, (byte) 0);
            // force flush when flush disk type is sync
            if (type == FlushDiskType.SYNC_FLUSH) {
                if ((i / OS_PAGE_SIZE) - (flush / OS_PAGE_SIZE) >= pages) {
                    flush = i;
                    mappedByteBuffer.force();
                }
            }

            // prevent gc
            if (j % 1000 == 0) {
                log.info("j={}, costTime={}", j, System.currentTimeMillis() - time);
                time = System.currentTimeMillis();
                try {
                    Thread.sleep(0);
                } catch (InterruptedException e) {
                    log.error("Interrupted", e);
                }
            }
        }

        // force flush when prepare load finished
        if (type == FlushDiskType.SYNC_FLUSH) {
            log.info("mapped file warm-up done, force to disk, mappedFile={}, costTime={}",
                this.getFileName(), System.currentTimeMillis() - beginTime);
            mappedByteBuffer.force();
        }
        log.info("mapped file warm-up done. mappedFile={}, costTime={}", this.getFileName(),
            System.currentTimeMillis() - beginTime);

        this.mlock();
    }

    public String getFileName() {
        return fileName;
    }

    public MappedByteBuffer getMappedByteBuffer() {
        return mappedByteBuffer;
    }

    public ByteBuffer sliceByteBuffer() {
        return this.mappedByteBuffer.slice();
    }

    public long getStoreTimestamp() {
        return storeTimestamp;
    }

    public boolean isFirstCreateInQueue() {
        return firstCreateInQueue;
    }

    public void setFirstCreateInQueue(boolean firstCreateInQueue) {
        this.firstCreateInQueue = firstCreateInQueue;
    }

    public void mlock() {
        final long beginTime = System.currentTimeMillis();
        final long address = ((DirectBuffer) (this.mappedByteBuffer)).address();
        Pointer pointer = new Pointer(address);
        {
            int ret = LibC.INSTANCE.mlock(pointer, new NativeLong(this.fileSize));
            log.info("mlock {} {} {} ret = {} time consuming = {}", address, this.fileName, this.fileSize, ret, System.currentTimeMillis() - beginTime);
        }

        {
            int ret = LibC.INSTANCE.madvise(pointer, new NativeLong(this.fileSize), LibC.MADV_WILLNEED);
            log.info("madvise {} {} {} ret = {} time consuming = {}", address, this.fileName, this.fileSize, ret, System.currentTimeMillis() - beginTime);
        }
    }

    public void munlock() {
        final long beginTime = System.currentTimeMillis();
        final long address = ((DirectBuffer) (this.mappedByteBuffer)).address();
        Pointer pointer = new Pointer(address);
        int ret = LibC.INSTANCE.munlock(pointer, new NativeLong(this.fileSize));
        log.info("munlock {} {} {} ret = {} time consuming = {}", address, this.fileName, this.fileSize, ret, System.currentTimeMillis() - beginTime);
    }

    //testable
    File getFile() {
        return this.file;
    }

    @Override
    public String toString() {
        return this.fileName;
    }
}
