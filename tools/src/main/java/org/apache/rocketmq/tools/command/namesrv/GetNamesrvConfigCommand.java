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

package org.apache.rocketmq.tools.command.namesrv;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.rocketmq.common.protocol.RequestCode;
import org.apache.rocketmq.remoting.RPCHook;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.apache.rocketmq.tools.command.SubCommand;
import org.apache.rocketmq.tools.command.SubCommandException;

public class GetNamesrvConfigCommand implements SubCommand {

    @Override
    public String commandName() {
        return "getNamesrvConfig";
    }

    @Override
    public String commandDesc() {
        return "Get configs of name server.";
    }

    @Override
    public Options buildCommandlineOptions(final Options options) {
        return options;
    }

    @Override
    public void execute(final CommandLine commandLine, final Options options,
        final RPCHook rpcHook) throws SubCommandException {
        DefaultMQAdminExt defaultMQAdminExt = new DefaultMQAdminExt(rpcHook);
        defaultMQAdminExt.setInstanceName(Long.toString(System.currentTimeMillis()));
        try {
            // servers
            String servers = commandLine.getOptionValue('n');
            List<String> serverList = null;
            if (servers != null && servers.length() > 0) {
                String[] serverArray = servers.trim().split(";");

                if (serverArray.length > 0) {
                    serverList = Arrays.asList(serverArray);
                }
            }

            defaultMQAdminExt.start();

            /**
             * 向所有的namesrv发送 {@link RequestCode#GET_NAMESRV_CONFIG}
             *
             * [root@localhost bin]# ./mqadmin getNamesrvConfig -n '10.110.104.105:9876;10.110.104.106:9876;10.110.104.107:9876'
             * ============10.110.104.105:9876============
             * writeBufferLowWaterMark                           =  0
             * writeBufferHighWaterMark                          =  0
             * waitSecondsForService                             =  45
             * serverCallbackExecutorThreads                     =  0
             * defaultThreadPoolQueueCapacity                    =  10000
             * useEpollNativeSelector                            =  false
             * notifyMinBrokerIdChanged                          =  false
             * serverSocketBacklog                               =  1024
             * productEnvName                                    =  center
             * configStorePath                                   =  /root/namesrv/namesrv.properties
             * kvConfigPath                                      =  /root/namesrv/kvConfig.json
             * serverAsyncSemaphoreValue                         =  64
             * defaultThreadPoolNums                             =  16
             * enableTopicList                                   =  true
             * rocketmqHome                                      =  /opt/local/rocketmq-all
             * needWaitForService                                =  false
             * serverOnewaySemaphoreValue                        =  256
             * enableControllerInNamesrv                         =  false
             * serverSocketRcvBufSize                            =  0
             * serverPooledByteBufAllocatorEnable                =  true
             * unRegisterBrokerQueueCapacity                     =  3000
             * serverWorkerThreads                               =  8
             * clientRequestThreadPoolQueueCapacity              =  50000
             * clusterTest                                       =  false
             * serverSocketSndBufSize                            =  0
             * clientRequestThreadPoolNums                       =  8
             * supportActingMaster                               =  false
             * scanNotActiveBrokerInterval                       =  5000
             * orderMessageEnable                                =  false
             * returnOrderTopicConfigToBroker                    =  true
             * enableAllTopicList                                =  true
             * serverSelectorThreads                             =  3
             * bindAddress                                       =  0.0.0.0
             * serverChannelMaxIdleTimeSeconds                   =  120
             * listenPort                                        =  9876
             * ============10.110.104.106:9876============
             * writeBufferLowWaterMark                           =  0
             * writeBufferHighWaterMark                          =  0
             * waitSecondsForService                             =  45
             * serverCallbackExecutorThreads                     =  0
             * defaultThreadPoolQueueCapacity                    =  10000
             * useEpollNativeSelector                            =  false
             * notifyMinBrokerIdChanged                          =  false
             * serverSocketBacklog                               =  1024
             * productEnvName                                    =  center
             * configStorePath                                   =  /root/namesrv/namesrv.properties
             * kvConfigPath                                      =  /root/namesrv/kvConfig.json
             * serverAsyncSemaphoreValue                         =  64
             * defaultThreadPoolNums                             =  16
             * enableTopicList                                   =  true
             * rocketmqHome                                      =  /opt/local/rocketmq-all
             * needWaitForService                                =  false
             * serverOnewaySemaphoreValue                        =  256
             * enableControllerInNamesrv                         =  false
             * serverSocketRcvBufSize                            =  0
             * serverPooledByteBufAllocatorEnable                =  true
             * unRegisterBrokerQueueCapacity                     =  3000
             * serverWorkerThreads                               =  8
             * clientRequestThreadPoolQueueCapacity              =  50000
             * clusterTest                                       =  false
             * serverSocketSndBufSize                            =  0
             * clientRequestThreadPoolNums                       =  8
             * supportActingMaster                               =  false
             * scanNotActiveBrokerInterval                       =  5000
             * orderMessageEnable                                =  false
             * returnOrderTopicConfigToBroker                    =  true
             * enableAllTopicList                                =  true
             * serverSelectorThreads                             =  3
             * bindAddress                                       =  0.0.0.0
             * serverChannelMaxIdleTimeSeconds                   =  120
             * listenPort                                        =  9876
             * ============10.110.104.107:9876============
             * writeBufferLowWaterMark                           =  0
             * writeBufferHighWaterMark                          =  0
             * waitSecondsForService                             =  45
             * serverCallbackExecutorThreads                     =  0
             * defaultThreadPoolQueueCapacity                    =  10000
             * useEpollNativeSelector                            =  false
             * notifyMinBrokerIdChanged                          =  false
             * serverSocketBacklog                               =  1024
             * productEnvName                                    =  center
             * configStorePath                                   =  /root/namesrv/namesrv.properties
             * kvConfigPath                                      =  /root/namesrv/kvConfig.json
             * serverAsyncSemaphoreValue                         =  64
             * defaultThreadPoolNums                             =  16
             * enableTopicList                                   =  true
             * rocketmqHome                                      =  /opt/local/rocketmq-all
             * needWaitForService                                =  false
             * serverOnewaySemaphoreValue                        =  256
             * enableControllerInNamesrv                         =  false
             * serverSocketRcvBufSize                            =  0
             * serverPooledByteBufAllocatorEnable                =  true
             * unRegisterBrokerQueueCapacity                     =  3000
             * serverWorkerThreads                               =  8
             * clientRequestThreadPoolQueueCapacity              =  50000
             * clusterTest                                       =  false
             * serverSocketSndBufSize                            =  0
             * clientRequestThreadPoolNums                       =  8
             * supportActingMaster                               =  false
             * scanNotActiveBrokerInterval                       =  5000
             * orderMessageEnable                                =  false
             * returnOrderTopicConfigToBroker                    =  true
             * enableAllTopicList                                =  true
             * serverSelectorThreads                             =  3
             * bindAddress                                       =  0.0.0.0
             * serverChannelMaxIdleTimeSeconds                   =  120
             * listenPort                                        =  9876
             */
            Map<String, Properties> nameServerConfigs = defaultMQAdminExt.getNameServerConfig(serverList);

            for (String server : nameServerConfigs.keySet()) {
                System.out.printf("============%s============\n",
                    server);
                for (Object key : nameServerConfigs.get(server).keySet()) {
                    System.out.printf("%-50s=  %s\n", key, nameServerConfigs.get(server).get(key));
                }
            }
        } catch (Exception e) {
            throw new SubCommandException(this.getClass().getSimpleName() + " command failed", e);
        } finally {
            defaultMQAdminExt.shutdown();
        }
    }
}