CREATE TABLE recommendation_job (
    job_id VARCHAR(64) NOT NULL,
    user_id BIGINT NOT NULL,
    scenario VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,
    error_code VARCHAR(64) NULL,
    error_message VARCHAR(500) NULL,
    requested_at DATETIME(6) NOT NULL,
    responded_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (job_id),
    KEY idx_recommendation_job_user_created (user_id, created_at DESC),
    KEY idx_recommendation_job_status_created (status, created_at)
);
