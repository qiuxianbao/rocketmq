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
package org.apache.rocketmq.remoting.netty;

/**
 * netty的网络配置
 */
public class NettyServerConfig implements Cloneable {

    /**
     * 监听端口号
     * 默认被初始化为9876
     * @see org.apache.rocketmq.namesrv.NamesrvStartup#createNamesrvController(String[])
     */
    private int listenPort = 8888;

    /**
     * 业务线程池线程个数
     */
    private int serverWorkerThreads = 8;

    /**
     * Netty public 任务线程池线程个数
     * 根据业务类型会创建不同的线程池，比如消息发送、消息消费、心跳检测
     * 如果该业务类型（RequestCode）未注册线程池，则由public线程池执行
     */
    private int serverCallbackExecutorThreads = 0;

    /**
     * IO 线程池线程个数，
     * 主要是NameSrc、Broker端解析请求、返回相应的线程个数
     * 这类线程主要用于处理网络请求，解析请求包，然后转发到各个业务线程池完成具体的业务操作，然后将结果再返回给调用方
     */
    private int serverSelectorThreads = 3;

    /**
     * send oneway 消息请求的并发度
     * broker端参数
     */
    private int serverOnewaySemaphoreValue = 256;

    /**
     * 异步消息发送最大并发度
     * broker端参数
     */
    private int serverAsyncSemaphoreValue = 64;

    /**
     * 网络连接最大空闲时间，默认是 120s
     * 如果连接空闲时间超过该参数设置的值，连接将关闭
     */
    private int serverChannelMaxIdleTimeSeconds = 120;

    /**
     * 网络 socket 发送Buf 缓冲区大小，默认64K
     */
    private int serverSocketSndBufSize = NettySystemConfig.socketSndbufSize;

    /**
     * 网络 socket 接收Buf 缓冲区大小，默认64K
     */
    private int serverSocketRcvBufSize = NettySystemConfig.socketRcvbufSize;

    /**
     * ByteBuffer是否开启缓存，建议开启
     */
    private boolean serverPooledByteBufAllocatorEnable = true;

    /**
     * 是否启用Epoll IO模型，Linux环境建议开启
     *
     * epoll是一种高效的I/O模型，适用于处理大量的客户端连接
     * 在Linux系统中，epoll是通过本地库(libepoll)提供的，因此如果要使用epoll，需要在编译时进行特殊的配置
     *
     * 资料：
     * <a href="https://blog.csdn.net/z_ryan/article/details/80873449">Linux的5中IO模型</a>
     *
     * （1）阻塞式I/O
     * （2）非阻塞式I/O
     * （3）I/O复用（select，poll，epoll等）
     * （4）信号驱动式I/O（SIGIO）
     * （5）异步I/O（POSIX的aio_系列函数）
     *
     * make make install
     * ../glibc-2.10.1/configure \ --prefix=/usr \ --with-headers=/usr/include \
     * --host=x86_64-linux-gnu \ --build=x86_64-pc-linux-gnu \ --without-gd
     */
    private boolean useEpollNativeSelector = false;

    public int getListenPort() {
        return listenPort;
    }

    public void setListenPort(int listenPort) {
        this.listenPort = listenPort;
    }

    public int getServerWorkerThreads() {
        return serverWorkerThreads;
    }

    public void setServerWorkerThreads(int serverWorkerThreads) {
        this.serverWorkerThreads = serverWorkerThreads;
    }

    public int getServerSelectorThreads() {
        return serverSelectorThreads;
    }

    public void setServerSelectorThreads(int serverSelectorThreads) {
        this.serverSelectorThreads = serverSelectorThreads;
    }

    public int getServerOnewaySemaphoreValue() {
        return serverOnewaySemaphoreValue;
    }

    public void setServerOnewaySemaphoreValue(int serverOnewaySemaphoreValue) {
        this.serverOnewaySemaphoreValue = serverOnewaySemaphoreValue;
    }

    public int getServerCallbackExecutorThreads() {
        return serverCallbackExecutorThreads;
    }

    public void setServerCallbackExecutorThreads(int serverCallbackExecutorThreads) {
        this.serverCallbackExecutorThreads = serverCallbackExecutorThreads;
    }

    public int getServerAsyncSemaphoreValue() {
        return serverAsyncSemaphoreValue;
    }

    public void setServerAsyncSemaphoreValue(int serverAsyncSemaphoreValue) {
        this.serverAsyncSemaphoreValue = serverAsyncSemaphoreValue;
    }

    public int getServerChannelMaxIdleTimeSeconds() {
        return serverChannelMaxIdleTimeSeconds;
    }

    public void setServerChannelMaxIdleTimeSeconds(int serverChannelMaxIdleTimeSeconds) {
        this.serverChannelMaxIdleTimeSeconds = serverChannelMaxIdleTimeSeconds;
    }

    public int getServerSocketSndBufSize() {
        return serverSocketSndBufSize;
    }

    public void setServerSocketSndBufSize(int serverSocketSndBufSize) {
        this.serverSocketSndBufSize = serverSocketSndBufSize;
    }

    public int getServerSocketRcvBufSize() {
        return serverSocketRcvBufSize;
    }

    public void setServerSocketRcvBufSize(int serverSocketRcvBufSize) {
        this.serverSocketRcvBufSize = serverSocketRcvBufSize;
    }

    public boolean isServerPooledByteBufAllocatorEnable() {
        return serverPooledByteBufAllocatorEnable;
    }

    public void setServerPooledByteBufAllocatorEnable(boolean serverPooledByteBufAllocatorEnable) {
        this.serverPooledByteBufAllocatorEnable = serverPooledByteBufAllocatorEnable;
    }

    public boolean isUseEpollNativeSelector() {
        return useEpollNativeSelector;
    }

    public void setUseEpollNativeSelector(boolean useEpollNativeSelector) {
        this.useEpollNativeSelector = useEpollNativeSelector;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return (NettyServerConfig) super.clone();
    }
}
