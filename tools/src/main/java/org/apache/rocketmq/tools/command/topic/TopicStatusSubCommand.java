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
package org.apache.rocketmq.tools.command.topic;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.rocketmq.common.UtilAll;
import org.apache.rocketmq.common.admin.TopicOffset;
import org.apache.rocketmq.common.admin.TopicStatsTable;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.common.protocol.RequestCode;
import org.apache.rocketmq.remoting.RPCHook;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.apache.rocketmq.tools.command.SubCommand;
import org.apache.rocketmq.tools.command.SubCommandException;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class TopicStatusSubCommand implements SubCommand {

    @Override
    public String commandName() {
        return "topicStatus";
    }

    @Override
    public String commandDesc() {
        return "Examine topic Status info";
    }

    @Override
    public Options buildCommandlineOptions(Options options) {

        /**
         * 首先获取主题的路由信息，然后向broker发送 {@link RequestCode#GET_CONSUME_STATS} 获取该主题在每一个broker上的配置信息并返回主题的队列信息
         * 返回结果以消费组分组
         *
         * [root@mysqlpre bin]# ./mqadmin topicStatus -n 10.110.104.105:9876 -t acc_charging_mq_aiparkcity
         *
         * #Broker Name                      #QID  #Min Offset           #Max Offset             #Last Updated
         * broker-a                          0     56889                 68763                   2025-03-12 18:03:34,074
         * broker-a                          1     56903                 68779                   2025-03-12 18:03:48,945
         * broker-a                          2     56840                 68708                   2025-03-12 18:04:34,223
         * broker-a                          3     56875                 68748                   2025-03-12 18:04:49,060
         * broker-a                          4     56897                 68784                   2025-03-12 18:05:34,350
         * broker-a                          5     56916                 68810                   2025-03-12 18:05:49,197
         * broker-a                          6     56914                 68828                   2025-03-12 18:00:00,192
         * broker-a                          7     56939                 68860                   2025-03-12 18:00:00,192
         * broker-b                          0     56888                 68805                   2025-03-12 18:00:00,198
         * broker-b                          1     56910                 68817                   2025-03-12 18:00:00,203
         * broker-b                          2     56897                 68786                   2025-03-12 18:00:00,203
         * broker-b                          3     56866                 68726                   2025-03-12 18:00:00,203
         * broker-b                          4     56938                 68761                   2025-03-12 18:00:00,202
         * broker-b                          5     56944                 68770                   2025-03-12 18:00:00,203
         * broker-b                          6     56927                 68733                   2025-03-12 18:01:11,173
         * broker-b                          7     56918                 68717                   2025-03-12 18:03:12,245
         * broker-c                          0     56907                 68732                   2025-03-12 18:00:00,193
         * broker-c                          1     56920                 68769                   2025-03-12 18:00:00,191
         * broker-c                          2     56881                 68724                   2025-03-12 18:00:33,725
         * broker-c                          3     56943                 68806                   2025-03-12 18:00:48,535
         * broker-c                          4     56876                 68712                   2025-03-12 18:01:33,837
         * broker-c                          5     56879                 68698                   2025-03-12 18:01:48,691
         * broker-c                          6     56879                 68723                   2025-03-12 18:02:33,958
         * broker-c                          7     56884                 68727                   2025-03-12 18:02:48,812
         *
         */
        Option opt = new Option("t", "topic", true, "topic name");
        opt.setRequired(true);
        options.addOption(opt);
        return options;
    }

    @Override
    public void execute(final CommandLine commandLine, final Options options,
        RPCHook rpcHook) throws SubCommandException {
        DefaultMQAdminExt defaultMQAdminExt = new DefaultMQAdminExt(rpcHook);

        defaultMQAdminExt.setInstanceName(Long.toString(System.currentTimeMillis()));

        try {
            defaultMQAdminExt.start();
            String topic = commandLine.getOptionValue('t').trim();
            TopicStatsTable topicStatsTable = defaultMQAdminExt.examineTopicStats(topic);

            List<MessageQueue> mqList = new LinkedList<MessageQueue>();
            mqList.addAll(topicStatsTable.getOffsetTable().keySet());
            Collections.sort(mqList);

            System.out.printf("%-32s  %-4s  %-20s  %-20s    %s%n",
                "#Broker Name",
                "#QID",
                "#Min Offset",
                "#Max Offset",
                "#Last Updated"
            );

            for (MessageQueue mq : mqList) {
                TopicOffset topicOffset = topicStatsTable.getOffsetTable().get(mq);

                String humanTimestamp = "";
                if (topicOffset.getLastUpdateTimestamp() > 0) {
                    humanTimestamp = UtilAll.timeMillisToHumanString2(topicOffset.getLastUpdateTimestamp());
                }

                System.out.printf("%-32s  %-4d  %-20d  %-20d    %s%n",
                    UtilAll.frontStringAtLeast(mq.getBrokerName(), 32),
                    mq.getQueueId(),
                    topicOffset.getMinOffset(),
                    topicOffset.getMaxOffset(),
                    humanTimestamp
                );
            }
        } catch (Exception e) {
            throw new SubCommandException(this.getClass().getSimpleName() + " command failed", e);
        } finally {
            defaultMQAdminExt.shutdown();
        }
    }
}
