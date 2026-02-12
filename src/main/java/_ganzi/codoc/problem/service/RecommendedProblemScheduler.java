package _ganzi.codoc.problem.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RecommendedProblemScheduler {

    private final RecommendedProblemService recommendedProblemService;

    @Scheduled(cron = "${app.recommend.cron:0 0 3 * * *}", zone = "Asia/Seoul")
    public void issueDailyRecommendations() {
        recommendedProblemService.issueDailyRecommendations();
    }
}
