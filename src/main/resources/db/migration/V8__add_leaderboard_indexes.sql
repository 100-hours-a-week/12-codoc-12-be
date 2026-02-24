ALTER TABLE `leaderboard_group_member`
  ADD KEY `idx_leaderboard_group_member_season_user` (`season_id`, `user_id`);

ALTER TABLE `leaderboard_season`
  ADD KEY `idx_leaderboard_season_window` (`starts_at`, `ends_at`);
