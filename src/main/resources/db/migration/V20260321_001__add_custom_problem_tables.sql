CREATE TABLE `custom_problem` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `status` varchar(20) NOT NULL,
  `source_file_keys` json NOT NULL,
  `title` varchar(255) NULL,
  `content` text NULL,
  `is_deleted` tinyint(1) NOT NULL,
  `created_at` timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  `deleted_at` timestamp(6) NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_custom_problem_user_created_id` (`user_id`, `created_at` DESC, `id` DESC),
  KEY `idx_custom_problem_status_created` (`status`, `created_at` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `custom_quiz` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `custom_problem_id` bigint NOT NULL,
  `quiz_type` varchar(30) NOT NULL,
  `question` varchar(255) NOT NULL,
  `explanation` varchar(2000) NOT NULL,
  `choices` json NOT NULL,
  `answer_index` int NOT NULL,
  `sequence` int NOT NULL,
  `created_at` timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  KEY `idx_custom_quiz_problem_sequence` (`custom_problem_id`, `sequence`),
  CONSTRAINT `fk_custom_quiz_problem`
    FOREIGN KEY (`custom_problem_id`) REFERENCES `custom_problem` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `custom_summary_card` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `custom_problem_id` bigint NOT NULL,
  `paragraph_type` varchar(30) NOT NULL,
  `paragraph_order` int NOT NULL,
  `choices` json NOT NULL,
  `answer_index` int NOT NULL,
  `created_at` timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  KEY `idx_custom_summary_card_problem_order` (`custom_problem_id`, `paragraph_order`),
  CONSTRAINT `fk_custom_summary_card_problem`
    FOREIGN KEY (`custom_problem_id`) REFERENCES `custom_problem` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
