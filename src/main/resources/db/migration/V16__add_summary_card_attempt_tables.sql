CREATE TABLE `summary_card_attempt` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `problem_session_id` bigint NOT NULL,
  `created_at` timestamp(6) NOT NULL,
  `completed_at` timestamp(6) NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_summary_card_attempt_session` (`problem_session_id`),
  CONSTRAINT `fk_summary_card_attempt_session`
    FOREIGN KEY (`problem_session_id`) REFERENCES `problem_session` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `summary_card_submission` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `attempt_id` bigint NOT NULL,
  `summary_card_id` bigint NOT NULL,
  `is_correct` tinyint(1) NOT NULL,
  `submitted_at` timestamp(6) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_summary_card_submission_attempt` (`attempt_id`),
  KEY `idx_summary_card_submission_card` (`summary_card_id`),
  KEY `idx_summary_card_submission_attempt_time` (`attempt_id`, `submitted_at`),
  CONSTRAINT `fk_summary_card_submission_attempt`
    FOREIGN KEY (`attempt_id`) REFERENCES `summary_card_attempt` (`id`),
  CONSTRAINT `fk_summary_card_submission_summary_card`
    FOREIGN KEY (`summary_card_id`) REFERENCES `summary_card` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
