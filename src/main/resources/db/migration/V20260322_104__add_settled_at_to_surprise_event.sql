ALTER TABLE surprise_event
  ADD COLUMN settled_at TIMESTAMP(6) NULL AFTER ends_at,
  ADD KEY idx_surprise_event_status_ends_at_settled_at (status, ends_at, settled_at);
