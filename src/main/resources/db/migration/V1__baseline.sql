-- Baseline schema (server-aligned)
SET FOREIGN_KEY_CHECKS=0;

CREATE TABLE `avatar` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(10) NOT NULL,
  `image_url` varchar(500) NOT NULL,
  `is_default` tinyint(1) NOT NULL,
  `created_at` timestamp(6) NOT NULL,
  `updated_at` timestamp(6) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `avatar_id` int NOT NULL,
  `nickname` varchar(15) NOT NULL,
  `init_level` varchar(50) DEFAULT NULL,
  `daily_goal` varchar(50) DEFAULT NULL,
  `status` varchar(50) NOT NULL,
  `last_access` timestamp(6) NOT NULL,
  `created_at` timestamp(6) NOT NULL,
  `updated_at` timestamp(6) NOT NULL,
  `deleted_at` timestamp(6) NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `nickname` (`nickname`),
  KEY `idx_user_avatar_id` (`avatar_id`),
  CONSTRAINT `fk_user_avatar` FOREIGN KEY (`avatar_id`) REFERENCES `avatar` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `problem` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(255) NOT NULL,
  `content` text NOT NULL,
  `difficulty` varchar(20) NOT NULL,
  `tags` json NOT NULL,
  `created_at` timestamp(6) NOT NULL,
  `updated_at` timestamp(6) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `quest` (
  `id` int NOT NULL AUTO_INCREMENT,
  `title` varchar(255) NOT NULL,
  `requirements` json NOT NULL,
  `issue_conditions` json DEFAULT NULL,
  `reward` int NOT NULL,
  `type` varchar(50) NOT NULL,
  `duration` int NOT NULL,
  `created_at` timestamp(6) NOT NULL,
  `updated_at` timestamp(6) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_quest_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `quiz` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `problem_id` bigint NOT NULL,
  `quiz_type` varchar(255) NOT NULL,
  `question` varchar(255) NOT NULL,
  `explanation` varchar(500) NOT NULL,
  `choices` json NOT NULL,
  `answer_index` int NOT NULL,
  `sequence` int NOT NULL,
  `created_at` timestamp(6) NOT NULL,
  `updated_at` timestamp(6) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_quiz_problem_id` (`problem_id`),
  CONSTRAINT `fk_quiz_problem` FOREIGN KEY (`problem_id`) REFERENCES `problem` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `summary_card` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `problem_id` bigint NOT NULL,
  `paragraph_type` varchar(255) NOT NULL,
  `paragraph_order` int NOT NULL,
  `choices` json NOT NULL,
  `answer_index` int NOT NULL,
  `created_at` timestamp(6) NOT NULL,
  `updated_at` timestamp(6) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_summary_card_problem_id` (`problem_id`),
  CONSTRAINT `fk_summary_card_problem` FOREIGN KEY (`problem_id`) REFERENCES `problem` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `chatbot_attempt` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `problem_id` bigint NOT NULL,
  `paragraph_type` varchar(20) NOT NULL,
  `status` varchar(20) NOT NULL,
  `expires_at` timestamp(6) NOT NULL,
  `created_at` timestamp(6) NOT NULL,
  `updated_at` timestamp(6) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_chatbot_attempt_user_id` (`user_id`),
  KEY `idx_chatbot_attempt_problem_id` (`problem_id`),
  CONSTRAINT `fk_chatbot_attempt_problem` FOREIGN KEY (`problem_id`) REFERENCES `problem` (`id`),
  CONSTRAINT `fk_chatbot_attempt_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `chatbot_conversation` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `attempt_id` bigint NOT NULL,
  `user_message` varchar(500) NOT NULL,
  `ai_message` text,
  `paragraph_type` varchar(20) NOT NULL,
  `is_correct` tinyint(1) NOT NULL,
  `created_at` timestamp(6) NOT NULL,
  `updated_at` timestamp(6) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_chatbot_conversation_attempt_id` (`attempt_id`),
  CONSTRAINT `fk_chatbot_conversation_attempt` FOREIGN KEY (`attempt_id`) REFERENCES `chatbot_attempt` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `bookmark` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `problem_id` bigint NOT NULL,
  `created_at` timestamp(6) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_bookmark_user_problem` (`user_id`,`problem_id`),
  KEY `idx_bookmark_user_id` (`user_id`),
  KEY `idx_bookmark_problem_id` (`problem_id`),
  CONSTRAINT `fk_bookmark_problem` FOREIGN KEY (`problem_id`) REFERENCES `problem` (`id`),
  CONSTRAINT `fk_bookmark_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `daily_solved_count` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `solved_count` int NOT NULL,
  `date` date NOT NULL,
  `created_at` timestamp(6) NOT NULL,
  `updated_at` timestamp(6) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_daily_solved_count_user_date` (`user_id`,`date`),
  KEY `idx_daily_solved_count_user_id` (`user_id`),
  CONSTRAINT `fk_daily_solved_count_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `refresh_token` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `token_value` varchar(255) NOT NULL,
  `expires_at` timestamp(6) NOT NULL,
  `created_at` timestamp(6) NOT NULL,
  `updated_at` timestamp(6) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_refresh_token_value` (`token_value`),
  KEY `idx_refresh_token_user_id` (`user_id`),
  CONSTRAINT `fk_refresh_token_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `social_login` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `provider_name` varchar(20) NOT NULL,
  `provider_user_id` varchar(255) NOT NULL,
  `is_deleted` tinyint(1) NOT NULL,
  `deleted_at` timestamp(6) NULL DEFAULT NULL,
  `created_at` timestamp(6) NOT NULL,
  `updated_at` timestamp(6) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_social_login_provider` (`provider_name`,`provider_user_id`),
  KEY `idx_social_login_user_id` (`user_id`),
  CONSTRAINT `fk_social_login_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `user_problem_result` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `problem_id` bigint NOT NULL,
  `status` varchar(50) NOT NULL,
  `created_at` timestamp(6) NOT NULL,
  `updated_at` timestamp(6) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_problem_result_user_problem` (`user_id`,`problem_id`),
  KEY `idx_user_problem_result_user_id` (`user_id`),
  KEY `idx_user_problem_result_problem_id` (`problem_id`),
  CONSTRAINT `fk_user_problem_result_problem` FOREIGN KEY (`problem_id`) REFERENCES `problem` (`id`),
  CONSTRAINT `fk_user_problem_result_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `user_quest` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `quest_id` int NOT NULL,
  `status` varchar(50) NOT NULL,
  `expires_at` timestamp(6) NOT NULL,
  `issued_date` date NOT NULL,
  `created_at` timestamp(6) NOT NULL,
  `updated_at` timestamp(6) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_quest_issue` (`user_id`,`quest_id`,`issued_date`),
  KEY `idx_user_quest_user_status` (`user_id`,`status`),
  KEY `idx_user_quest_quest_id` (`quest_id`),
  CONSTRAINT `fk_user_quest_quest` FOREIGN KEY (`quest_id`) REFERENCES `quest` (`id`),
  CONSTRAINT `fk_user_quest_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `user_quiz_attempt` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `problem_id` bigint NOT NULL,
  `status` varchar(50) NOT NULL,
  `completed_at` timestamp(6) NULL DEFAULT NULL,
  `abandoned_at` timestamp(6) NULL DEFAULT NULL,
  `created_at` timestamp(6) NOT NULL,
  `updated_at` timestamp(6) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_user_quiz_attempt_user_id` (`user_id`),
  KEY `idx_user_quiz_attempt_problem_id` (`problem_id`),
  CONSTRAINT `fk_user_quiz_attempt_problem` FOREIGN KEY (`problem_id`) REFERENCES `problem` (`id`),
  CONSTRAINT `fk_user_quiz_attempt_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `user_quiz_result` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `attempt_id` bigint NOT NULL,
  `quiz_id` bigint NOT NULL,
  `idempotency_key` varchar(128) NOT NULL,
  `is_correct` tinyint(1) NOT NULL,
  `created_at` timestamp(6) NOT NULL,
  `updated_at` timestamp(6) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_quiz_result_attempt_quiz` (`attempt_id`,`quiz_id`),
  KEY `idx_user_quiz_result_attempt_id` (`attempt_id`),
  KEY `idx_user_quiz_result_quiz_id` (`quiz_id`),
  CONSTRAINT `fk_user_quiz_result_attempt` FOREIGN KEY (`attempt_id`) REFERENCES `user_quiz_attempt` (`id`),
  CONSTRAINT `fk_user_quiz_result_quiz` FOREIGN KEY (`quiz_id`) REFERENCES `quiz` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `user_stats` (
  `user_id` bigint NOT NULL,
  `xp` int NOT NULL,
  `solved_cnt` int NOT NULL,
  `solving_cnt` int NOT NULL,
  `streak` int NOT NULL,
  `deleted_at` timestamp(6) NULL DEFAULT NULL,
  `created_at` timestamp(6) NOT NULL,
  `updated_at` timestamp(6) NOT NULL,
  PRIMARY KEY (`user_id`),
  CONSTRAINT `fk_user_stats_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

SET FOREIGN_KEY_CHECKS=1;
