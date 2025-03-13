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
package org.apache.rocketmq.tools.command.broker;

import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.rocketmq.common.UtilAll;
import org.apache.rocketmq.common.admin.ConsumeStats;
import org.apache.rocketmq.common.admin.OffsetWrapper;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.common.protocol.body.ConsumeStatsList;
import org.apache.rocketmq.remoting.RPCHook;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.apache.rocketmq.tools.command.SubCommand;
import org.apache.rocketmq.tools.command.SubCommandException;

public class BrokerConsumeStatsSubCommad implements SubCommand {

    private DefaultMQAdminExt defaultMQAdminExt;

    private DefaultMQAdminExt createMQAdminExt(RPCHook rpcHook) throws SubCommandException {
        if (this.defaultMQAdminExt != null) {
            return defaultMQAdminExt;
        } else {
            defaultMQAdminExt = new DefaultMQAdminExt(rpcHook);
            defaultMQAdminExt.setInstanceName(Long.toString(System.currentTimeMillis()));
            try {
                defaultMQAdminExt.start();
            }
            catch (Exception e) {
                throw new SubCommandException(this.getClass().getSimpleName() + " command failed", e);
            }
            return defaultMQAdminExt;
        }
    }

    @Override
    public String commandName() {
        return "brokerConsumeStats";
    }

    @Override
    public String commandDesc() {
        return "Fetch broker consume stats data";
    }

    @Override
    public Options buildCommandlineOptions(Options options) {

        /**
         * 根据boker上的订阅消息组反推出所有消息组订阅的主题
         * 然后统计各消费组在该broker上消息消费队列的消息消费进度
         *
         * [root@localhost bin]# ./mqadmin brokerConsumeStats -n 10.110.104.105:9876 -b 10.110.104.105:10911
         *
         * #Broker Offset，Broker消息消费队列当前偏移量
         * #Consumer Offset，该消息消费组当前消息消费进度
         * #Diff，Broker Offset - Consumer Offset，消息滞留条数
         *
         * #Topic                                                            #Group                                                            #Broker Name                      #QID  #Broker Offset        #Consumer Offset      #Diff                 #LastTime
         * acc_refund_mq_aiparkcity                                          GID_acc_park_order_acs_aiparkcity                                 broker-a                          0     39                    39                    0                     2025-02-20 16:15:02
         * acc_refund_mq_aiparkcity                                          GID_acc_park_order_acs_aiparkcity                                 broker-a                          1     33                    33                    0                     2025-02-20 16:15:02
         * acc_refund_mq_aiparkcity                                          GID_acc_park_order_acs_aiparkcity                                 broker-a                          2     34                    34                    0                     2025-03-07 15:01:17
         * ...
         *
         */
        Option opt = new Option("b", "brokerAddr", true, "Broker address");
        opt.setRequired(true);
        options.addOption(opt);

        opt = new Option("t", "timeoutMillis", true, "request timeout Millis");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("l", "level", true, "threshold of print diff");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("o", "order", true, "order topic");
        opt.setRequired(false);
        options.addOption(opt);

        return options;
    }

    @Override
    public void execute(CommandLine commandLine, Options options, RPCHook rpcHook) throws SubCommandException {
        try {
            defaultMQAdminExt =  createMQAdminExt(rpcHook);

            String brokerAddr = commandLine.getOptionValue('b').trim();
            boolean isOrder = false;
            long timeoutMillis = 50000;
            long diffLevel = 0;
            if (commandLine.hasOption('o')) {
                isOrder = Boolean.parseBoolean(commandLine.getOptionValue('o').trim());
            }
            if (commandLine.hasOption('t')) {
                timeoutMillis = Long.parseLong(commandLine.getOptionValue('t').trim());
            }
            if (commandLine.hasOption('l')) {
                diffLevel = Long.parseLong(commandLine.getOptionValue('l').trim());
            }

            ConsumeStatsList consumeStatsList = defaultMQAdminExt.fetchConsumeStatsInBroker(brokerAddr, isOrder, timeoutMillis);
            System.out.printf("%-32s  %-32s  %-32s  %-4s  %-20s  %-20s  %-20s  %s%n",
                "#Topic",
                "#Group",
                "#Broker Name",
                "#QID",
                "#Broker Offset",
                "#Consumer Offset",
                "#Diff",
                "#LastTime");
            for (Map<String, List<ConsumeStats>> map : consumeStatsList.getConsumeStatsList()) {
                for (Map.Entry<String, List<ConsumeStats>> entry : map.entrySet()) {
                    String group = entry.getKey();
                    List<ConsumeStats> consumeStatsArray = entry.getValue();
                    for (ConsumeStats consumeStats : consumeStatsArray) {
                        List<MessageQueue> mqList = new LinkedList<MessageQueue>();
                        mqList.addAll(consumeStats.getOffsetTable().keySet());
                        Collections.sort(mqList);
                        for (MessageQueue mq : mqList) {
                            OffsetWrapper offsetWrapper = consumeStats.getOffsetTable().get(mq);
                            long diff = offsetWrapper.getBrokerOffset() - offsetWrapper.getConsumerOffset();

                            if (diff < diffLevel) {
                                continue;
                            }
                            String lastTime = "-";
                            try {
                                lastTime = UtilAll.formatDate(new Date(offsetWrapper.getLastTimestamp()), UtilAll.YYYY_MM_DD_HH_MM_SS);
                            } catch (Exception ignored) {

                            }
                            if (offsetWrapper.getLastTimestamp() > 0)
                                System.out.printf("%-32s  %-32s  %-32s  %-4d  %-20d  %-20d  %-20d  %s%n",
                                    UtilAll.frontStringAtLeast(mq.getTopic(), 32),
                                    group,
                                    UtilAll.frontStringAtLeast(mq.getBrokerName(), 32),
                                    mq.getQueueId(),
                                    offsetWrapper.getBrokerOffset(),
                                    offsetWrapper.getConsumerOffset(),
                                    diff,
                                    lastTime
                                );
                        }
                    }
                }
            }
            System.out.printf("%nDiff Total: %d%n", consumeStatsList.getTotalDiff());
        } catch (Exception e) {
            throw new SubCommandException(this.getClass().getSimpleName() + " command failed", e);
        } finally {
            defaultMQAdminExt.shutdown();
        }
    }
}
