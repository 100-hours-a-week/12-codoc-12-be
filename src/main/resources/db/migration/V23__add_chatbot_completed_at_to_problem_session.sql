ALTER TABLE `problem_session`
  ADD COLUMN `chatbot_completed_at` timestamp(6) NULL DEFAULT NULL
    AFTER `chatbot_paragraph_type`;
