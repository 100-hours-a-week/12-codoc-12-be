package _ganzi.codoc.leaderboard.infra.redis;

import org.springframework.stereotype.Component;

@Component
public class LeaderboardRedisKeyFactory {

    public String globalKey(Integer seasonId) {
        return "lb:" + seasonId + ":global";
    }

    public String leagueKey(Integer seasonId, Integer leagueId) {
        return "lb:" + seasonId + ":league:" + leagueId;
    }

    public String groupKey(Integer seasonId, Long groupId) {
        return "lb:" + seasonId + ":group:" + groupId;
    }
}
