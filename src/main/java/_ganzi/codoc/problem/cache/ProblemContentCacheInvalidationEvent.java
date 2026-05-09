package _ganzi.codoc.problem.cache;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record ProblemContentCacheInvalidationEvent(
        String eventId, String sourceServerId, Long problemId, Instant publishedAt) {

    public ProblemContentCacheInvalidationEvent {
        Objects.requireNonNull(eventId, "eventId must not be null");
        Objects.requireNonNull(sourceServerId, "sourceServerId must not be null");
        Objects.requireNonNull(problemId, "problemId must not be null");
        Objects.requireNonNull(publishedAt, "publishedAt must not be null");
    }

    public static ProblemContentCacheInvalidationEvent of(String sourceServerId, Long problemId) {
        return new ProblemContentCacheInvalidationEvent(
                UUID.randomUUID().toString(), sourceServerId, problemId, Instant.now());
    }
}
