-- Core seed data: avatars + daily quests
-- Keep statements idempotent (insert only if missing).

-- Avatars
INSERT INTO avatar (created_at, updated_at, name, image_url, is_default)
SELECT NOW(6), NOW(6), 'profile1', 'https://codoc.cloud/images/profile1.png', b'0'
WHERE NOT EXISTS (SELECT 1 FROM avatar WHERE name = 'profile1');

INSERT INTO avatar (created_at, updated_at, name, image_url, is_default)
SELECT NOW(6), NOW(6), 'profile2', 'https://codoc.cloud/images/profile2.png', b'0'
WHERE NOT EXISTS (SELECT 1 FROM avatar WHERE name = 'profile2');

INSERT INTO avatar (created_at, updated_at, name, image_url, is_default)
SELECT NOW(6), NOW(6), 'profile3', 'https://codoc.cloud/images/profile3.png', b'0'
WHERE NOT EXISTS (SELECT 1 FROM avatar WHERE name = 'profile3');

INSERT INTO avatar (created_at, updated_at, name, image_url, is_default)
SELECT NOW(6), NOW(6), 'profile4', 'https://codoc.cloud/images/profile4.png', b'0'
WHERE NOT EXISTS (SELECT 1 FROM avatar WHERE name = 'profile4');

INSERT INTO avatar (created_at, updated_at, name, image_url, is_default)
SELECT NOW(6), NOW(6), 'profile5', 'https://codoc.cloud/images/profile5.png', b'0'
WHERE NOT EXISTS (SELECT 1 FROM avatar WHERE name = 'profile5');

INSERT INTO avatar (created_at, updated_at, name, image_url, is_default)
SELECT NOW(6), NOW(6), 'profile6', 'https://codoc.cloud/images/profile6.png', b'1'
WHERE NOT EXISTS (SELECT 1 FROM avatar WHERE name = 'profile6');

INSERT INTO avatar (created_at, updated_at, name, image_url, is_default)
SELECT NOW(6), NOW(6), 'profile7', 'https://codoc.cloud/images/profile7.png', b'0'
WHERE NOT EXISTS (SELECT 1 FROM avatar WHERE name = 'profile7');

INSERT INTO avatar (created_at, updated_at, name, image_url, is_default)
SELECT NOW(6), NOW(6), 'profile8', 'https://codoc.cloud/images/profile8.png', b'0'
WHERE NOT EXISTS (SELECT 1 FROM avatar WHERE name = 'profile8');

INSERT INTO avatar (created_at, updated_at, name, image_url, is_default)
SELECT NOW(6), NOW(6), 'profile9', 'https://codoc.cloud/images/profile9.png', b'0'
WHERE NOT EXISTS (SELECT 1 FROM avatar WHERE name = 'profile9');

-- Daily quests
INSERT INTO quest (created_at, updated_at, duration, requirements, reward, title, type, issue_conditions)
SELECT NOW(6), NOW(6), 1, JSON_OBJECT('DailySolvedCount', 1), 10,
       '오늘의 첫 문제 해결', 'DAILY', NULL
WHERE NOT EXISTS (SELECT 1 FROM quest WHERE title = '오늘의 첫 문제 해결');

INSERT INTO quest (created_at, updated_at, duration, requirements, reward, title, type, issue_conditions)
SELECT NOW(6), NOW(6), 1, JSON_OBJECT('DailySolvedCount', 1), 10,
       '일일 문제 해결 목표 달성 (1 문제)', 'DAILY', JSON_OBJECT('DailyGoal', 'ONE')
WHERE NOT EXISTS (SELECT 1 FROM quest WHERE title = '일일 문제 해결 목표 달성 (1 문제)');

INSERT INTO quest (created_at, updated_at, duration, requirements, reward, title, type, issue_conditions)
SELECT NOW(6), NOW(6), 1, JSON_OBJECT('DailySolvedCount', 3), 10,
       '일일 문제 해결 목표 달성 (3 문제)', 'DAILY', JSON_OBJECT('DailyGoal', 'THREE')
WHERE NOT EXISTS (SELECT 1 FROM quest WHERE title = '일일 문제 해결 목표 달성 (3 문제)');

INSERT INTO quest (created_at, updated_at, duration, requirements, reward, title, type, issue_conditions)
SELECT NOW(6), NOW(6), 1, JSON_OBJECT('DailySolvedCount', 5), 10,
       '일일 문제 해결 목표 달성 (5 문제)', 'DAILY', JSON_OBJECT('DailyGoal', 'FIVE')
WHERE NOT EXISTS (SELECT 1 FROM quest WHERE title = '일일 문제 해결 목표 달성 (5 문제)');
