ALTER TABLE `user_problem_result`
  ADD KEY `idx_user_problem_result_user_status_updated`
    (`user_id`, `status`, `updated_at`);

ALTER TABLE `problem_session`
  ADD KEY `idx_problem_session_user_created` (`user_id`, `created_at`),
  ADD KEY `idx_problem_session_user_closed` (`user_id`, `closed_at`),
  ADD KEY `idx_problem_session_user_expires` (`user_id`, `expires_at`);
