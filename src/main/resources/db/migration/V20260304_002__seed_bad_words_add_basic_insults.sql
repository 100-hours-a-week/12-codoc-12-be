-- Delta seed migration: add basic insults not present in V20260304_001

INSERT INTO bad_words (word, word_norm, source) VALUES
  ('바보', '바보', 'csv'),
  ('멍청이', '멍청이', 'csv'),
  ('멍청', '멍청', 'csv'),
  ('찌질이', '찌질이', 'csv'),
  ('한심이', '한심이', 'csv')
ON DUPLICATE KEY UPDATE
  word_norm=VALUES(word_norm),
  source=VALUES(source),
  updated_at=CURRENT_TIMESTAMP;
