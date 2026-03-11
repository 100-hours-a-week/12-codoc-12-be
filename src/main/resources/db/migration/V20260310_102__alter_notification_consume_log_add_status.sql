ALTER TABLE `notification_consume_log`
  ADD COLUMN `status` varchar(20) NOT NULL DEFAULT 'PENDING' AFTER `channel`,
  ADD COLUMN `last_error` varchar(500) NULL AFTER `status`,
  ADD COLUMN `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
    ON UPDATE CURRENT_TIMESTAMP(6) AFTER `created_at`;

CREATE INDEX `idx_notification_consume_log_status` ON `notification_consume_log` (`status`);
