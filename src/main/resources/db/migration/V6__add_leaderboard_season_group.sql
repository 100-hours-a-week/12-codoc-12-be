CREATE TABLE `leaderboard_season` (
  `season_id` int NOT NULL,
  `starts_at` timestamp(6) NOT NULL,
  `ends_at` timestamp(6) NOT NULL,
  `created_at` timestamp(6) NOT NULL,
  `updated_at` timestamp(6) NOT NULL,
  PRIMARY KEY (`season_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `leaderboard_group` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `season_id` int NOT NULL,
  `league_id` int NOT NULL,
  `group_no` int NOT NULL,
  `created_at` timestamp(6) NOT NULL,
  `updated_at` timestamp(6) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_leaderboard_group_season_league_no` (`season_id`, `league_id`, `group_no`),
  CONSTRAINT `fk_leaderboard_group_season` FOREIGN KEY (`season_id`) REFERENCES `leaderboard_season` (`season_id`),
  CONSTRAINT `fk_leaderboard_group_league` FOREIGN KEY (`league_id`) REFERENCES `league` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `leaderboard_group_member` (
  `group_id` bigint NOT NULL,
  `season_id` int NOT NULL,
  `user_id` bigint NOT NULL,
  `created_at` timestamp(6) NOT NULL,
  `updated_at` timestamp(6) NOT NULL,
  PRIMARY KEY (`group_id`, `user_id`),
  KEY `idx_leaderboard_group_member_season` (`season_id`),
  KEY `idx_leaderboard_group_member_user` (`user_id`),
  CONSTRAINT `fk_leaderboard_group_member_group` FOREIGN KEY (`group_id`) REFERENCES `leaderboard_group` (`id`),
  CONSTRAINT `fk_leaderboard_group_member_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

ALTER TABLE `user`
  ADD COLUMN `league_id` int DEFAULT NULL,
  ADD KEY `idx_user_league_id` (`league_id`),
  ADD CONSTRAINT `fk_user_league` FOREIGN KEY (`league_id`) REFERENCES `league` (`id`);
