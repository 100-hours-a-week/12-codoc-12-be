ALTER TABLE `chatbot_attempt`
  ADD COLUMN `problem_session_id` bigint NULL DEFAULT NULL,
  ADD KEY `idx_chatbot_attempt_session_id` (`problem_session_id`),
  ADD CONSTRAINT `fk_chatbot_attempt_problem_session`
    FOREIGN KEY (`problem_session_id`) REFERENCES `problem_session` (`id`);

ALTER TABLE `user_quiz_attempt`
  ADD COLUMN `problem_session_id` bigint NULL DEFAULT NULL,
  ADD KEY `idx_user_quiz_attempt_session_id` (`problem_session_id`),
  ADD CONSTRAINT `fk_user_quiz_attempt_problem_session`
    FOREIGN KEY (`problem_session_id`) REFERENCES `problem_session` (`id`);
