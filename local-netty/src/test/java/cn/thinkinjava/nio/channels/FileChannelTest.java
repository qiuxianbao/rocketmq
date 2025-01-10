package cn.thinkinjava.nio.channels;

import org.junit.Test;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * nio 文件通道
 * FileChannel是Java NIO中的一个重要组件。它提供了一种高效、非阻塞的I/O操作方式。FileChannel无法设置为非阻塞模式，它总是运行在阻塞模式下（因为不可以将FileChannel注册到Selector上）
 *
 * 1）文件通道 FileChannel 能够将数据从 I/O 设备中读入(read)到字节缓冲区中，或者将字节缓冲区中的数据写入(write)到 I/O 设备中。
 * 2）文件通道能够转换到 (transferTo) 一个可写通道中，也可以从一个可读通道转换而来（transferFrom）。这种方式使用于通道之间地数据传输，比使用缓冲区更加高效。
 * 3）文件通道能够将文件的部分内容映射（map）到 JVM 堆外内存中，这种方式适合处理大文件，不适合处理小文件，因为映射过程本身开销很大。
 * 4）在对文件进行重要的操作之后，应该将数据刷出刷出(force)到磁盘，避免操作系统崩溃导致的数据丢失。
 *
 * <a href="https://www.cnblogs.com/robothy/p/14235598.html">Java NIO 文件通道 FileChannel 用法</a>
 *
 * @author qiuxianbao
 * @date 2025/01/08
 */
public class FileChannelTest {


    /**
     * mmap
     * 映射文件到直接内存
     * 文件通道 FileChannel 可以将文件的指定范围映射到程序的地址空间中，映射部分使用字节缓冲区的一个子类 MappedByteBuffer 的对象表示，只要对映射字节缓冲区进行操作就能够达到操作文件的效果。
     *
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void testMap() throws IOException, InterruptedException {
        // 通过 ByteBuffer.allocate() 分配的缓冲区是一个 HeapByteBuffer，存在于 JVM 堆中；
        // 而 FileChannle.map() 将文件映射到直接内存，返回的是一个 MappedByteBuffer，存在于堆外的直接内存中；这块内存在 MappedByteBuffer 对象本身被回收之前有效。
        Path path = Paths.get("C:\\Idea\\a-learn\\rocketmq\\local-netty\\src\\test\\java\\cn\\thinkinjava\\nio\\channels\\from");
        // 读写模式
        FileChannel channel = FileChannel.open(path, StandardOpenOption.READ, StandardOpenOption.WRITE);
        // 能够将文件映射到直接内存
        // 调用 map() 方法之后，返回的 MappedByteBuffer 就与 fileChannel 脱离了关系，关闭 fileChannel 对 buf 没有影响
        MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_WRITE, 0, channel.size());

        channel.close();

        // java.lang.UnsupportedOperationException
        // Printer.print(buffer);

        // 将 MappedByteBuffer 转换为字符串
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        String content = new String(bytes, StandardCharsets.UTF_8);
        System.out.println(content);

        // 加载到物理内存
        System.out.println(buffer.isLoaded());
        buffer.load();
        System.out.println(buffer.isLoaded());      // ? false

    }


    /**
     * 通道转换
     * 两个通道之间进行数据转换
     * <a href="https://blog.csdn.net/AwesomeP/article/details/131495350">FileChannel类来实现文件之间的数据传输</a>
     *
     * 文件通道允许从一个 ReadableByteChannel 中直接输入数据，也允许直接往 WritableByteChannel 中写入数据。
     * 实现这两个操作的分别为 transferFrom(ReadableByteChannel src, position, count) 和 transferTo(position, count, WritableChannel target) 方法。
     *
     * 没有文件不会创建文件
     * java.io.FileNotFoundException: C:\Idea\a-learn\rocketmq\local-netty\src\test\java\cn\thinkinjava\nio\channels\from (系统找不到指定的文件。)
     *
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void testTransferTo() throws IOException {
        File fromFile = new File("C:\\Idea\\a-learn\\rocketmq\\local-netty\\src\\test\\java\\cn\\thinkinjava\\nio\\channels\\from");
        File toFile = new File("C:\\Idea\\a-learn\\rocketmq\\local-netty\\src\\test\\java\\cn\\thinkinjava\\nio\\channels\\to");
        try (
                FileInputStream in = new FileInputStream(fromFile);
                FileOutputStream out = new FileOutputStream(toFile)
        ) {
            // 获取只读通道
            FileChannel from = in.getChannel();
            // 获取只写通道
            FileChannel to = out.getChannel();

            long size = from.size();
            System.out.println("File from size: " + size);
            // 多次传输，以解决最多传输2G的问题
            // left 变量代表还剩余多少字节
            for (long left = size; left > 0;) {
                // 这进行通道间的数据传输时，这两个方法比使用 ByteBuffer 作为媒介的效率要高；很多操作系统支持文件系统缓存，两个文件之间实际可能并没有发生复制。
                // 效率高，底层会利用操作系统的零拷贝进行优化, 一次最多传输2G数据
                left -= from.transferTo((size - left), left, to);
            }
        }
    }


    /**
     * 获取文件锁
     *
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void testLock() throws IOException, InterruptedException {
        File file = new File("C:\\Idea\\a-learn\\rocketmq\\local-netty\\src\\test\\java\\cn\\thinkinjava\\nio\\channels\\lock");
        try (RandomAccessFile lockFile = new RandomAccessFile(file, "rw")) {
            FileChannel channel = lockFile.getChannel();
            // 可以通过调用 FileChannel 的 lock() 或者 tryLock() 方法来获得一个文件锁，
            // 获取锁的时候可以指定参数起始位置 position，锁定大小 size，是否共享 shared。
            // 如果没有指定参数，默认参数为 position = 0, size = Long.MAX_VALUE, shared = false。
            // 位置 position 和大小 size 不需要严格与文件保持一致，position 和 size 均可以超过文件的大小范围。例如：文件大小为 100，可以指定位置为 200， 大小为 50；则当文件大小扩展到 250 时，[200,250) 的部分会被锁住。

            // shared 参数指定是排他的还是共享的。
            // 要获取共享锁，文件通道必须是可读的；
            // 要获取排他锁，文件通道必须是可写的。

            // 排它锁，此时同一操作系统下的其它进程不能访问
            FileLock lock = channel.lock(0, Long.MAX_VALUE, false);
            System.out.println("Channel locked in exclusive mode.");
            Thread.sleep(3 * 1000L);
            // 释放锁
            lock.release();

            // 共享锁，此时文件可以被其它文件访问
            lock = channel.lock(0, Long.MAX_VALUE, true);
            System.out.println("Channel locked in shared mode.");
            Thread.sleep(3 * 1000L);
            // 释放锁
            lock.release();


            // 与 lock() 相比，tryLock() 是非阻塞的，无论是否能够获取到锁，它都会立即返回。
            // 若 tryLock() 请求锁定的区域已经被操作系统内的其它的进程锁住了，则返回 null；而 lock() 会阻塞，直到获取到了锁、通道被关闭或者线程被中断为止。
            // FileLock lock = channel.tryLock(0, 1, false);

        }
    }


    /**
     * 写入数据
     *
     * @throws IOException
     */
    @Test
    public void testWrite() throws IOException {
        // 单缓冲区写入
        Path path = Paths.get("C:\\Idea\\a-learn\\rocketmq\\local-netty\\src\\test\\java\\cn\\thinkinjava\\nio\\channels\\from");
        // 写模式
        FileChannel channel = FileChannel.open(path, StandardOpenOption.WRITE);
        ByteBuffer buffer = ByteBuffer.allocate(5);
        byte[] data = "Hello, Java NIO.".getBytes();
        for (int i = 0; i < data.length;) {
            buffer.put(data, i, Math.min(data.length -i, buffer.limit() - buffer.position()));
            buffer.flip();
            // 当使用FileChannel进行读写操作时，需要先创建一个Buffer实例，并将数据读入或写入到Buffer中
            i += channel.write(buffer);
            // 主要作用是将缓冲区中尚未读取的字节（即位置从 position 到 limit 之间的字节）移动到缓冲区的起始位置，
            // 然后将 position 设置为写模式下未读字节数的位置，limit 设置为缓冲区容量。这个方法通常用于处理非直接缓冲区，并且在读写操作之间切换时非常有用
            buffer.compact();
        }

        // 为了减少访问磁盘的次数，通过文件通道对文件进行操作之后可能不会立即刷出到磁盘，此时如果系统崩溃，将导致数据的丢失。为了减少这种风险，在进行了重要数据的操作之后应该调用 force() 方法强制将数据刷出到磁盘。
        // 将尚未写入磁盘的数据强制写到磁盘上，true包括元数据（权限信息等）
        channel.force(true);

        // 多缓冲区写入
        // ByteBuffer key = ByteBuffer.allocate(10), value = ByteBuffer.allocate(10);
        // key.put(data, 0, 3);
        // value.put(data, 4, data.length - 4);
        // ByteBuffer[] buffers = new ByteBuffer[]{key, value};
        // key.flip();
        // value.flip();
        // channel.write(buffers);
        // channel.force(false); // 将数据刷出到磁盘
        // channel.close();
    }


    /**
     * 读取数据
     *
     * @throws IOException
     */
    @Test
    public void testRead() throws IOException {
        Path path = Paths.get("C:\\Idea\\a-learn\\rocketmq\\local-netty\\src\\test\\java\\cn\\thinkinjava\\nio\\channels\\from");
        // 读模式
        FileChannel channel = FileChannel.open(path, StandardOpenOption.READ);
        // 通道大小
        System.out.println(channel.size());

        // 英文
        // 在ASCII编码中，一个英文字符占1个字节。
        // 在UTF-8编码中，一个英文字符也占1个字节。
        // 在GBK编码中，一个英文字符同样占1个字节

        // 中文
        // GBK编码：一个汉字占2个字节。
        // UTF-8编码：一个汉字占3个字节。
        // UTF-16编码：一个汉字占2个字节（对于常用的汉字）或4个字节（对于一些扩展字符）。
        // UTF-32编码：一个汉字固定占4个字节。

        // 读取文件到单缓冲区
        ByteBuffer buffer = ByteBuffer.allocate(19 + 2 * 3);
        while (channel.read(buffer) != -1) {
            // 转读模式
            buffer.flip();
            System.out.println(new String(buffer.array()));
            // 读切换成写
            buffer.clear();
        }


        // 通过position()方法，可以定位到文件的任意位置开始进行操作，还能够将文件映射到直接内存
        channel.position(0);
        // 截取文件，指定长度后面的内容将被删除
        // channel.truncate(1);

        // 读取文件到多缓冲区
        ByteBuffer key = ByteBuffer.allocate(19), value=ByteBuffer.allocate(2 * 3);
        ByteBuffer[] buffers = new ByteBuffer[]{key, value};
        while (channel.read(buffers) != -1) {
            key.flip();
            value.flip();

            System.out.println(new String(key.array()));
            System.out.println(new String(value.array()));

            key.clear();
            value.clear();
        }

        channel.close();
    }


    /**
     * 获取文件通道
     * 三种方式
     *
     * @throws IOException
     */
    @Test
    public void testGetChannel() throws IOException {

        // open方式
        Path path = Paths.get("C:\\Idea\\a-learn\\rocketmq\\local-netty\\src\\test\\java\\cn\\thinkinjava\\nio\\channels\\from");
        FileChannel channel1 = FileChannel.open(path, StandardOpenOption.READ);


        /**
         * 流的方式获取通道
         * <a href="https://blog.csdn.net/AwesomeP/article/details/131495350">FileChannel类来实现文件之间的数据传输</a>
         *
         * 没有文件不会创建文件
         * java.io.FileNotFoundException: C:\Idea\a-learn\rocketmq\local-netty\src\test\java\cn\thinkinjava\nio\channels\from (系统找不到指定的文件。)
         *
         */
        File fromFile = new File("C:\\Idea\\a-learn\\rocketmq\\local-netty\\src\\test\\java\\cn\\thinkinjava\\nio\\channels\\from");
        File toFile = new File("C:\\Idea\\a-learn\\rocketmq\\local-netty\\src\\test\\java\\cn\\thinkinjava\\nio\\channels\\to");
        try (
                FileInputStream in = new FileInputStream(fromFile);
                FileOutputStream out = new FileOutputStream(toFile)
        ) {
            // 获取只读通道
            FileChannel from = in.getChannel();
            // 获取只写通道
            FileChannel to = out.getChannel();

        }

        /**
         * 文件的方式获取通道
         *  <a href="https://cloud.baidu.com/article/2775797">Java NIO中的FileChannel详解</a>
         *
         * 没有文件，会创建文件
         */
        // 创建lock文件
        File file = new File("C:\\Idea\\a-learn\\rocketmq\\local-netty\\src\\test\\java\\cn\\thinkinjava\\nio\\channels\\lock");
        try (RandomAccessFile lockFile = new RandomAccessFile(file, "rw")) {
            FileChannel channel = lockFile.getChannel();
        }

    }


}
