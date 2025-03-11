package org.apache.rocketmq.example.transaction.localmessage;//package cn.thinkinjava.main.transaction.localmessage;
//
//import com.alibaba.fastjson.JSON;
//import org.apache.rocketmq.client.producer.DefaultMQProducer;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.transaction.support.TransactionSynchronization;
//import org.springframework.transaction.support.TransactionSynchronizationManager;
//
//import javax.annotation.Resource;
//import java.util.Date;
//
///**
// * @author qiuxianbao
// * @date 2024/03/12
// */
//@Service
//public class MessageServiceImpl implements MessageService {
//    @Resource
//    private DefaultMQProducer producer;
//    @Resource
//    private MessageMapper messageMapper;
//
//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public void send(String topic, String tag, String key, Object obj) {
//        sendDelay(topic, tag, key, obj, 0L);
//    }
//
//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public void sendDelay(String topic, String tag, String key, Object obj, Long period) {
//        //计算时间，防止定时任务扫描将还在正常流程中的消息进行重试
//        int time = (period == 0L ? 10 : period.intValue() / 1000);
////        Date nextTime = DateUtil.getAfterNewDateSecond(new Date(), time);
//        Date nextTime = new Date();
//        String data = JSON.toJSONString(obj);
//        Message message = new Message()
//                .setStatusDelete(0)
//                .setTopic(topic)
//                .setTag(tag)
//                .setMsgId("")
//                .setMsgKey(key)
//                .setData(data)
//                .setTryNum(0)
//                .setStatus(0)
//                .setNextTime(nextTime);
//        // 保存本地消息记录
//        messageMapper.save(message);
//
//        // 事务同步
//        // 当前事务提交后，再执行发送消息和更改本地消息记录状态
//        TransactionSynchronizationManager.registerSynchronization(
//                new TransactionSynchronization() {
//                    @Override
//                    public void afterCommit() {
//                        String messageId = "";
//                        try {
//                            if (period == 0L) {
////                                messageId = producer.send(topic, tag, key, data);
//                            } else {
////                                messageId = producer.sendDelay(topic, tag, key, data, period);
//                            }
//                            Message update = new Message()
//                                    .setId(message.getId())
//                                    .setMsgId(messageId)
//                                    .setStatus(1);
//                            messageMapper.updateById(update);
//                        } catch (Exception e) {
////                            log.error("..");
//                        }
//                    }
//                }
//        );
//    }
//}
