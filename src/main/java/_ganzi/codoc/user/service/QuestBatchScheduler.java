package _ganzi.codoc.user.service;

import java.time.LocalDate;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QuestBatchScheduler {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    private final QuestBatchService questBatchService;

    @Scheduled(cron = "0 0 2 * * *", zone = "Asia/Seoul")
    public void issueDailyQuests() {
        questBatchService.issueDailyQuests(LocalDate.now(SEOUL));
    }
}
