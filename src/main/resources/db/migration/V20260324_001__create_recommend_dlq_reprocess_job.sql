CREATE TABLE recommend_dlq_reprocess_job (
    id BIGINT NOT NULL AUTO_INCREMENT,
    request_type VARCHAR(20) NOT NULL,
    target_job_id VARCHAR(64) NULL,
    status VARCHAR(20) NOT NULL,
    requested_limit INT NOT NULL,
    success_count INT NOT NULL DEFAULT 0,
    failed_count INT NOT NULL DEFAULT 0,
    skipped_count INT NOT NULL DEFAULT 0,
    failed_job_ids_json JSON NULL,
    last_error VARCHAR(255) NULL,
    started_at DATETIME(6) NULL,
    finished_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_recommend_dlq_reprocess_job_status_created_at (status, created_at),
    KEY idx_recommend_dlq_reprocess_job_target_job_id (target_job_id)
);
