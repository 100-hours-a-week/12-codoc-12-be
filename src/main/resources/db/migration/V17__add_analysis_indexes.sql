ALTER TABLE `chatbot_conversation`
  DROP INDEX `idx_chatbot_conversation_attempt_id`,
  ADD KEY `idx_chatbot_conversation_attempt_time_correct_type`
    (`attempt_id`, `created_at`, `is_correct`, `paragraph_type`);

ALTER TABLE `user_quiz_result`
  DROP INDEX `idx_user_quiz_result_attempt_id`,
  ADD KEY `idx_user_quiz_result_attempt_time_correct`
    (`attempt_id`, `created_at`, `is_correct`);

ALTER TABLE `user_quest`
  DROP INDEX `idx_user_quest_user_status`,
  ADD KEY `idx_user_quest_user_status_updated` (`user_id`, `status`, `updated_at`);
