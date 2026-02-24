CREATE TABLE `league` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
  `logo_url` varchar(255) DEFAULT NULL,
  `sort_order` int NOT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT 1,
  `created_at` timestamp(6) NOT NULL,
  `updated_at` timestamp(6) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `leaderboard_policy` (
  `id` int NOT NULL AUTO_INCREMENT,
  `league_id` int NOT NULL,
  `promote_top_n` int NOT NULL,
  `demote_bottom_n` int NOT NULL,
  `created_at` timestamp(6) NOT NULL,
  `updated_at` timestamp(6) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_leaderboard_policy_league_id` (`league_id`),
  CONSTRAINT `fk_leaderboard_policy_league` FOREIGN KEY (`league_id`) REFERENCES `league` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `leaderboard_score` (
  `season_id` int NOT NULL,
  `user_id` bigint NOT NULL,
  `league_id` int NOT NULL,
  `group_id` bigint DEFAULT NULL,
  `weekly_xp` int NOT NULL,
  `created_at` timestamp(6) NOT NULL,
  `updated_at` timestamp(6) NOT NULL,
  PRIMARY KEY (`season_id`, `user_id`),
  KEY `idx_leaderboard_score_season_league` (`season_id`, `league_id`),
  KEY `idx_leaderboard_score_season_group` (`season_id`, `group_id`),
  CONSTRAINT `fk_leaderboard_score_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `fk_leaderboard_score_league` FOREIGN KEY (`league_id`) REFERENCES `league` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `leaderboard_snapshot_batch` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `season_id` int NOT NULL,
  `created_at` timestamp(6) NOT NULL,
  `updated_at` timestamp(6) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_leaderboard_snapshot_batch_season` (`season_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `leaderboard_snapshot` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `snapshot_id` bigint NOT NULL,
  `season_id` int NOT NULL,
  `scope_type` varchar(10) NOT NULL,
  `scope_id` bigint DEFAULT NULL,
  `user_id` bigint NOT NULL,
  `rank` int NOT NULL,
  `weekly_xp` int NOT NULL,
  `updated_at` timestamp(6) NOT NULL,
  `created_at` timestamp(6) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_leaderboard_snapshot_scope` (`scope_type`, `scope_id`, `snapshot_id`, `rank`),
  KEY `idx_leaderboard_snapshot_season_user` (`season_id`, `user_id`),
  CONSTRAINT `fk_leaderboard_snapshot_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `fk_leaderboard_snapshot_batch` FOREIGN KEY (`snapshot_id`) REFERENCES `leaderboard_snapshot_batch` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
