package cn.thinkinjava.nio;
// 包名不能是java开头，Cannot instantiate test(s): java.lang.SecurityException: Prohibited package name: java.buffer

import cn.thinkinjava.nio.utils.Printer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteOrder;

/**
 * 大小端
 * link <a href="https://www.cnblogs.com/HuaJFrame/p/17792088.html">大端小端</a>
 * link <a href="https://segmentfault.com/a/1190000010093082">netty4.x ByteBuf 基本机制及其骨架实现</a>
 *
 * 在网络数据传输过程中都是字节(byte)数据的传输，字节存在两种序列化方式，即大端序和小端序。
 * 一般情况下，基于 TCP 的网络通信约定采用大端字节序，而机器 CPU 的字节序则各有各的不同。
 *
 * 定义：
 * 1、大端(Big-Endian) 数据的【高位字节位】存放在内存的【低地址端】，低位字节存放在内存的高地址端。（人类的读写数值的方式）
 * 2、小端(Little-Endian)数据的【低位字节位】存放在内存的【低地址端】，高位字节存放在内存的高地址端。（计算机电路优先处理低位字节，效率比较高）
 * @see ByteOrder
 *
 *
 * 示例：
 * 16进制的数字0x12345678来说，它有4个字节（一个字节8位，1个16进制数占4位，两个16进制数就是8位，一个字节)
 *
 * 在大端模式下的内存存储布局为
 *  低地址   <===============   高地址
 * 高位字节  <===============  低位字节
 *  0x12   |  0x34  |  0x56  |  0x78
 *
 * 在小端模式下的内存存储布局为
 *  低地址   <===============   高地址
 * 低位字节  <===============  高位字节
 *  0x78   |  0x56  |  0x34  |  0x12
 *
 * @author qiuxianbao
 * @date 2025/01/06
 */
public class ByteBufTest {


    /**
     * ByteBuf 实现了 ReferenceCounted 接口
     * netty 中支持 ByteBuf 的池化，而引用计数就是实现池化的关键技术点，不过并非只有池化的 ByteBuf 才有引用计数，非池化的也会有引用计数。
     */
    @Test
    public void testByteBuf() {
        ByteBuf buffer = Unpooled.buffer();
        buffer.writeInt(0x12345678);
        Printer.print(buffer);

        int readerIndex = buffer.readerIndex();

        // readerIndex += 4;
        short i = buffer.readShort();
        System.out.println(Integer.toHexString(i));
        Printer.print(buffer);

        // readerIndex = 0
        // buffer.discardReadBytes();
        // Printer.print(buffer);

        // writerIndex += 4
        buffer.writeInt(0x12345678);
        Printer.print(buffer);

        /**
         * 扩容原理
         * 如果期望的新容量不超过 4MB，则从 64 字节开始，容量翻倍增长；
         * 如果期望的新容量已经超过了 4MB，，那么就再增加 4 MB 的倍数，直到接近或达到最大容量
         * @see Integer.MAX_VALUE 2G
         */
        // buffer.ensureWritable(4);

    }


    /**
     * 非池化
     * 大端
     */
    @Test
    public void testBigEndian() {
        // 大端写入
        ByteBuf buffer = Unpooled.buffer();
        // big_endian
        buffer.writeInt(0x12345678);
        Printer.print(buffer);

        Assert.assertEquals("12", ByteBufUtil.hexDump(buffer.readBytes(1)));
        Assert.assertEquals(1, buffer.readerIndex());

        // 重置读指针
        buffer.resetReaderIndex();
        Assert.assertEquals(0, buffer.readerIndex());

    }

    /**
     * 非池化
     * 小端
     */
    @Test
    public void testLittleEndian() {
        // 小端写入
        ByteBuf buffer = Unpooled.buffer();
        // little_endian
        buffer.writeIntLE(0x12345678);
        Printer.print(buffer);

        Assert.assertEquals("78", ByteBufUtil.hexDump(buffer.readBytes(1)));

    }

}
