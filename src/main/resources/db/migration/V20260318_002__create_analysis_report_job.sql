CREATE TABLE analysis_report_job (
    job_id VARCHAR(64) NOT NULL,
    user_id BIGINT NOT NULL,
    status VARCHAR(32) NOT NULL,
    error_code VARCHAR(64) NULL,
    error_message VARCHAR(500) NULL,
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    requested_at DATETIME(6) NOT NULL,
    responded_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (job_id),
    KEY idx_analysis_report_job_user_created (user_id, created_at DESC),
    KEY idx_analysis_report_job_status_created (status, created_at),
    KEY idx_analysis_report_job_window_user (period_start, period_end, user_id)
);
