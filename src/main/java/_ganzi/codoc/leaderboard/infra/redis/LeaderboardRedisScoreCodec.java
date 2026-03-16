package _ganzi.codoc.leaderboard.infra.redis;

import org.springframework.stereotype.Component;

@Component
public class LeaderboardRedisScoreCodec {

    private static final long USER_ID_BUCKET = 1_000_000_000L;
    private static final double FRACTION_BASE = 1_000_000_000_000D;

    public double encode(int weeklyXp, Long userId) {
        long tieBreaker = USER_ID_BUCKET - normalizeUserId(userId);
        return weeklyXp + (tieBreaker / FRACTION_BASE);
    }

    public int decodeWeeklyXp(double encodedScore) {
        return (int) Math.floor(encodedScore);
    }

    private long normalizeUserId(Long userId) {
        if (userId == null) {
            return 0L;
        }
        long value = userId % USER_ID_BUCKET;
        return value < 0 ? value + USER_ID_BUCKET : value;
    }
}
