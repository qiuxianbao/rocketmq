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

package org.apache.rocketmq.srvutil;

import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.common.ServiceThread;
import org.apache.rocketmq.common.UtilAll;
import org.apache.rocketmq.common.constant.LoggerName;
import org.apache.rocketmq.logging.InternalLogger;
import org.apache.rocketmq.logging.InternalLoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * 监听器
 * 用于监听TLS文件发生变更时，重新加载context
 */
public class FileWatchService extends ServiceThread {
    private static final InternalLogger log = InternalLoggerFactory.getLogger(LoggerName.COMMON_LOGGER_NAME);

    /**
     * 监控的文件
     */
    private final List<String> watchFiles;

    /**
     * 被监控文件对应的hash值
     * 用于判断文件是否发生改变
     */
    private final List<String> fileCurrentHash;

    /**
     * 监听器
     * 用于监听文件发生改变，重新加载操作
     */
    private final Listener listener;

    /**
     * 文件监听周期
     */
    private static final int WATCH_INTERVAL = 500;

    /**
     * 加密
     */
    private MessageDigest md = MessageDigest.getInstance("MD5");

    public FileWatchService(final String[] watchFiles,
        final Listener listener) throws Exception {
        this.listener = listener;
        // new 文件列表
        this.watchFiles = new ArrayList<>();
        this.fileCurrentHash = new ArrayList<>();

        for (int i = 0; i < watchFiles.length; i++) {
            // 文件是存在的
            if (StringUtils.isNotEmpty(watchFiles[i]) && new File(watchFiles[i]).exists()) {
                // 添加文件路径
                this.watchFiles.add(watchFiles[i]);
                // 添加文件对应的hash值
                this.fileCurrentHash.add(hash(watchFiles[i]));
            }
        }
    }

    @Override
    public String getServiceName() {
        return "FileWatchService";
    }

    @Override
    public void run() {
        log.info(this.getServiceName() + " service started");

        // 运行状态，自旋
        while (!this.isStopped()) {
            try {
                // 阻塞 0.5 s
                this.waitForRunning(WATCH_INTERVAL);

                // 遍历文件是否发生变化
                for (int i = 0; i < watchFiles.size(); i++) {
                    String newHash;
                    try {
                        newHash = hash(watchFiles.get(i));
                    } catch (Exception ignored) {
                        log.warn(this.getServiceName() + " service has exception when calculate the file hash. ", ignored);
                        continue;
                    }
                    // 发生了改变
                    if (!newHash.equals(fileCurrentHash.get(i))) {
                        // 修改hash值
                        fileCurrentHash.set(i, newHash);
                        // 监听进行变更
                        listener.onChanged(watchFiles.get(i));
                    }
                }
            } catch (Exception e) {
                log.warn(this.getServiceName() + " service has exception. ", e);
            }
        }
        log.info(this.getServiceName() + " service end");
    }

    /**
     * 用于比较文件是否发生改变
     *
     * @param filePath
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    private String hash(String filePath) throws IOException, NoSuchAlgorithmException {
        Path path = Paths.get(filePath);
        md.update(Files.readAllBytes(path));
        byte[] hash = md.digest();
        return UtilAll.bytes2string(hash);
    }

    /**
     * 监听器
     * 同于监听文件是否发生改变
     */
    public interface Listener {
        /**
         * Will be called when the target files are changed
         * @param path the changed file path
         */
        void onChanged(String path);
    }
}
