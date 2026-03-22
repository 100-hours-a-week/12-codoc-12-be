CREATE TABLE surprise_event (
  id BIGINT NOT NULL AUTO_INCREMENT,
  quiz_pool_id BIGINT NOT NULL,
  status VARCHAR(20) NOT NULL,
  starts_at TIMESTAMP(6) NOT NULL,
  ends_at TIMESTAMP(6) NOT NULL,
  created_at TIMESTAMP(6) NOT NULL,
  updated_at TIMESTAMP(6) NOT NULL,
  PRIMARY KEY (id),
  KEY idx_surprise_event_status_starts_at (status, starts_at),
  KEY idx_surprise_event_starts_at (starts_at),
  CONSTRAINT fk_surprise_event_quiz_pool
    FOREIGN KEY (quiz_pool_id) REFERENCES surprise_quiz_pool (id),
  CONSTRAINT chk_surprise_event_status
    CHECK (status IN ('SCHEDULED', 'OPEN', 'CLOSED', 'CANCELED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE surprise_quiz_submission (
  id BIGINT NOT NULL AUTO_INCREMENT,
  event_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  is_correct TINYINT(1) NOT NULL,
  submitted_at TIMESTAMP(6) NOT NULL,
  elapsed_millis BIGINT NOT NULL,
  rank_no INT NULL,
  earned_xp INT NULL,
  created_at TIMESTAMP(6) NOT NULL,
  updated_at TIMESTAMP(6) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_surprise_quiz_submission_event_user (event_id, user_id),
  KEY idx_surprise_quiz_submission_event_rank (event_id, rank_no),
  KEY idx_surprise_quiz_submission_event_elapsed (event_id, is_correct, elapsed_millis, submitted_at, user_id),
  CONSTRAINT fk_surprise_quiz_submission_event
    FOREIGN KEY (event_id) REFERENCES surprise_event (id),
  CONSTRAINT fk_surprise_quiz_submission_user
    FOREIGN KEY (user_id) REFERENCES user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
