ALTER TABLE surprise_event
    ADD COLUMN max_reward_count INT NULL DEFAULT NULL,
    ADD COLUMN remaining_reward_count INT NOT NULL DEFAULT 0,
    ADD COLUMN reward_exhausted_at TIMESTAMP(6) NULL;

UPDATE surprise_event e
LEFT JOIN (
    SELECT event_id, COUNT(*) AS correct_count
    FROM surprise_quiz_submission
    WHERE is_correct = 1
    GROUP BY event_id
) s ON s.event_id = e.id
SET
    e.remaining_reward_count = GREATEST(COALESCE(e.max_reward_count, 0) - COALESCE(s.correct_count, 0), 0),
    e.status = CASE
        WHEN e.status = 'OPEN'
            AND e.max_reward_count IS NOT NULL
            AND COALESCE(s.correct_count, 0) >= e.max_reward_count THEN 'CLOSED'
        ELSE e.status
    END,
    e.reward_exhausted_at = CASE
        WHEN e.status = 'CLOSED' THEN COALESCE(e.settled_at, e.ends_at)
        WHEN e.max_reward_count IS NOT NULL
            AND COALESCE(s.correct_count, 0) >= e.max_reward_count THEN e.updated_at
        ELSE NULL
    END;
