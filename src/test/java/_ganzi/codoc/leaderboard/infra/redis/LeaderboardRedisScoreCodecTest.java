package _ganzi.codoc.leaderboard.infra.redis;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class LeaderboardRedisScoreCodecTest {

    private final LeaderboardRedisScoreCodec codec = new LeaderboardRedisScoreCodec();

    @Test
    void should_decode_weekly_xp_from_encoded_score() {
        double encoded = codec.encode(1234, 42L);

        assertThat(codec.decodeWeeklyXp(encoded)).isEqualTo(1234);
    }

    @Test
    void should_rank_smaller_user_id_higher_when_weekly_xp_is_same() {
        double scoreUser1 = codec.encode(1000, 1L);
        double scoreUser2 = codec.encode(1000, 2L);

        assertThat(scoreUser1).isGreaterThan(scoreUser2);
    }

    @Test
    void should_rank_higher_weekly_xp_higher() {
        double scoreLowXp = codec.encode(999, 1L);
        double scoreHighXp = codec.encode(1000, 999999999L);

        assertThat(scoreHighXp).isGreaterThan(scoreLowXp);
    }
}
