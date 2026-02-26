ALTER TABLE `problem_session`
  ADD COLUMN `source_attempt_type` varchar(10) NULL DEFAULT NULL,
  ADD COLUMN `source_attempt_id` bigint NULL DEFAULT NULL;

INSERT INTO `problem_session` (
  `user_id`,
  `problem_id`,
  `status`,
  `expires_at`,
  `closed_at`,
  `created_at`,
  `updated_at`,
  `source_attempt_type`,
  `source_attempt_id`
)
SELECT
  qa.user_id,
  qa.problem_id,
  CASE
    WHEN qa.completed_at IS NOT NULL THEN 'CLOSED'
    WHEN qa.created_at + INTERVAL 30 MINUTE < NOW(6) THEN 'EXPIRED'
    ELSE 'ACTIVE'
  END,
  qa.created_at + INTERVAL 30 MINUTE,
  qa.completed_at,
  qa.created_at,
  COALESCE(qa.completed_at, qa.created_at),
  'QUIZ',
  qa.id
FROM user_quiz_attempt qa
WHERE qa.problem_session_id IS NULL;

UPDATE user_quiz_attempt qa
JOIN problem_session ps
  ON ps.source_attempt_type = 'QUIZ'
 AND ps.source_attempt_id = qa.id
SET qa.problem_session_id = ps.id
WHERE qa.problem_session_id IS NULL;

INSERT INTO `problem_session` (
  `user_id`,
  `problem_id`,
  `status`,
  `expires_at`,
  `closed_at`,
  `created_at`,
  `updated_at`,
  `source_attempt_type`,
  `source_attempt_id`
)
SELECT
  ca.user_id,
  ca.problem_id,
  CASE
    WHEN ca.expires_at < NOW(6) THEN 'EXPIRED'
    ELSE 'ACTIVE'
  END,
  ca.expires_at,
  NULL,
  ca.created_at,
  ca.created_at,
  'CHATBOT',
  ca.id
FROM chatbot_attempt ca
WHERE ca.problem_session_id IS NULL;

UPDATE chatbot_attempt ca
JOIN problem_session ps
  ON ps.source_attempt_type = 'CHATBOT'
 AND ps.source_attempt_id = ca.id
SET ca.problem_session_id = ps.id
WHERE ca.problem_session_id IS NULL;

ALTER TABLE `problem_session`
  DROP COLUMN `source_attempt_type`,
  DROP COLUMN `source_attempt_id`;
