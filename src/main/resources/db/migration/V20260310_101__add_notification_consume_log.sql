CREATE TABLE `notification_consume_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `message_id` varchar(64) NOT NULL,
  `channel` varchar(20) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_notification_consume_log_message_channel` (`message_id`, `channel`),
  KEY `idx_notification_consume_log_created_at` (`created_at`)
);
