package java.nio;

import org.junit.Test;

/**
 * 字节缓冲区
 * 链接：<a href="https://juejin.cn/post/7217425505926447161">...</a>
 *
 * 缺点：
 * 读写模式需要切换
 * 无法扩容
 * 线程不安全
 *
 * @author qiuxianbao
 * @date 2024/04/12
 */
public class ByteBufferTest {


    // TransientStorePool


    @Test
    public void testBuffer() {

        /**
         * Buffer 4个值的含义
         * @see Buffer
         */
//        0 <= mark <= position <= limit <= capacity

//        读模式
//        然后position后移一个位置
//        Buffer#nextGetIndex
//        Buffer#nextGetIndex(int nb)

//        写模式
//        Buffer#nextPutIndex

//        读切换成写模式
//        Buffer#clear

//        写切换成读模式
//        Buffer#flip

//        重操作
//        Buffer#rewind

//        重置，主要就是将位置索引position重新设置到mark标记的位置
//        Buffer#reset


        /**
         * 堆分配
         * 直接内存
         * @see ByteBuffer
         */

        /**
         * 在【堆】上分配一个新的字节缓冲区。说明如下：
         * 1. 创建出来后，position为0，并且limit会取值为capacity；
         * 2. 创建出来的实际为 HeapByteBuffer，其内部使用一个字节数组hb存储元素；
         * 3. 初始时hb中所有元素为0
         *
         * @see HeapByteBuffer
         */
        ByteBuffer buffer1 = ByteBuffer.allocate(1024);


        /**
         * 将字节数组包装到字节缓冲区，说明如下。
         * 1. 创建出来的是HeapByteBuffer，其内部的hb字节数组就会使用传入的array；
         * 2. 改变HeapByteBuffer会影响array，改变array会影响HeapByteBuffer；
         * 3. capacity取值为array.length；
         * 4. limit取值为off + length；
         * 5. position取值为off
         */
        byte[] bytes = new byte[1024];
        ByteBuffer buffer12 = ByteBuffer.wrap(bytes, 0, bytes.length);


        /**
         * 在【直接内存】中分配一个新的字节缓冲区。通过一个 address字段来标识数据所在直接内存的开始地址
         * 说明如下：
         * 1. 创建出来后，position为0，并且limit会取值为capacity；
         * 2. 创建出来的实际为DirectByteBuffer，是基于操作系统创建的内存区域作为缓冲区；
         * 3. 初始时所有元素为0
         *
         * @see java.nio.DirectByteBuffer
         * @see Buffer.address
         */
        ByteBuffer buffer2 = ByteBuffer.allocateDirect(1024);

        /**
         * 在已有的ByteBuffer上创建一个视图，得到一个新的ByteBuffer
         * 两个ByteBuffer的position，limit，capacity和mark都是独立的，但是底层存储数据的内存区域是一样的，
         * 那么相应的，对其中任何一个ByteBuffer做更改，会影响到另外一个ByteBuffer
         */
        ByteBuffer slice = buffer1.slice();

        // 会在当前ByteBuffer基础上创建一个新的ByteBuffer，创建出来的ByteBuffer能看见老ByteBuffer的数据（共享同一块内存），但只能读不能写
        ByteBuffer readOnlyBuffer = buffer2.asReadOnlyBuffer();


//      写操作，单个字节
        buffer1.put((byte) 1);
        // 多个字节
        buffer1.put(bytes);
        buffer1.putDouble(0.1);


//      读操作
        byte b = buffer1.get();


    }

}
