package _ganzi.codoc.surprise.service;

import _ganzi.codoc.surprise.domain.SurpriseEvent;
import _ganzi.codoc.surprise.dto.SubmissionReservation;
import _ganzi.codoc.surprise.dto.SurpriseEventSnapshot;
import _ganzi.codoc.surprise.enums.SubmissionReservationStatus;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class SurpriseEventRedisService {

    private static final String META_KEY_FORMAT = "quiz:event:%d:meta";
    private static final String COUNT_KEY_FORMAT = "quiz:event:%d:count";
    private static final String SUBMISSIONS_KEY_FORMAT = "quiz:event:%d:submissions";
    private static final String QUIZ_CACHE_KEY_FORMAT = "quiz:event:%d";
    private static final Duration KEY_TTL_BUFFER = Duration.ofMinutes(5);

    @SuppressWarnings("rawtypes")
    private static final DefaultRedisScript<List> RESERVE_SUBMISSION_SCRIPT =
            new DefaultRedisScript<>(
                    // language=lua
                    """
                    local countKey = KEYS[1]
                    local submissionsKey = KEYS[2]
                    local maxRewardCount = tonumber(ARGV[1])
                    local correct = tonumber(ARGV[2])
                    local userId = ARGV[3]

                    local marker = redis.call('HGET', submissionsKey, userId)
                    if marker then
                      return {'DUPLICATE', tonumber(marker)}
                    end

                    local count = tonumber(redis.call('GET', countKey) or '0')
                    if count >= maxRewardCount then
                      return {'EXHAUSTED', 0}
                    end

                    local rankMarker = 0
                    if correct == 1 then
                      rankMarker = redis.call('INCR', countKey)
                    end

                    redis.call('HSET', submissionsKey, userId, rankMarker)
                    return {'ACCEPTED', rankMarker}
                    """,
                    List.class);

    private static final DefaultRedisScript<Long> ROLLBACK_SUBMISSION_SCRIPT =
            new DefaultRedisScript<>(
                    // language=lua
                    """
                    local countKey = KEYS[1]
                    local submissionsKey = KEYS[2]
                    local expectedMarker = tonumber(ARGV[1])
                    local userId = ARGV[2]

                    local currentMarker = redis.call('HGET', submissionsKey, userId)
                    if not currentMarker or tonumber(currentMarker) ~= expectedMarker then
                      return 0
                    end

                    redis.call('HDEL', submissionsKey, userId)
                    if expectedMarker > 0 then
                      redis.call('DECR', countKey)
                    end
                    return 1
                    """,
                    Long.class);

    private final StringRedisTemplate stringRedisTemplate;

    public SubmissionReservation reserveSubmission(
            SurpriseEventSnapshot snapshot, Long userId, boolean correct, Instant now) {
        List<?> result =
                stringRedisTemplate.execute(
                        RESERVE_SUBMISSION_SCRIPT,
                        List.of(countKey(snapshot.eventId()), submissionsKey(snapshot.eventId())),
                        String.valueOf(snapshot.maxRewardCount()),
                        correct ? "1" : "0",
                        String.valueOf(userId));

        if (result == null || result.size() < 2) {
            throw new IllegalStateException("Failed to reserve surprise submission.");
        }

        SubmissionReservationStatus status =
                SubmissionReservationStatus.fromValue(String.valueOf(result.get(0)));
        int marker = toInt(result.get(1));

        return switch (status) {
            case ACCEPTED ->
                    new SubmissionReservation(
                            SubmissionReservationStatus.ACCEPTED,
                            marker > 0 ? marker : null,
                            marker);
            case DUPLICATE ->
                    new SubmissionReservation(
                            SubmissionReservationStatus.DUPLICATE, null, marker);
            case EXHAUSTED ->
                    new SubmissionReservation(
                            SubmissionReservationStatus.EXHAUSTED, null, 0);
        };
    }

    public boolean isRewardExhausted(SurpriseEvent event) {
        String count = stringRedisTemplate.opsForValue().get(countKey(event.getId()));
        if (count == null) {
            return false;
        }
        return Integer.parseInt(count) >= event.getMaxRewardCount();
    }

    public Optional<SurpriseEventSnapshot> getEventSnapshot(Long eventId) {
        List<Object> values =
                stringRedisTemplate
                        .opsForHash()
                        .multiGet(
                                metaKey(eventId),
                                List.of("startsAt", "endsAt", "answerChoiceNo", "maxRewardCount"));
        if (values.stream().anyMatch(Objects::isNull)) {
            return Optional.empty();
        }
        return Optional.of(
                new SurpriseEventSnapshot(
                        eventId,
                        Instant.parse((String) values.get(0)),
                        Instant.parse((String) values.get(1)),
                        Integer.parseInt((String) values.get(2)),
                        Integer.parseInt((String) values.get(3))));
    }

    public void cacheEventSnapshot(SurpriseEventSnapshot snapshot, Instant now) {
        Duration ttl = Duration.ofSeconds(resolveTtlSeconds(snapshot.endsAt(), now));
        String meta = metaKey(snapshot.eventId());
        stringRedisTemplate
                .opsForHash()
                .putAll(
                        meta,
                        Map.of(
                                "startsAt", snapshot.startsAt().toString(),
                                "endsAt", snapshot.endsAt().toString(),
                                "answerChoiceNo", String.valueOf(snapshot.answerChoiceNo()),
                                "maxRewardCount", String.valueOf(snapshot.maxRewardCount())));
        stringRedisTemplate.expire(meta, ttl);
        stringRedisTemplate.opsForValue().setIfAbsent(countKey(snapshot.eventId()), "0", ttl);
        String submissions = submissionsKey(snapshot.eventId());
        stringRedisTemplate.opsForHash().putIfAbsent(submissions, "_init", "0");
        stringRedisTemplate.expire(submissions, ttl);
    }

    public Optional<String> getQuizPayload(Long eventId) {
        String cached = stringRedisTemplate.opsForValue().get(quizCacheKey(eventId));
        return Optional.ofNullable(cached);
    }

    public void cacheQuizPayload(Long eventId, String payloadJson, Instant endsAt) {
        Duration ttl = Duration.between(Instant.now(), endsAt);
        if (!ttl.isNegative() && !ttl.isZero()) {
            stringRedisTemplate.opsForValue().set(quizCacheKey(eventId), payloadJson, ttl);
        }
    }

    public void rollbackReservation(Long eventId, Long userId, int markerValue) {
        stringRedisTemplate.execute(
                ROLLBACK_SUBMISSION_SCRIPT,
                List.of(countKey(eventId), submissionsKey(eventId)),
                String.valueOf(markerValue),
                String.valueOf(userId));
    }

    private long resolveTtlSeconds(Instant endsAt, Instant now) {
        Duration ttl = Duration.between(now, endsAt).plus(KEY_TTL_BUFFER);
        return Math.max(ttl.toSeconds(), 1);
    }

    private String metaKey(Long eventId) {
        return String.format(META_KEY_FORMAT, eventId);
    }

    private String countKey(Long eventId) {
        return String.format(COUNT_KEY_FORMAT, eventId);
    }

    private String submissionsKey(Long eventId) {
        return String.format(SUBMISSIONS_KEY_FORMAT, eventId);
    }

    private String quizCacheKey(Long eventId) {
        return String.format(QUIZ_CACHE_KEY_FORMAT, eventId);
    }

    private int toInt(Object value) {
        return ((Number) value).intValue();
    }
}
