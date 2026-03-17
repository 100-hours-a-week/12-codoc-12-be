package _ganzi.codoc.leaderboard.scheduler;

import _ganzi.codoc.leaderboard.service.LeaderboardSnapshotBatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LeaderboardSnapshotScheduler {

    private final LeaderboardSnapshotBatchService snapshotBatchService;

    @Scheduled(
            cron = "${app.schedule.leaderboard-hourly-snapshot-cron:0 0 * * * *}",
            zone = "${app.schedule.time-zone:Asia/Seoul}")
    public void createHourlySnapshot() {
        snapshotBatchService.createHourlySnapshot();
    }

    @Scheduled(
            cron = "${app.schedule.leaderboard-season-start-snapshot-cron:0 0 0 * * TUE}",
            zone = "${app.schedule.time-zone:Asia/Seoul}")
    public void createSeasonStartSnapshot() {
        snapshotBatchService.createSeasonStartSnapshot();
    }

    @Scheduled(
            cron = "${app.schedule.leaderboard-season-end-snapshot-cron:0 0 0 * * MON}",
            zone = "${app.schedule.time-zone:Asia/Seoul}")
    public void createSeasonEndSnapshot() {
        snapshotBatchService.createSeasonEndSnapshot();
    }
}
