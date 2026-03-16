package _ganzi.codoc.leaderboard.infra.redis;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class LeaderboardRedisKeyFactoryTest {

    private final LeaderboardRedisKeyFactory keyFactory = new LeaderboardRedisKeyFactory();

    @Test
    void should_build_global_key() {
        String key = keyFactory.globalKey(202610);

        assertThat(key).isEqualTo("lb:202610:global");
    }

    @Test
    void should_build_league_key() {
        String key = keyFactory.leagueKey(202610, 3);

        assertThat(key).isEqualTo("lb:202610:league:3");
    }

    @Test
    void should_build_group_key() {
        String key = keyFactory.groupKey(202610, 17L);

        assertThat(key).isEqualTo("lb:202610:group:17");
    }
}
