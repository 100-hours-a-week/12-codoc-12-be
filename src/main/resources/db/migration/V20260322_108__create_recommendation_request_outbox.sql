CREATE TABLE recommendation_request_outbox (
    id BIGINT NOT NULL AUTO_INCREMENT,
    job_id VARCHAR(64) NOT NULL,
    requested_at DATETIME(6) NOT NULL,
    payload JSON NOT NULL,
    status VARCHAR(20) NOT NULL,
    attempt_count INT NOT NULL DEFAULT 0,
    next_attempt_at DATETIME(6) NOT NULL,
    processing_started_at DATETIME(6) NULL,
    published_at DATETIME(6) NULL,
    last_error VARCHAR(255) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_recommendation_request_outbox_job_id (job_id),
    KEY idx_recommendation_request_outbox_publish_scan (status, next_attempt_at, id),
    KEY idx_recommendation_request_outbox_processing_started_at (processing_started_at)
);
