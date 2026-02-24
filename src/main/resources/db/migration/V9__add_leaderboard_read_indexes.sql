ALTER TABLE `leaderboard_snapshot_batch`
  ADD KEY `idx_leaderboard_snapshot_batch_season_id` (`season_id`, `id`);

ALTER TABLE `leaderboard_snapshot`
  ADD KEY `idx_leaderboard_snapshot_batch_scope_user` (
    `snapshot_id`,
    `scope_type`,
    `scope_id`,
    `user_id`
  );

ALTER TABLE `leaderboard_season`
  ADD KEY `idx_leaderboard_season_ends_at` (`ends_at`);
