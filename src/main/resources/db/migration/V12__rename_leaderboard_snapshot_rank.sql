ALTER TABLE `leaderboard_snapshot`
  CHANGE COLUMN `rank` `rank_no` int NOT NULL;

ALTER TABLE `leaderboard_snapshot`
  DROP INDEX `idx_leaderboard_snapshot_scope`,
  ADD KEY `idx_leaderboard_snapshot_scope` (`scope_type`, `scope_id`, `snapshot_id`, `rank_no`);
