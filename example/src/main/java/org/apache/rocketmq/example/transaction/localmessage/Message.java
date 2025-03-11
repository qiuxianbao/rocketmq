package org.apache.rocketmq.example.transaction.localmessage;

import java.util.Date;

/**
 * @author qiuxianbao
 * @date 2024/03/12
 */
// @Accessors(chain = true)
// @Data
public class Message {
    private Long id;
    private Date createTime;
    private Date updateTime;
    private Integer statusDelete;
    private String topic;
    private String tag;
    private String msgId;
    private String msgKey;
    private String data;
    private Integer tryNum;
    private Integer status;
    private Date nextTime;
}
