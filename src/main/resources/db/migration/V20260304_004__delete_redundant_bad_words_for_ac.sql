-- Remove AC-redundant words:
-- If a shorter banned word already matches at the start or end of a longer word,
-- the longer word is redundant for Aho-Corasick based substring matching.
-- Example: keep '한심', delete '한심이', '한심이이이'.

DELETE bw
FROM bad_words bw
JOIN bad_words base
  ON CHAR_LENGTH(base.word) < CHAR_LENGTH(bw.word)
 AND (
      bw.word LIKE CONCAT(base.word, '%')
   OR bw.word LIKE CONCAT('%', base.word)
 );
