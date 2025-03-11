package org.apache.rocketmq.example.transaction.localmessage;

/**
 * @author qiuxianbao
 * @date 2024/03/12
 */
public interface MessageService {
    void send(String topic, String tag, String key, Object obj);
    void sendDelay(String topic, String tag, String key, Object obj, Long period);
}
