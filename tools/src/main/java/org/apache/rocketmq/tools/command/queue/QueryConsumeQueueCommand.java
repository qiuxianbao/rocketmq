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

package org.apache.rocketmq.tools.command.queue;

import com.alibaba.fastjson.JSON;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.rocketmq.common.protocol.body.ConsumeQueueData;
import org.apache.rocketmq.common.protocol.body.QueryConsumeQueueResponseBody;
import org.apache.rocketmq.common.protocol.route.TopicRouteData;
import org.apache.rocketmq.remoting.RPCHook;
import org.apache.rocketmq.srvutil.ServerUtil;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.apache.rocketmq.tools.command.SubCommand;

public class QueryConsumeQueueCommand implements SubCommand {

    public static void main(String[] args) {
        QueryConsumeQueueCommand cmd = new QueryConsumeQueueCommand();

        Options options = ServerUtil.buildCommandlineOptions(new Options());
        String[] subargs = new String[] {"-t TopicTest", "-q 0", "-i 6447", "-b 100.81.165.119:10911"};
        final CommandLine commandLine =
            ServerUtil.parseCmdLine("mqadmin " + cmd.commandName(), subargs, cmd.buildCommandlineOptions(options),
                new PosixParser());
        cmd.execute(commandLine, options, null);
    }

    @Override
    public String commandName() {
        return "queryCq";
    }

    @Override
    public String commandDesc() {
        return "Query cq command.";
    }

    @Override
    public Options buildCommandlineOptions(Options options) {
        Option opt = new Option("t", "topic", true, "topic name");
        opt.setRequired(true);
        options.addOption(opt);

        /**
         * 如果没有指定-b选项，则选择topic路由表中第一个broker地址
         * 发送 {@link org.apache.rocketmq.common.protocol.RequestCode#QUERY_CONSUME_QUEUE}
         *
         * broker返回该消息消费队列最大最小偏移量；
         * 如果消息消费组不为空，则返回该消息消费组的订阅信息；如果订阅信息不为空，则返回过滤器信息；
         * 然后返回查询到的消息消费条目（物理偏移量、消息长度、TAG HashCode）
         */
        opt = new Option("q", "queue", true, "queue num, ie. 1");
        opt.setRequired(true);
        options.addOption(opt);

        opt = new Option("i", "index", true, "start queue index.");
        opt.setRequired(true);
        options.addOption(opt);

        opt = new Option("c", "count", true, "how many.");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("b", "broker", true, "broker addr.");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("g", "consumer", true, "consumer group.");
        opt.setRequired(false);
        options.addOption(opt);

        return options;
    }

    @Override
    public void execute(CommandLine commandLine, Options options, RPCHook rpcHook) {
        DefaultMQAdminExt defaultMQAdminExt = new DefaultMQAdminExt(rpcHook);

        defaultMQAdminExt.setInstanceName(Long.toString(System.currentTimeMillis()));

        try {
            defaultMQAdminExt.start();

            String topic = commandLine.getOptionValue("t").trim();
            int queueId = Integer.valueOf(commandLine.getOptionValue("q").trim());
            long index = Long.valueOf(commandLine.getOptionValue("i").trim());
            int count = Integer.valueOf(commandLine.getOptionValue("c", "10").trim());
            String broker = null;
            if (commandLine.hasOption("b")) {
                broker = commandLine.getOptionValue("b").trim();
            }
            String consumerGroup = null;
            if (commandLine.hasOption("g")) {
                consumerGroup = commandLine.getOptionValue("g").trim();
            }

            if (broker == null || broker == "") {
                TopicRouteData topicRouteData = defaultMQAdminExt.examineTopicRouteInfo(topic);

                if (topicRouteData == null || topicRouteData.getBrokerDatas() == null
                    || topicRouteData.getBrokerDatas().isEmpty()) {
                    throw new Exception("No topic route data!");
                }

                broker = topicRouteData.getBrokerDatas().get(0).getBrokerAddrs().get(0L);
            }

            QueryConsumeQueueResponseBody queryConsumeQueueResponseBody = defaultMQAdminExt.queryConsumeQueue(
                broker, topic, queueId, index, count, consumerGroup
            );

            if (queryConsumeQueueResponseBody.getSubscriptionData() != null) {
                System.out.printf("Subscription data: \n%s\n", JSON.toJSONString(queryConsumeQueueResponseBody.getSubscriptionData(), true));
                System.out.print("======================================\n");
            }

            if (queryConsumeQueueResponseBody.getFilterData() != null) {
                System.out.printf("Filter data: \n%s\n", queryConsumeQueueResponseBody.getFilterData());
                System.out.print("======================================\n");
            }

            System.out.printf("Queue data: \nmax: %d, min: %d\n", queryConsumeQueueResponseBody.getMaxQueueIndex(),
                queryConsumeQueueResponseBody.getMinQueueIndex());
            System.out.print("======================================\n");

            if (queryConsumeQueueResponseBody.getQueueData() != null) {

                long i = index;
                for (ConsumeQueueData queueData : queryConsumeQueueResponseBody.getQueueData()) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("idx: " + i + "\n");

                    stringBuilder.append(queueData.toString() + "\n");

                    stringBuilder.append("======================================\n");

                    System.out.print(stringBuilder.toString());
                    i++;
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            defaultMQAdminExt.shutdown();
        }
    }
}
