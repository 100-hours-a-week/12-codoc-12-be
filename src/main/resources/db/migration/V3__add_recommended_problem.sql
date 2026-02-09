CREATE TABLE `recommended_problem` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `problem_id` bigint NOT NULL,
  `reason_msg` varchar(500) NOT NULL,
  `recommended_at` timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `solved_at` timestamp(6) NULL DEFAULT NULL,
  `is_done` tinyint(1) NOT NULL DEFAULT 0,
  `created_at` timestamp(6) NOT NULL,
  `updated_at` timestamp(6) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_recommended_problem_user_done` (`user_id`,`is_done`),
  KEY `idx_recommended_problem_user_solved_at` (`user_id`,`solved_at`),
  CONSTRAINT `fk_recommended_problem_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `fk_recommended_problem_problem` FOREIGN KEY (`problem_id`) REFERENCES `problem` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
