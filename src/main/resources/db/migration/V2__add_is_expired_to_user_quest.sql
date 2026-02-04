ALTER TABLE user_quest
  ADD COLUMN is_expired TINYINT(1) NOT NULL DEFAULT 0,
  ADD INDEX idx_user_quest_user_expired (user_id, is_expired);

UPDATE user_quest
SET is_expired = 1
WHERE status = 'EXPIRED';

UPDATE user_quest
SET status = 'IN_PROGRESS'
WHERE status = 'EXPIRED';
