CREATE TABLE surprise_event_reward_log (
  id BIGINT NOT NULL AUTO_INCREMENT,
  event_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  reward_type VARCHAR(20) NOT NULL,
  reward_amount INT NOT NULL,
  idempotency_key VARCHAR(120) NOT NULL,
  created_at TIMESTAMP(6) NOT NULL,
  updated_at TIMESTAMP(6) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_surprise_event_reward_log_idempotency (idempotency_key),
  KEY idx_surprise_event_reward_log_event_user (event_id, user_id),
  CONSTRAINT fk_surprise_event_reward_log_event
    FOREIGN KEY (event_id) REFERENCES surprise_event (id),
  CONSTRAINT fk_surprise_event_reward_log_user
    FOREIGN KEY (user_id) REFERENCES user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
