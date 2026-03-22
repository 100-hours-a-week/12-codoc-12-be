ALTER TABLE surprise_event
  ADD COLUMN event_week_key VARCHAR(8) NULL AFTER quiz_pool_id;

UPDATE surprise_event
SET event_week_key = DATE_FORMAT(CONVERT_TZ(starts_at, '+00:00', '+09:00'), '%Y%m%d')
WHERE event_week_key IS NULL;

ALTER TABLE surprise_event
  ADD UNIQUE KEY uk_surprise_event_week_key (event_week_key);
