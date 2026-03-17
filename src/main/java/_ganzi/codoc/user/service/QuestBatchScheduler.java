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

    @Scheduled(
            cron = "${app.schedule.quest-daily-cron:0 0 2 * * *}",
            zone = "${app.schedule.time-zone:Asia/Seoul}")
    public void issueDailyQuests() {
        questBatchService.issueDailyQuests(LocalDate.now(SEOUL));
    }
}
