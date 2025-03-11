CREATE TABLE `message` (
   `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
   `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
   `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
   `status_delete` tinyint NOT NULL DEFAULT '0' COMMENT '删除标记 0正常 1删除',
   `topic` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'topic',
   `tag` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'tag',
   `msg_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '消息id',
   `msg_key` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '消息key',
   `data` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '消息json串',
   `try_num` int NOT NULL DEFAULT '0' COMMENT '重试次数',
   `status` tinyint NOT NULL DEFAULT '0' COMMENT '发送状态 0-未发送 1-已发送',
   `next_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下次驱动开始时间'
   PRIMARY KEY (`id`),
   KEY `idx_key` (`msg_key`),
   KEY `idx_nexttime_status` (`next_time`,`status`),
   KEY `idx_msgid` (`msg_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='本地消息记录表';
