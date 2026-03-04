-- Delta seed migration: add variants for basic insults (no overlap with V20260304_001/002)

INSERT INTO bad_words (word, word_norm, source) VALUES
  ('바아보', '바아보', 'generated'),
  ('바아아보', '바아아보', 'generated'),
  ('빠보', '빠보', 'generated'),
  ('빠아보', '빠아보', 'generated'),
  ('멍충이', '멍충이', 'generated'),
  ('멍처이', '멍처이', 'generated'),
  ('머엉청이', '머엉청이', 'generated'),
  ('멍청이이', '멍청이이', 'generated'),
  ('멍청이이이', '멍청이', 'generated'),
  ('머엉청', '머엉청', 'generated'),
  ('멍청청', '멍청청', 'generated'),
  ('멍청청청', '멍청', 'generated'),
  ('찌질', '찌질', 'generated'),
  ('찌지리', '찌지리', 'generated'),
  ('찌질이이', '찌질이이', 'generated'),
  ('찌질이이이', '찌질이', 'generated'),
  ('한심', '한심', 'generated'),
  ('한시미', '한시미', 'generated'),
  ('한심이이', '한심이이', 'generated'),
  ('한심이이이', '한심이', 'generated')
ON DUPLICATE KEY UPDATE
  word_norm=VALUES(word_norm),
  source=VALUES(source),
  updated_at=CURRENT_TIMESTAMP;
