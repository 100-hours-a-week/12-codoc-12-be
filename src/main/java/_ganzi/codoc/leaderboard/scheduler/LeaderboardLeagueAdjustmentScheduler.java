package _ganzi.codoc.leaderboard.scheduler;

import _ganzi.codoc.leaderboard.service.LeaderboardLeagueAdjustmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LeaderboardLeagueAdjustmentScheduler {

    private final LeaderboardLeagueAdjustmentService adjustmentService;

    @Scheduled(cron = "0 0 4 * * MON", zone = "Asia/Seoul")
    public void adjustLeaguesForSeasonEnd() {
        adjustmentService.adjustLeaguesForSeasonEnd();
    }
}
