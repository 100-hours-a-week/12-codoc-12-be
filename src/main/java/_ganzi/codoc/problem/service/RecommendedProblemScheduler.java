package _ganzi.codoc.problem.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RecommendedProblemScheduler {

    private final RecommendedProblemService recommendedProblemService;

    @Scheduled(
            cron = "${app.schedule.recommended-problem-cron:0 0 3 * * *}",
            zone = "${app.schedule.time-zone:Asia/Seoul}")
    public void issueDailyRecommendations() {
        recommendedProblemService.issueDailyRecommendations();
    }
}
