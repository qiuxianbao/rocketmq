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
package org.apache.rocketmq.common;

import org.apache.rocketmq.common.constant.PermName;

/**
 * Topic主题配置信息
 * 默认存储在 ${ROCKETMQ_HOME}/store/config/topic.json
 *
 * 说明：
 * 4.x版本和5.x版本有区别
 *
 * 示例：
 * {
 * 	"dataVersion":{
 * 		"counter":1,
 * 		"timestamp":1734951568860
 * 	    },
 * 	"topicConfigTable":{
 * 		"SELF_TEST_TOPIC":{
 * 			"order":false,
 * 			"perm":6,
 * 			"readQueueNums":1,
 * 			"topicFilterType":"SINGLE_TAG",
 * 			"topicName":"SELF_TEST_TOPIC",
 * 			"topicSysFlag":0,
 * 			"writeQueueNums":1
 *        },
 * 		"DefaultCluster":{
 * 			"order":false,
 * 			"perm":7,
 * 			"readQueueNums":16,
 * 			"topicFilterType":"SINGLE_TAG",
 * 			"topicName":"DefaultCluster",
 * 			"topicSysFlag":0,
 * 			"writeQueueNums":16
 *        },
 * 		"DefaultCluster_REPLY_TOPIC":{
 * 			"order":false,
 * 			"perm":6,
 * 			"readQueueNums":1,
 * 			"topicFilterType":"SINGLE_TAG",
 * 			"topicName":"DefaultCluster_REPLY_TOPIC",
 * 			"topicSysFlag":0,
 * 			"writeQueueNums":1
 *        },
 * 		"RMQ_SYS_TRANS_HALF_TOPIC":{
 * 			"order":false,
 * 			"perm":6,
 * 			"readQueueNums":1,
 * 			"topicFilterType":"SINGLE_TAG",
 * 			"topicName":"RMQ_SYS_TRANS_HALF_TOPIC",
 * 			"topicSysFlag":0,
 * 			"writeQueueNums":1
 *        },
 * 		"broker-a":{
 * 			"order":false,
 * 			"perm":7,
 * 			"readQueueNums":1,
 * 			"topicFilterType":"SINGLE_TAG",
 * 			"topicName":"broker-a",
 * 			"topicSysFlag":0,
 * 			"writeQueueNums":1
 *        },
 * 		"TBW102":{
 * 			"order":false,
 * 			"perm":7,
 * 			"readQueueNums":8,
 * 			"topicFilterType":"SINGLE_TAG",
 * 			"topicName":"TBW102",
 * 			"topicSysFlag":0,
 * 			"writeQueueNums":8
 *        },
 * 		"BenchmarkTest":{
 * 			"order":false,
 * 			"perm":6,
 * 			"readQueueNums":1024,
 * 			"topicFilterType":"SINGLE_TAG",
 * 			"topicName":"BenchmarkTest",
 * 			"topicSysFlag":0,
 * 			"writeQueueNums":1024
 *        },
 * 		"OFFSET_MOVED_EVENT":{
 * 			"order":false,
 * 			"perm":6,
 * 			"readQueueNums":1,
 * 			"topicFilterType":"SINGLE_TAG",
 * 			"topicName":"OFFSET_MOVED_EVENT",
 * 			"topicSysFlag":0,
 * 			"writeQueueNums":1
 *        }
 *    }
 * }
 */
public class TopicConfig {
    private static final String SEPARATOR = " ";
    public static int defaultReadQueueNums = 16;
    public static int defaultWriteQueueNums = 16;
    private String topicName;
    private int readQueueNums = defaultReadQueueNums;
    private int writeQueueNums = defaultWriteQueueNums;
    private int perm = PermName.PERM_READ | PermName.PERM_WRITE;
    private TopicFilterType topicFilterType = TopicFilterType.SINGLE_TAG;
    private int topicSysFlag = 0;
    private boolean order = false;

    public TopicConfig() {
    }

    public TopicConfig(String topicName) {
        this.topicName = topicName;
    }

    public TopicConfig(String topicName, int readQueueNums, int writeQueueNums, int perm) {
        this.topicName = topicName;
        this.readQueueNums = readQueueNums;
        this.writeQueueNums = writeQueueNums;
        this.perm = perm;
    }

    public String encode() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.topicName);
        sb.append(SEPARATOR);
        sb.append(this.readQueueNums);
        sb.append(SEPARATOR);
        sb.append(this.writeQueueNums);
        sb.append(SEPARATOR);
        sb.append(this.perm);
        sb.append(SEPARATOR);
        sb.append(this.topicFilterType);

        return sb.toString();
    }

    public boolean decode(final String in) {
        String[] strs = in.split(SEPARATOR);
        if (strs != null && strs.length == 5) {
            this.topicName = strs[0];

            this.readQueueNums = Integer.parseInt(strs[1]);

            this.writeQueueNums = Integer.parseInt(strs[2]);

            this.perm = Integer.parseInt(strs[3]);

            this.topicFilterType = TopicFilterType.valueOf(strs[4]);

            return true;
        }

        return false;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public int getReadQueueNums() {
        return readQueueNums;
    }

    public void setReadQueueNums(int readQueueNums) {
        this.readQueueNums = readQueueNums;
    }

    public int getWriteQueueNums() {
        return writeQueueNums;
    }

    public void setWriteQueueNums(int writeQueueNums) {
        this.writeQueueNums = writeQueueNums;
    }

    public int getPerm() {
        return perm;
    }

    public void setPerm(int perm) {
        this.perm = perm;
    }

    public TopicFilterType getTopicFilterType() {
        return topicFilterType;
    }

    public void setTopicFilterType(TopicFilterType topicFilterType) {
        this.topicFilterType = topicFilterType;
    }

    public int getTopicSysFlag() {
        return topicSysFlag;
    }

    public void setTopicSysFlag(int topicSysFlag) {
        this.topicSysFlag = topicSysFlag;
    }

    public boolean isOrder() {
        return order;
    }

    public void setOrder(boolean isOrder) {
        this.order = isOrder;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        final TopicConfig that = (TopicConfig) o;

        if (readQueueNums != that.readQueueNums)
            return false;
        if (writeQueueNums != that.writeQueueNums)
            return false;
        if (perm != that.perm)
            return false;
        if (topicSysFlag != that.topicSysFlag)
            return false;
        if (order != that.order)
            return false;
        if (topicName != null ? !topicName.equals(that.topicName) : that.topicName != null)
            return false;
        return topicFilterType == that.topicFilterType;

    }

    @Override
    public int hashCode() {
        int result = topicName != null ? topicName.hashCode() : 0;
        result = 31 * result + readQueueNums;
        result = 31 * result + writeQueueNums;
        result = 31 * result + perm;
        result = 31 * result + (topicFilterType != null ? topicFilterType.hashCode() : 0);
        result = 31 * result + topicSysFlag;
        result = 31 * result + (order ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "TopicConfig [topicName=" + topicName + ", readQueueNums=" + readQueueNums
            + ", writeQueueNums=" + writeQueueNums + ", perm=" + PermName.perm2String(perm)
            + ", topicFilterType=" + topicFilterType + ", topicSysFlag=" + topicSysFlag + ", order="
            + order + "]";
    }
}
