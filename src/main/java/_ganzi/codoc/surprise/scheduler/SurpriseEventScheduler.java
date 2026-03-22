package _ganzi.codoc.surprise.scheduler;

import _ganzi.codoc.surprise.service.SurpriseEventLifecycleService;
import _ganzi.codoc.surprise.service.SurpriseEventSettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.surprise-event", name = "enabled", havingValue = "true")
@Component
public class SurpriseEventScheduler {

    private final SurpriseEventLifecycleService surpriseEventLifecycleService;
    private final SurpriseEventSettlementService surpriseEventSettlementService;

    @Scheduled(
            cron = "${app.schedule.surprise-event-create-cron:0 0 0 * * MON}",
            zone = "${app.schedule.time-zone:Asia/Seoul}")
    public void createWeeklyEvent() {
        surpriseEventLifecycleService.createWeeklyEventIfAbsent();
    }

    @Scheduled(
            cron = "${app.schedule.surprise-event-open-cron:0 0 20 * * FRI}",
            zone = "${app.schedule.time-zone:Asia/Seoul}")
    public void openDueEvents() {
        surpriseEventLifecycleService.openDueEvents();
    }

    @Scheduled(
            cron = "${app.schedule.surprise-event-close-cron:0 50 20 * * FRI}",
            zone = "${app.schedule.time-zone:Asia/Seoul}")
    public void closeAndSettle() {
        surpriseEventSettlementService.closeExpiredOpenEventsAndSettle();
    }
}
