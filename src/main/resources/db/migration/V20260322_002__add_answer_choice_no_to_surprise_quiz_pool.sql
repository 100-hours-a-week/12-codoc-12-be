ALTER TABLE surprise_quiz_pool
  ADD COLUMN answer_choice_no TINYINT NOT NULL DEFAULT 1 AFTER content,
  ADD CONSTRAINT chk_surprise_quiz_pool_answer_choice_no
    CHECK (answer_choice_no BETWEEN 1 AND 4);
