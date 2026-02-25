package _ganzi.codoc.leaderboard.scheduler;

import _ganzi.codoc.leaderboard.service.LeaderboardSeasonBatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LeaderboardSeasonScheduler {

    private final LeaderboardSeasonBatchService seasonBatchService;

    @Scheduled(cron = "0 0 10 * * MON", zone = "Asia/Seoul")
    public void assignGroupsForNextSeason() {
        seasonBatchService.assignGroupsForNextSeason();
    }
}
