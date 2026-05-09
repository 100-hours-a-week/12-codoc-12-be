package _ganzi.codoc.problem.cache;

import _ganzi.codoc.problem.config.ProblemContentCacheInvalidationProperties;
import _ganzi.codoc.problem.service.ProblemContentCacheInvalidationService;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "app.cache.problem-content.invalidation",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
@Component
public class RedisProblemContentCacheInvalidationSubscriber implements MessageListener {

    private final JsonMapper jsonMapper;
    private final ProblemContentCacheInvalidationService invalidationService;
    private final ProblemContentCacheInvalidationProperties properties;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String payload = new String(message.getBody(), StandardCharsets.UTF_8);
        deserialize(payload).ifPresent(this::handleEvent);
    }

    private void handleEvent(ProblemContentCacheInvalidationEvent event) {
        if (isPublishedByCurrentServer(event)) {
            return;
        }
        try {
            invalidationService.invalidateLocalCache(event.problemId());
        } catch (RuntimeException exception) {
            log.warn(
                    "Problem content cache invalidation evict failed. eventId={}, sourceServerId={}, problemId={}",
                    event.eventId(),
                    event.sourceServerId(),
                    event.problemId(),
                    exception);
        }
    }

    private Optional<ProblemContentCacheInvalidationEvent> deserialize(String payload) {
        try {
            return Optional.of(jsonMapper.readValue(payload, ProblemContentCacheInvalidationEvent.class));
        } catch (Exception e) {
            log.warn("Failed to deserialize problem content cache invalidation event.", e);
            return Optional.empty();
        }
    }

    private boolean isPublishedByCurrentServer(ProblemContentCacheInvalidationEvent event) {
        return event.sourceServerId() != null
                && Objects.equals(event.sourceServerId(), properties.serverId());
    }
}
