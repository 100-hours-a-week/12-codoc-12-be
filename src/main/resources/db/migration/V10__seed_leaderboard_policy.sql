INSERT INTO `leaderboard_policy` (`league_id`, `promote_top_n`, `demote_bottom_n`, `created_at`, `updated_at`)
SELECT l.id, v.promote_top_n, v.demote_bottom_n, NOW(6), NOW(6)
FROM `league` l
JOIN (
  SELECT 'BRONZE' AS `name`, 20 AS promote_top_n, 0 AS demote_bottom_n
  UNION ALL SELECT 'SILVER', 15, 5
  UNION ALL SELECT 'GOLD', 10, 10
  UNION ALL SELECT 'PLATINUM', 10, 10
  UNION ALL SELECT 'DIAMOND', 0, 10
) v ON v.`name` = l.`name`
ON DUPLICATE KEY UPDATE
  `promote_top_n` = VALUES(`promote_top_n`),
  `demote_bottom_n` = VALUES(`demote_bottom_n`),
  `updated_at` = VALUES(`updated_at`);
