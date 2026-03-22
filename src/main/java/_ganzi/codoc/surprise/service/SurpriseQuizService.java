package _ganzi.codoc.surprise.service;

import _ganzi.codoc.surprise.domain.SurpriseEvent;
import _ganzi.codoc.surprise.domain.SurpriseEventStatus;
import _ganzi.codoc.surprise.domain.SurpriseQuizSubmission;
import _ganzi.codoc.surprise.dto.SurpriseQuizPayload;
import _ganzi.codoc.surprise.dto.SurpriseQuizSubmitRequest;
import _ganzi.codoc.surprise.dto.SurpriseQuizSubmitResponse;
import _ganzi.codoc.surprise.dto.SurpriseQuizViewResponse;
import _ganzi.codoc.surprise.exception.SurpriseEventNotFoundException;
import _ganzi.codoc.surprise.exception.SurpriseEventNotOpenException;
import _ganzi.codoc.surprise.exception.SurpriseEventSubmissionClosedException;
import _ganzi.codoc.surprise.exception.SurpriseInvalidChoiceNoException;
import _ganzi.codoc.surprise.exception.SurpriseQuizAlreadySubmittedException;
import _ganzi.codoc.surprise.exception.SurpriseQuizContentInvalidException;
import _ganzi.codoc.surprise.repository.SurpriseEventRepository;
import _ganzi.codoc.surprise.repository.SurpriseQuizSubmissionRepository;
import _ganzi.codoc.user.domain.User;
import _ganzi.codoc.user.exception.UserNotFoundException;
import _ganzi.codoc.user.repository.UserRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class SurpriseQuizService {

    private static final String QUIZ_CACHE_KEY_FORMAT = "surprise:event:%d:quiz";
    private static final int CHOICE_NO_MIN = 1;
    private static final int CHOICE_NO_MAX = 4;

    private final SurpriseEventRepository surpriseEventRepository;
    private final SurpriseQuizSubmissionRepository surpriseQuizSubmissionRepository;
    private final UserRepository userRepository;
    private final JsonMapper jsonMapper;
    private final StringRedisTemplate stringRedisTemplate;

    public SurpriseQuizViewResponse getQuiz(Long userId, Long eventId) {
        SurpriseEvent event =
                surpriseEventRepository
                        .findByIdWithQuizPool(eventId)
                        .orElseThrow(SurpriseEventNotFoundException::new);

        SurpriseQuizSubmission submission =
                surpriseQuizSubmissionRepository.findByEventIdAndUserId(eventId, userId).orElse(null);
        if (submission != null) {
            return SurpriseQuizViewResponse.submitted(
                    submission.getRankNo(), submission.getElapsedMillis(), event.getEndsAt());
        }

        validateEventOpenAndSubmittable(event, Instant.now());
        SurpriseQuizPayload payload = resolveQuizPayloadWithCache(event);
        return SurpriseQuizViewResponse.notSubmitted(payload, event.getEndsAt());
    }

    @Transactional
    public SurpriseQuizSubmitResponse submitQuiz(
            Long userId, Long eventId, SurpriseQuizSubmitRequest request) {
        SurpriseEvent event =
                surpriseEventRepository
                        .findByIdForUpdate(eventId)
                        .orElseThrow(SurpriseEventNotFoundException::new);
        Instant now = Instant.now();

        validateEventOpenAndSubmittable(event, now);
        validateChoiceNo(request.choiceNo());

        SurpriseQuizSubmission existing =
                surpriseQuizSubmissionRepository.findByEventIdAndUserId(eventId, userId).orElse(null);
        if (existing != null) {
            throw new SurpriseQuizAlreadySubmittedException();
        }

        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

        boolean correct = event.getQuizPool().getAnswerChoiceNo().intValue() == request.choiceNo();
        long elapsedMillis = Duration.between(event.getStartsAt(), now).toMillis();
        Integer rankNo = null;
        if (correct) {
            long correctCount = surpriseQuizSubmissionRepository.countByEventIdAndCorrectTrue(eventId);
            rankNo = (int) correctCount + 1;
        }

        SurpriseQuizSubmission submission =
                SurpriseQuizSubmission.submit(event, user, correct, now, elapsedMillis, rankNo);
        surpriseQuizSubmissionRepository.save(submission);

        return new SurpriseQuizSubmitResponse(rankNo, elapsedMillis);
    }

    private void validateChoiceNo(Integer choiceNo) {
        if (choiceNo == null || choiceNo < CHOICE_NO_MIN || choiceNo > CHOICE_NO_MAX) {
            throw new SurpriseInvalidChoiceNoException();
        }
    }

    private void validateEventOpenAndSubmittable(SurpriseEvent event, Instant now) {
        if (!event.isOpenAt(now)) {
            if (event.getStatus() != SurpriseEventStatus.OPEN) {
                throw new SurpriseEventNotOpenException();
            }
            throw new SurpriseEventSubmissionClosedException();
        }
    }

    private SurpriseQuizPayload resolveQuizPayloadWithCache(SurpriseEvent event) {
        String cacheKey = String.format(QUIZ_CACHE_KEY_FORMAT, event.getId());
        try {
            String cached = stringRedisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                return validatePayload(jsonMapper.readValue(cached, SurpriseQuizPayload.class));
            }
        } catch (Exception exception) {
            log.warn("surprise quiz cache read failed. eventId={}", event.getId(), exception);
        }

        SurpriseQuizPayload payload = parsePayload(event.getQuizPool().getContent());
        Duration ttl = Duration.between(Instant.now(), event.getEndsAt());
        if (!ttl.isNegative() && !ttl.isZero()) {
            try {
                String serialized = jsonMapper.writeValueAsString(payload);
                stringRedisTemplate.opsForValue().set(cacheKey, serialized, ttl);
            } catch (Exception exception) {
                log.warn("surprise quiz cache write failed. eventId={}", event.getId(), exception);
            }
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
