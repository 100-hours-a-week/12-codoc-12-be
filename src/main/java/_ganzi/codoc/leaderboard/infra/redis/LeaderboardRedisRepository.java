package _ganzi.codoc.leaderboard.infra.redis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class LeaderboardRedisRepository {

    private final StringRedisTemplate redisTemplate;

    public void upsertScore(String key, Long userId, double score) {
        redisTemplate.opsForZSet().add(key, String.valueOf(userId), score);
    }

    public Long reverseRank(String key, Long userId) {
        return redisTemplate.opsForZSet().reverseRank(key, String.valueOf(userId));
    }

    public Double score(String key, Long userId) {
        return redisTemplate.opsForZSet().score(key, String.valueOf(userId));
    }

    public List<ZSetOperations.TypedTuple<String>> top(String key, long start, long end) {
        Set<ZSetOperations.TypedTuple<String>> tuples =
                redisTemplate.opsForZSet().reverseRangeWithScores(key, start, end);
        if (tuples == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(tuples);
    }
}
