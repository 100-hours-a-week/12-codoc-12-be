package _ganzi.codoc.leaderboard.scheduler;

import _ganzi.codoc.leaderboard.service.LeaderboardSnapshotBatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LeaderboardSnapshotScheduler {

    private final LeaderboardSnapshotBatchService snapshotBatchService;

    @Scheduled(cron = "0 0 * * * *", zone = "Asia/Seoul")
    public void createHourlySnapshot() {
        snapshotBatchService.createHourlySnapshot();
    }
}
