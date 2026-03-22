package _ganzi.codoc.surprise.service;

import _ganzi.codoc.surprise.domain.SurpriseEvent;
import _ganzi.codoc.surprise.domain.SurpriseEventStatus;
import _ganzi.codoc.surprise.domain.SurpriseQuizPool;
import _ganzi.codoc.surprise.domain.SurpriseQuizPoolStatus;
import _ganzi.codoc.surprise.repository.SurpriseEventRepository;
import _ganzi.codoc.surprise.repository.SurpriseQuizPoolRepository;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class SurpriseEventLifecycleService {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    private static final Duration EVENT_DURATION = Duration.ofMinutes(50);
    private static final LocalTime EVENT_START_TIME = LocalTime.of(20, 0);

    private final SurpriseEventRepository surpriseEventRepository;
    private final SurpriseQuizPoolRepository surpriseQuizPoolRepository;

    @Transactional
    public void createWeeklyEventIfAbsent() {
        ZonedDateTime now = ZonedDateTime.now(SEOUL);
        LocalDate friday = now.toLocalDate().with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY));
        ZonedDateTime startsAtSeoul = friday.atTime(EVENT_START_TIME).atZone(SEOUL);
        ZonedDateTime endsAtSeoul = startsAtSeoul.plus(EVENT_DURATION);
        String weekKey = friday.format(DateTimeFormatter.BASIC_ISO_DATE);

        if (surpriseEventRepository.existsByEventWeekKey(weekKey)) {
            return;
        }

        SurpriseQuizPool quizPool =
                surpriseQuizPoolRepository
                        .findFirstByStatusOrderByIdAsc(SurpriseQuizPoolStatus.UNUSED)
                        .orElse(null);
        if (quizPool == null) {
            log.warn("surprise event create skipped. reason=no-unused-quiz, weekKey={}", weekKey);
            return;
        }

        quizPool.markInProgress();
        SurpriseEvent event =
                SurpriseEvent.schedule(
                        quizPool, weekKey, startsAtSeoul.toInstant(), endsAtSeoul.toInstant());
        surpriseEventRepository.save(event);
        log.info("surprise event scheduled. eventId={}, weekKey={}", event.getId(), weekKey);
    }

    @Transactional
    public void openDueEvents() {
        Instant now = Instant.now();
        List<SurpriseEvent> dueEvents =
                surpriseEventRepository.findAllByStatusAndStartsAtLessThanEqual(
                        SurpriseEventStatus.SCHEDULED, now);
        for (SurpriseEvent event : dueEvents) {
            event.open();
            log.info(
                    "surprise event opened. eventId={}, weekKey={}", event.getId(), event.getEventWeekKey());
        }
    }
}
