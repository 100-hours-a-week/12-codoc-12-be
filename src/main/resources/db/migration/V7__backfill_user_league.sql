UPDATE `user` u
JOIN `league` l ON l.name = 'BRONZE'
SET u.league_id = l.id
WHERE u.league_id IS NULL;
