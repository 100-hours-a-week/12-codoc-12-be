-- 1) problem_session에 chatbot_paragraph_type 추가
ALTER TABLE `problem_session`
  ADD COLUMN `chatbot_paragraph_type` varchar(20) NULL AFTER `updated_at`;

-- 2) V21 기준 누락 가능 데이터 보강: chatbot_attempt.problem_session_id가 NULL이면 세션 생성 후 매핑
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
  ca.`user_id`,
  ca.`problem_id`,
  CASE
    WHEN ca.`expires_at` < NOW(6) THEN 'EXPIRED'
    ELSE 'ACTIVE'
  END,
  ca.`expires_at`,
  NULL,
  ca.`created_at`,
  ca.`updated_at`,
  'CHATBOT',
  ca.`id`
FROM `chatbot_attempt` ca
WHERE ca.`problem_session_id` IS NULL;

UPDATE `chatbot_attempt` ca
JOIN `problem_session` ps
  ON ps.`source_attempt_type` = 'CHATBOT'
 AND ps.`source_attempt_id` = ca.`id`
SET ca.`problem_session_id` = ps.`id`
WHERE ca.`problem_session_id` IS NULL;

ALTER TABLE `problem_session`
  DROP COLUMN `source_attempt_type`,
  DROP COLUMN `source_attempt_id`;

-- 3) chatbot_conversation에 problem_session_id 추가 및 백필
ALTER TABLE `chatbot_conversation`
  ADD COLUMN `problem_session_id` bigint NULL AFTER `attempt_id`;

UPDATE `chatbot_conversation` c
JOIN `chatbot_attempt` a ON c.`attempt_id` = a.`id`
SET c.`problem_session_id` = a.`problem_session_id`;

-- 4) problem_session의 paragraph_type 백필 (각 세션별 최신 attempt의 값)
UPDATE `problem_session` ps
JOIN (
  SELECT a.`problem_session_id`, a.`paragraph_type`
  FROM `chatbot_attempt` a
  JOIN (
    SELECT `problem_session_id`, MAX(`id`) AS `max_id`
    FROM `chatbot_attempt`
    WHERE `problem_session_id` IS NOT NULL
    GROUP BY `problem_session_id`
  ) latest
    ON latest.`problem_session_id` = a.`problem_session_id`
   AND latest.`max_id` = a.`id`
) latest_attempt ON latest_attempt.`problem_session_id` = ps.`id`
SET ps.`chatbot_paragraph_type` = latest_attempt.`paragraph_type`;

-- 5) 챗봇 이력이 없는 세션은 초기 paragraph_type으로 보정
UPDATE `problem_session`
SET `chatbot_paragraph_type` = 'BACKGROUND'
WHERE `chatbot_paragraph_type` IS NULL;

-- 6) 신규 제약/인덱스 적용
ALTER TABLE `chatbot_conversation`
  MODIFY COLUMN `problem_session_id` bigint NOT NULL,
  ADD KEY `idx_chatbot_conversation_session_time_correct_type`
    (`problem_session_id`, `created_at`, `is_correct`, `paragraph_type`),
  ADD CONSTRAINT `fk_chatbot_conversation_session`
    FOREIGN KEY (`problem_session_id`) REFERENCES `problem_session` (`id`);

ALTER TABLE `problem_session`
  MODIFY COLUMN `chatbot_paragraph_type` varchar(20) NOT NULL;

-- 7) attempt_id 의존성 제거
ALTER TABLE `chatbot_conversation`
  DROP FOREIGN KEY `fk_chatbot_conversation_attempt`;

ALTER TABLE `chatbot_conversation`
  DROP INDEX `idx_chatbot_conversation_attempt_time_correct_type`,
  DROP COLUMN `attempt_id`;

-- 8) chatbot_attempt 테이블 삭제
DROP TABLE `chatbot_attempt`;
