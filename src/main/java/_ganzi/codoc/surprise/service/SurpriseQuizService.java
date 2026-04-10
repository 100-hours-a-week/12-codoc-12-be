package _ganzi.codoc.surprise.service;

import _ganzi.codoc.surprise.domain.SurpriseEvent;
import _ganzi.codoc.surprise.domain.SurpriseEventStatus;
import _ganzi.codoc.surprise.domain.SurpriseQuizSubmission;
import _ganzi.codoc.surprise.dto.*;
import _ganzi.codoc.surprise.enums.SubmissionReservationStatus;
import _ganzi.codoc.surprise.exception.SurpriseEventNotFoundException;
import _ganzi.codoc.surprise.exception.SurpriseEventNotOpenException;
import _ganzi.codoc.surprise.exception.SurpriseEventRewardExhaustedException;
import _ganzi.codoc.surprise.exception.SurpriseEventSubmissionClosedException;
import _ganzi.codoc.surprise.exception.SurpriseQuizAlreadySubmittedException;
import _ganzi.codoc.surprise.exception.SurpriseQuizContentInvalidException;
import _ganzi.codoc.surprise.repository.SurpriseEventRepository;
import _ganzi.codoc.surprise.repository.SurpriseQuizSubmissionRepository;
import _ganzi.codoc.user.domain.User;
import _ganzi.codoc.user.repository.UserRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
@RequiredArgsConstructor
@Service
public class SurpriseQuizService {

    private static final PageRequest CURRENT_EVENT_PAGE = PageRequest.of(0, 1);

    private final SurpriseEventRepository surpriseEventRepository;
    private final SurpriseQuizSubmissionRepository surpriseQuizSubmissionRepository;
    private final SurpriseEventRedisService surpriseEventRedisService;
    private final UserRepository userRepository;
    private final JsonMapper jsonMapper;
    private final TransactionTemplate transactionTemplate;

    @Transactional(readOnly = true)
    public SurpriseQuizViewResponse getCurrentQuiz(Long userId) {
        SurpriseEvent event = resolveCurrentActiveEvent();
        return getQuizInternal(userId, event);
    }

    @Transactional(readOnly = true)
    public SurpriseQuizViewResponse getQuiz(Long userId, Long eventId) {
        SurpriseEvent event =
                surpriseEventRepository
                        .findByIdWithQuizPool(eventId)
                        .orElseThrow(SurpriseEventNotFoundException::new);
        return getQuizInternal(userId, event);
    }

    public SurpriseQuizSubmitResponse submitCurrentQuiz(
            Long userId, SurpriseQuizSubmitRequest request) {
        SurpriseEvent event = resolveCurrentActiveEvent();
        validateEventOpen(event, Instant.now());
        SurpriseEventSnapshot snapshot = cacheAndCreateSnapshot(event);
        return submitQuizInternal(userId, snapshot, request.choiceNo());
    }

    public SurpriseQuizSubmitResponse submitQuiz(
            Long userId, Long eventId, SurpriseQuizSubmitRequest request) {
        SurpriseEventSnapshot snapshot = resolveEventSnapshot(eventId);
        return submitQuizInternal(userId, snapshot, request.choiceNo());
    }

    private SurpriseQuizViewResponse getQuizInternal(Long userId, SurpriseEvent event) {
        Long eventId = event.getId();
        SurpriseQuizSubmission submission =
                surpriseQuizSubmissionRepository.findByEventIdAndUserId(eventId, userId).orElse(null);
        if (submission != null) {
            return SurpriseQuizViewResponse.submitted(
                    submission.isCorrect(),
                    submission.getRankNo(),
                    submission.getElapsedMillis(),
                    event.getEndsAt());
        }

        validateEventOpenAndSubmittable(event, Instant.now());
        SurpriseQuizPayload payload = resolveQuizPayloadWithCache(event);
        return SurpriseQuizViewResponse.notSubmitted(payload, event.getEndsAt());
    }

    private SurpriseQuizSubmitResponse submitQuizInternal(
            Long userId, SurpriseEventSnapshot snapshot, int choiceNo) {
        Instant now = Instant.now();

        validateEventOpen(snapshot, now);
        boolean correct = snapshot.answerChoiceNo() == choiceNo;
        SubmissionReservation reservation =
                surpriseEventRedisService.reserveSubmission(snapshot, userId, correct, now);
        if (reservation.status() == SubmissionReservationStatus.DUPLICATE) {
            throw new SurpriseQuizAlreadySubmittedException();
        }
        if (reservation.status() == SubmissionReservationStatus.EXHAUSTED) {
            throw new SurpriseEventRewardExhaustedException();
        }

        long elapsedMillis = Duration.between(snapshot.startsAt(), now).toMillis();
        Integer rankNo = correct ? reservation.rankNo() : null;

        try {
            transactionTemplate.executeWithoutResult(status -> {
                SurpriseEvent eventRef =
                        surpriseEventRepository.getReferenceById(snapshot.eventId());
                User userRef = userRepository.getReferenceById(userId);
                SurpriseQuizSubmission submission =
                        SurpriseQuizSubmission.submit(
                                eventRef, userRef, correct, now, elapsedMillis, rankNo);
                saveSubmission(submission);
            });
        } catch (RuntimeException exception) {
            surpriseEventRedisService.rollbackReservation(
                    snapshot.eventId(), userId, reservation.markerValue());
            throw exception;
        }

        return new SurpriseQuizSubmitResponse(correct, rankNo, elapsedMillis);
    }

    private SurpriseEvent resolveCurrentActiveEvent() {
        Instant now = Instant.now();
        List<SurpriseEvent> events =
                surpriseEventRepository.findCurrentActiveEvents(now, CURRENT_EVENT_PAGE);
        if (events.isEmpty()) {
            throw new SurpriseEventNotOpenException();
        }
        return events.getFirst();
    }

    private SurpriseEventSnapshot resolveEventSnapshot(Long eventId) {
        try {
            Optional<SurpriseEventSnapshot> cached =
                    surpriseEventRedisService.getEventSnapshot(eventId);
            if (cached.isPresent()) {
                return cached.get();
            }
        } catch (Exception exception) {
            log.warn("event snapshot cache read failed. eventId={}", eventId, exception);
        }
        SurpriseEvent event =
                surpriseEventRepository
                        .findByIdWithQuizPool(eventId)
                        .orElseThrow(SurpriseEventNotFoundException::new);
        validateEventOpen(event, Instant.now());
        return cacheAndCreateSnapshot(event);
    }

    private SurpriseEventSnapshot cacheAndCreateSnapshot(SurpriseEvent event) {
        SurpriseEventSnapshot snapshot = SurpriseEventSnapshot.from(event);
        try {
            surpriseEventRedisService.cacheEventSnapshot(snapshot, Instant.now());
        } catch (Exception exception) {
            log.warn("event snapshot cache write failed. eventId={}", event.getId(), exception);
        }
        return snapshot;
    }

    private void validateEventOpenAndSubmittable(SurpriseEvent event, Instant now) {
        if (surpriseEventRedisService.isRewardExhausted(event)) {
            throw new SurpriseEventRewardExhaustedException();
        }
        validateEventOpen(event, now);
    }

    private void validateEventOpen(SurpriseEvent event, Instant now) {
        if (!event.isOpenAt(now)) {
            if (event.getStatus() != SurpriseEventStatus.OPEN) {
                throw new SurpriseEventNotOpenException();
            }
            throw new SurpriseEventSubmissionClosedException();
        }
    }

    private void validateEventOpen(SurpriseEventSnapshot snapshot, Instant now) {
        if (!snapshot.isOpenAt(now)) {
            if (now.isBefore(snapshot.startsAt())) {
                throw new SurpriseEventNotOpenException();
            }
            throw new SurpriseEventSubmissionClosedException();
        }
    }

    private void saveSubmission(SurpriseQuizSubmission submission) {
        try {
            surpriseQuizSubmissionRepository.saveAndFlush(submission);
        } catch (DataIntegrityViolationException exception) {
            throw new SurpriseQuizAlreadySubmittedException();
        }
    }

    private SurpriseQuizPayload resolveQuizPayloadWithCache(SurpriseEvent event) {
        try {
            Optional<String> cached =
                    surpriseEventRedisService.getQuizPayload(event.getId());
            if (cached.isPresent()) {
                return validatePayload(jsonMapper.readValue(cached.get(), SurpriseQuizPayload.class));
            }
        } catch (Exception exception) {
            log.warn("surprise quiz cache read failed. eventId={}", event.getId(), exception);
        }

        SurpriseQuizPayload payload = parsePayload(event.getQuizPool().getContent());
        try {
            String serialized = jsonMapper.writeValueAsString(payload);
            surpriseEventRedisService.cacheQuizPayload(
                    event.getId(), serialized, event.getEndsAt());
        } catch (Exception exception) {
            log.warn("surprise quiz cache write failed. eventId={}", event.getId(), exception);
        }
        return payload;
    }

    private SurpriseQuizPayload parsePayload(String contentJson) {
        try {
            SurpriseQuizPayload payload = jsonMapper.readValue(contentJson, SurpriseQuizPayload.class);
            return validatePayload(payload);
        } catch (Exception exception) {
            throw new SurpriseQuizContentInvalidException();
        }
    }

    private SurpriseQuizPayload validatePayload(SurpriseQuizPayload payload) {
        if (payload == null || payload.content() == null || payload.choices() == null) {
            throw new SurpriseQuizContentInvalidException();
        }
        List<String> choices = payload.choices();
        boolean hasInvalidChoice =
                choices.stream().anyMatch(choice -> choice == null || choice.isBlank());
        if (choices.size() != 4 || hasInvalidChoice) {
            throw new SurpriseQuizContentInvalidException();
        }
        return payload;
    }
}
