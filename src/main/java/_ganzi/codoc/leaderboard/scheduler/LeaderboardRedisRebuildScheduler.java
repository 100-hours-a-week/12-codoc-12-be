package _ganzi.codoc.leaderboard.scheduler;

import _ganzi.codoc.leaderboard.config.LeaderboardRedisProperties;
import _ganzi.codoc.leaderboard.service.LeaderboardRedisRebuildService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LeaderboardRedisRebuildScheduler {

    private final LeaderboardRedisProperties properties;
    private final LeaderboardRedisRebuildService rebuildService;

    @Scheduled(
            cron = "${app.schedule.leaderboard-redis-rebuild-cron:0 */10 * * * *}",
            zone = "${app.schedule.time-zone:Asia/Seoul}")
    public void rebuildIfNeeded() {
        if (!properties.writeEnabled()) {
            return;
        }
        boolean rebuilt = rebuildService.rebuildReadableSeasonIfMissing();
        if (rebuilt) {
            log.info("leaderboard redis rebuild triggered by key-missing detection");
        }
    }
}
