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
package org.apache.rocketmq.tools.command.consumer;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.rocketmq.client.log.ClientLogger;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.logging.InternalLogger;
import org.apache.rocketmq.remoting.RPCHook;
import org.apache.rocketmq.tools.command.SubCommand;
import org.apache.rocketmq.tools.command.SubCommandException;
import org.apache.rocketmq.tools.monitor.DefaultMonitorListener;
import org.apache.rocketmq.tools.monitor.MonitorConfig;
import org.apache.rocketmq.tools.monitor.MonitorService;

public class StartMonitoringSubCommand implements SubCommand {
    private final InternalLogger log = ClientLogger.getLog();

    @Override
    public String commandName() {
        return "startMonitoring";
    }

    @Override
    public String commandDesc() {
        return "Start Monitoring";
    }

    @Override
    public Options buildCommandlineOptions(Options options) {
        return options;
    }

    @Override
    public void execute(CommandLine commandLine, Options options, RPCHook rpcHook) throws SubCommandException {
        try {

            /**
             * 创建一个消息消费者 订阅 {@link MixAll#OFFSET_MOVED_EVENT} 系统主题
             * 该主题下的消息为被删除的消息，当该主题下有消息到达后报告消息的信息，同时会开启一个定时调度任务，重点关注消息重试的信息
             */
            MonitorService monitorService =
                new MonitorService(new MonitorConfig(), new DefaultMonitorListener(), rpcHook);

            monitorService.start();
        } catch (Exception e) {
            throw new SubCommandException(this.getClass().getSimpleName() + " command failed", e);
        }
    }
}
