package org.apache.rocketmq.example.transaction.localmessage;

/**
 * @author qiuxianbao
 * @date 2024/03/12
 */
public interface MessageMapper {
    void save(Message message);

    void updateById(Message update);
}
