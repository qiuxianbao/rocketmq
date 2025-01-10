package cn.thinkinjava.nio.utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.util.internal.StringUtil;

import java.nio.ByteBuffer;

/**
 * 可视化打印
 *
 * @author qiuxianbao
 * @date 2025/01/06
 */
public class Printer {

    /**
     * 打印
     * @param byteBuffer    java.nio
     */
    public static void print(ByteBuffer byteBuffer) {
        StringBuilder buffer = new StringBuilder(byteBuffer.toString())
                .append("; order: ").append(byteBuffer.order());
        System.out.println(buffer);

        print(Unpooled.wrappedBuffer(byteBuffer.array()));
    }

    /**
     * 打印
     * @param byteBuf   io.netty
     */
    public static void print(ByteBuf byteBuf) {
        StringBuilder builder = new StringBuilder()
                // this.writerIndex - this.readerIndex
                .append("readableBytes: ").append(byteBuf.readableBytes())
                .append("; readerIndex: ").append(byteBuf.readerIndex())
                .append("; writerIndex: ").append(byteBuf.writerIndex())
                .append("; capacity: ").append(byteBuf.capacity())
                .append("; maxCapacity: ").append(byteBuf.maxCapacity())
                .append(StringUtil.NEWLINE)
                .append("content is: ").append(new String(byteBuf.array()))
                .append(StringUtil.NEWLINE);

        // 将 ByteBuf 内容以十六进制格式追加到 StringBuilder
        ByteBufUtil.appendPrettyHexDump(builder, byteBuf);
        System.out.println(builder);
    }
}
