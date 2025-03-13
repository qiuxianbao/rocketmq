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
package org.apache.rocketmq.tools.command;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.rocketmq.acl.common.AclUtils;
import org.apache.rocketmq.common.MQVersion;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.remoting.RPCHook;
import org.apache.rocketmq.remoting.protocol.RemotingCommand;
import org.apache.rocketmq.srvutil.ServerUtil;
import org.apache.rocketmq.tools.command.acl.ClusterAclConfigVersionListSubCommand;
import org.apache.rocketmq.tools.command.acl.GetAccessConfigSubCommand;
import org.apache.rocketmq.tools.command.acl.DeleteAccessConfigSubCommand;
import org.apache.rocketmq.tools.command.acl.UpdateAccessConfigSubCommand;
import org.apache.rocketmq.tools.command.acl.UpdateGlobalWhiteAddrSubCommand;
import org.apache.rocketmq.tools.command.broker.BrokerConsumeStatsSubCommad;
import org.apache.rocketmq.tools.command.broker.BrokerStatusSubCommand;
import org.apache.rocketmq.tools.command.broker.CleanExpiredCQSubCommand;
import org.apache.rocketmq.tools.command.broker.CleanUnusedTopicCommand;
import org.apache.rocketmq.tools.command.broker.GetBrokerConfigCommand;
import org.apache.rocketmq.tools.command.broker.SendMsgStatusCommand;
import org.apache.rocketmq.tools.command.broker.UpdateBrokerConfigSubCommand;
import org.apache.rocketmq.tools.command.cluster.CLusterSendMsgRTCommand;
import org.apache.rocketmq.tools.command.cluster.ClusterListSubCommand;
import org.apache.rocketmq.tools.command.connection.ConsumerConnectionSubCommand;
import org.apache.rocketmq.tools.command.connection.ProducerConnectionSubCommand;
import org.apache.rocketmq.tools.command.consumer.ConsumerProgressSubCommand;
import org.apache.rocketmq.tools.command.consumer.ConsumerStatusSubCommand;
import org.apache.rocketmq.tools.command.consumer.DeleteSubscriptionGroupCommand;
import org.apache.rocketmq.tools.command.consumer.StartMonitoringSubCommand;
import org.apache.rocketmq.tools.command.consumer.UpdateSubGroupSubCommand;
import org.apache.rocketmq.tools.command.message.CheckMsgSendRTCommand;
import org.apache.rocketmq.tools.command.message.ConsumeMessageCommand;
import org.apache.rocketmq.tools.command.message.PrintMessageByQueueCommand;
import org.apache.rocketmq.tools.command.message.PrintMessageSubCommand;
import org.apache.rocketmq.tools.command.message.QueryMsgByIdSubCommand;
import org.apache.rocketmq.tools.command.message.QueryMsgByKeySubCommand;
import org.apache.rocketmq.tools.command.message.QueryMsgByOffsetSubCommand;
import org.apache.rocketmq.tools.command.message.QueryMsgByUniqueKeySubCommand;
import org.apache.rocketmq.tools.command.message.SendMessageCommand;
import org.apache.rocketmq.tools.command.namesrv.DeleteKvConfigCommand;
import org.apache.rocketmq.tools.command.namesrv.GetNamesrvConfigCommand;
import org.apache.rocketmq.tools.command.namesrv.UpdateKvConfigCommand;
import org.apache.rocketmq.tools.command.namesrv.UpdateNamesrvConfigCommand;
import org.apache.rocketmq.tools.command.namesrv.WipeWritePermSubCommand;
import org.apache.rocketmq.tools.command.offset.CloneGroupOffsetCommand;
import org.apache.rocketmq.tools.command.offset.ResetOffsetByTimeCommand;
import org.apache.rocketmq.tools.command.queue.QueryConsumeQueueCommand;
import org.apache.rocketmq.tools.command.stats.StatsAllSubCommand;
import org.apache.rocketmq.tools.command.topic.AllocateMQSubCommand;
import org.apache.rocketmq.tools.command.topic.DeleteTopicSubCommand;
import org.apache.rocketmq.tools.command.topic.TopicClusterSubCommand;
import org.apache.rocketmq.tools.command.topic.TopicListSubCommand;
import org.apache.rocketmq.tools.command.topic.TopicRouteSubCommand;
import org.apache.rocketmq.tools.command.topic.TopicStatusSubCommand;
import org.apache.rocketmq.tools.command.topic.UpdateOrderConfCommand;
import org.apache.rocketmq.tools.command.topic.UpdateTopicPermSubCommand;
import org.apache.rocketmq.tools.command.topic.UpdateTopicSubCommand;
import org.slf4j.LoggerFactory;

/**
 * MQ运维命令行工具
 * 可以通过 ./mqadmin [command] -h 查看帮助
 */
public class MQAdminStartup {
    protected static List<SubCommand> subCommandList = new ArrayList<SubCommand>();

    private static String rocketmqHome = System.getProperty(MixAll.ROCKETMQ_HOME_PROPERTY,
        System.getenv(MixAll.ROCKETMQ_HOME_ENV));

    public static void main(String[] args) {
        main0(args, null);
    }

    public static void main0(String[] args, RPCHook rpcHook) {
        System.setProperty(RemotingCommand.REMOTING_VERSION_KEY, Integer.toString(MQVersion.CURRENT_VERSION));

        //PackageConflictDetect.detectFastjson();

        initCommand();

        try {
            initLogback();
            switch (args.length) {
                case 0:
                    printHelp();
                    break;
                case 2:
                    if (args[0].equals("help")) {
                        SubCommand cmd = findSubCommand(args[1]);
                        if (cmd != null) {
                            Options options = ServerUtil.buildCommandlineOptions(new Options());
                            options = cmd.buildCommandlineOptions(options);
                            if (options != null) {
                                ServerUtil.printCommandLineHelp("mqadmin " + cmd.commandName(), options);
                            }
                        } else {
                            System.out.printf("The sub command %s not exist.%n", args[1]);
                        }
                        break;
                    }
                case 1:
                default:
                    SubCommand cmd = findSubCommand(args[0]);
                    if (cmd != null) {
                        String[] subargs = parseSubArgs(args);

                        Options options = ServerUtil.buildCommandlineOptions(new Options());
                        final CommandLine commandLine =
                            ServerUtil.parseCmdLine("mqadmin " + cmd.commandName(), subargs, cmd.buildCommandlineOptions(options),
                                new PosixParser());
                        if (null == commandLine) {
                            return;
                        }

                        if (commandLine.hasOption('n')) {
                            String namesrvAddr = commandLine.getOptionValue('n');
                            System.setProperty(MixAll.NAMESRV_ADDR_PROPERTY, namesrvAddr);
                        }

                        cmd.execute(commandLine, options, AclUtils.getAclRPCHook(rocketmqHome + MixAll.ACL_CONF_TOOLS_FILE));
                    } else {
                        System.out.printf("The sub command %s not exist.%n", args[0]);
                    }
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void initCommand() {
        // 创建或更新主题
        initCommand(new UpdateTopicSubCommand());
        // 删除主题
        initCommand(new DeleteTopicSubCommand());
        // 创建或更新消息费配置信息
        initCommand(new UpdateSubGroupSubCommand());
        // 删除消费组配置信息
        initCommand(new DeleteSubscriptionGroupCommand());
        // 更新broker配置信息
        initCommand(new UpdateBrokerConfigSubCommand());
        // 更新topic的读写权限
        initCommand(new UpdateTopicPermSubCommand());

        // 查看topic路由
        initCommand(new TopicRouteSubCommand());
        // 查看topic的状态（消息消费队列状态）
        initCommand(new TopicStatusSubCommand());
        // 获取topic所在broker集群信息
        initCommand(new TopicClusterSubCommand());

        // 获取broker的状态
        initCommand(new BrokerStatusSubCommand());
        // 根据消息id查询消息
        initCommand(new QueryMsgByIdSubCommand());
        // 根据消息索引键查询消息
        initCommand(new QueryMsgByKeySubCommand());
        // 根据唯一键查询消息
        initCommand(new QueryMsgByUniqueKeySubCommand());
        initCommand(new QueryMsgByOffsetSubCommand());

        // 打印消息
        initCommand(new PrintMessageSubCommand());
        // 根据消息队列打印消息
        initCommand(new PrintMessageByQueueCommand());
        // 测试broker消息发送性能
        initCommand(new SendMsgStatusCommand());
        // 查看broker消费进度
        initCommand(new BrokerConsumeStatsSubCommad());

        initCommand(new ProducerConnectionSubCommand());
        // 查看消息消费者的连接信息
        initCommand(new ConsumerConnectionSubCommand());
        // 查看消费组处理进度（消息消费进度）
        initCommand(new ConsumerProgressSubCommand());
        // 查看消息消费组内部线程状态
        initCommand(new ConsumerStatusSubCommand());
        // 克隆消费组进度
        initCommand(new CloneGroupOffsetCommand());

        // 查看namesrv集群下的broker的运行状态
        initCommand(new ClusterListSubCommand());
        // 查看所有的topic主题信息
        initCommand(new TopicListSubCommand());

        // 更新namesrv的KV配置
        initCommand(new UpdateKvConfigCommand());
        // 删除namesrv的KV配置
        initCommand(new DeleteKvConfigCommand());

        // 擦除broker的写权限
        initCommand(new WipeWritePermSubCommand());
        // 重置消息消费组消费进度
        initCommand(new ResetOffsetByTimeCommand());

        // 创建、更新、删除顺序消息的KV配置
        initCommand(new UpdateOrderConfCommand());
        // 删除过期消息消费队列文件
        initCommand(new CleanExpiredCQSubCommand());
        // 删除未使用的topic
        initCommand(new CleanUnusedTopicCommand());

        // 开启RocketMQ监控
        initCommand(new StartMonitoringSubCommand());
        // 打印主题与消费组的统计信息
        initCommand(new StatsAllSubCommand());

        // 查看消费队列负载情况
        initCommand(new AllocateMQSubCommand());

        // 检查消息发送的响应时间
        // Response Time
        initCommand(new CheckMsgSendRTCommand());
        // 测试集群消息发送的响应时间
        initCommand(new CLusterSendMsgRTCommand());

        // 获取namesrv配置
        initCommand(new GetNamesrvConfigCommand());
        // 更新namesrv配置
        initCommand(new UpdateNamesrvConfigCommand());
        // 获取broker配置信息
        initCommand(new GetBrokerConfigCommand());

        // 查询消息消费进度
        initCommand(new QueryConsumeQueueCommand());
        initCommand(new SendMessageCommand());
        initCommand(new ConsumeMessageCommand());

        //for acl command
        initCommand(new UpdateAccessConfigSubCommand());
        initCommand(new DeleteAccessConfigSubCommand());
        initCommand(new ClusterAclConfigVersionListSubCommand());
        initCommand(new UpdateGlobalWhiteAddrSubCommand());
        initCommand(new GetAccessConfigSubCommand());
    }

    private static void initLogback() throws JoranException {
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(lc);
        lc.reset();
        configurator.doConfigure(rocketmqHome + "/conf/logback_tools.xml");
    }

    private static void printHelp() {
        System.out.printf("The most commonly used mqadmin commands are:%n");
        for (SubCommand cmd : subCommandList) {
            System.out.printf("   %-20s %s%n", cmd.commandName(), cmd.commandDesc());
        }

        System.out.printf("%nSee 'mqadmin help <command>' for more information on a specific command.%n");
    }

    private static SubCommand findSubCommand(final String name) {
        for (SubCommand cmd : subCommandList) {
            if (cmd.commandName().toUpperCase().equals(name.toUpperCase())) {
                return cmd;
            }
        }

        return null;
    }

    private static String[] parseSubArgs(String[] args) {
        if (args.length > 1) {
            String[] result = new String[args.length - 1];
            for (int i = 0; i < args.length - 1; i++) {
                result[i] = args[i + 1];
            }
            return result;
        }
        return null;
    }

    public static void initCommand(SubCommand command) {
        subCommandList.add(command);
    }
}
