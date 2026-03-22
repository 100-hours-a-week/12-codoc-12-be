CREATE TABLE surprise_quiz_pool (
  id BIGINT NOT NULL AUTO_INCREMENT,
  status VARCHAR(20) NOT NULL DEFAULT 'UNUSED',
  content JSON NOT NULL,
  created_at TIMESTAMP(6) NOT NULL,
  updated_at TIMESTAMP(6) NOT NULL,
  PRIMARY KEY (id),
  KEY idx_surprise_quiz_pool_status (status),
  CONSTRAINT chk_surprise_quiz_pool_status
    CHECK (status IN ('UNUSED', 'IN_PROGRESS', 'USED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
