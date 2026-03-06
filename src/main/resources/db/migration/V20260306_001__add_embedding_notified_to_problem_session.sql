ALTER TABLE `problem_session`
    ADD COLUMN `ai_session_notified` TINYINT(1) NOT NULL DEFAULT 0 AFTER `chatbot_completed_at`;
