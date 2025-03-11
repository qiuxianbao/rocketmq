package org.apache.rocketmq.example.transaction.mq;//package cn.thinkinjava.main.transaction.mq;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.TransactionStatus;
//import org.springframework.transaction.support.TransactionTemplate;
//
///**
// * 订单发送事务消息
// *
// * @author qiuxianbao
// * @date 2024/07/02
// * @since acp_2.5.0_20240606
// */
//public class OrderService {
//
//    public void sendOrderMessage() {
//        try {
//            transactionTemplate.start();
//            messageTransactionProducer.send(buildOrderCreatedMsg(order), new TransactionExecutor(transactionTemplate){
//                @override
//                public TransactionStatus execute(Message msg) {
//                    orderManager.createOrder(order);
//                    return TransactionStatus.CommitTransaction;
//                }
//            });
//        } catch (Exception e) {
//            transactionTemplate.rollback();
//        }
//    }
//
//    // 订单创建成功消息的半消息回查实现
//    public class OrderTransactionCheckerImpl implements TransactionChecker {
//
//        @override
//        public TransactionStatus check(Message msg) {
//            if (orderManager.isOrderExist(getOrderId(msg))) {
//                return TransactionStatus.CommitTransaction;
//            } else {
//                // TODO-QIU: 2024年7月2日, 0002 此处添加超时
//                // 如果消息发送时间距当前时间20s以上，查询不到订单则认为是真的查询不到，否则有可能是创单的事务还未提交
//                // 20s的时间是来源于DB事务默认的超时时间设置15s加上5s的Buffer，不同团队会不同，这个值不能照搬
//                if (new Date().getTime() - msg.getSendTime() > 20000) {
//                    return TransactionStatus.RollbackTransaction;
//                } else {
//                    return TransactionStatus.Unknown;
//                }
//            }
//        }
//    }
//
//}
