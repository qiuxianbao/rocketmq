package cn.thinkinjava.nio;

import cn.thinkinjava.nio.utils.Printer;
import org.junit.Test;

import java.nio.ByteBuffer;

/**
 * 字节缓冲区
 * link <a href="https://juejin.cn/post/7217425505926447161">一文搞懂ByteBuffer使用与原理</a>
 *
 * 缺点：
 * 长度固定，无法扩容
 * API功能有限
 * 读写模式需要切换
 * 线程不安全
 *
 * @author qiuxianbao
 * @date 2024/04/12
 */
public class ByteBufferTest {

    /**
     * ByteBuffer操作
     *
     * @see ByteBuffer#toString
     */
    @Test
    public void testByteBuffer() {

        /**
         * Buffer 4个值的含义
         * @see Buffer
         */
       // 0 <= mark <= position <= limit <= capacity
        ByteBuffer buffer = ByteBuffer.allocate(4);
        Printer.print(buffer);

       // 写模式
       // Buffer#nextPutIndex
       //  buffer.put((byte) 1);
       //  buffer.put("hello".getBytes());
       //  buffer.putDouble(0.1);
        buffer.putInt(0x12345678);
        Printer.print(buffer);

        // 写切换成读模式
        // 如果不切换，java.nio.BufferUnderflowException
         buffer.flip();
         Printer.print(buffer);

        // 读模式
        // 然后position后移一个位置
        // Buffer#nextGetIndex
        // Buffer#nextGetIndex(int nb)
        int twoBytes = buffer.getShort();
        System.out.println(Integer.toHexString(twoBytes));

        /**
         * 创建共享缓冲区，与原先的ByteBuffer共享内存
         * 两个ByteBuffer的position，limit，capacity和mark都是独立的，但是底层存储数据的内存区域是一样的，
         * 那么相应的，对其中任何一个ByteBuffer做更改，会影响到另外一个ByteBuffer
         */
        ByteBuffer slice = buffer.slice();
        Printer.print(buffer);

        twoBytes = slice.getShort();
        System.out.println(Integer.toHexString(twoBytes));


        // 重操作
        //  buffer.rewind();
        //  Printer.print(buffer);

        // 重置，主要就是将位置索引position重新设置到mark标记的位置
        // buffer.mark();
        //  buffer.reset();

        // 读切换成写模式
        //  buffer.clear();

        // compact
        // 主要作用是将缓冲区中尚未读取的字节（即位置从 position 到 limit 之间的字节）移动到缓冲区的起始位置，
        // 然后将 position 设置为写模式下未读字节数的位置，limit 设置为缓冲区容量。这个方法通常用于处理非直接缓冲区，并且在读写操作之间切换时非常有用
        buffer = ByteBuffer.allocate(10);
        buffer.put("Hello".getBytes()); // 写入 "Hello"
        buffer.flip(); // 切换到读模式
        buffer.get(new byte[2]); // 读取前两个字节 "He"
        buffer.compact(); // 将剩余的 "llo" 移动到缓冲区开头，并准备写入更多数据
        Printer.print(buffer);

    }


    /**
     * 堆外内存
     */
    @Test
    public void testDirectByteBuffer() {
        /**
         * 适应场景：
         * 高性能 I/O 操作：如 NIO（非阻塞 I/O）中的直接缓冲区（Direct Buffer），用于提高文件读写、网络传输等操作的性能。
         * 大数据处理：处理大量数据时，将数据存储在堆外内存可以避免频繁的 GC 活动，提升系统整体性能。
         * 缓存：某些缓存框架（如 Redis 客户端、Netty 缓冲区）使用堆外内存来存储临时数据，以减少对堆内存的压力。
         * 分布式系统：在分布式消息队列（如 RocketMQ）、数据库等系统中，堆外内存用于高效地管理和传递消息或数据
         *
         * 堆外内存（Off-Heap Memory）是指不在 Java 虚拟机（JVM）的堆内存（Heap Memory）中分配的内存。它直接使用操作系统提供的本地内存，因此不受 JVM 垃圾回收机制的管理
         * 在【堆外内存】中分配一个直接缓冲区。通过一个 address字段来标识数据所在直接内存的开始地址
         * 说明如下：
         * 1. 创建出来后，position为0，并且limit会取值为capacity；
         * 2. 创建出来的实际为DirectByteBuffer，是基于操作系统创建的内存区域作为缓冲区；
         * 3. 初始时所有元素为0
         *
         * @see java.nio.DirectByteBuffer
         * @see Buffer.address
         */
        ByteBuffer buffer = ByteBuffer.allocateDirect(1024);

        // 会在当前ByteBuffer基础上创建一个新的ByteBuffer，创建出来的ByteBuffer能看见老ByteBuffer的数据（共享同一块内存），但只能读不能写
        ByteBuffer readOnlyBuffer = buffer.asReadOnlyBuffer();

    }

    /**
     * 堆内分配
     */
    @Test
    public void testHeapByteBuffer() {
        /**
         * 在【堆】上分配一个新的堆缓冲区。说明如下：
         * 1. 创建出来后，position为0，并且limit会取值为capacity；
         * 2. 创建出来的实际为 HeapByteBuffer，其内部使用一个字节数组hb存储元素；
         * 3. 初始时hb中所有元素为0
         *
         // * @see HeapByteBuffer
         */
        ByteBuffer buffer = ByteBuffer.allocate(1024);

    }

    /**
     * 创建缓冲区
     */
    @Test
    public void testWrap() {
        /**
         * 将字节数组包装到字节缓冲区，说明如下。
         * 1. 创建出来的是HeapByteBuffer，其内部的hb字节数组就会使用传入的array；
         * 2. 改变HeapByteBuffer会影响array，改变array会影响HeapByteBuffer；
         * 3. capacity取值为array.length；
         * 4. limit取值为off + length；
         * 5. position取值为off
         */
        byte[] bytes = new byte[100];
        ByteBuffer buffer12 = ByteBuffer.wrap(bytes, 0, bytes.length);
    }

}
