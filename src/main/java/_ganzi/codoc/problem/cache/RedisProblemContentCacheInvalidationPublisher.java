package _ganzi.codoc.problem.cache;

import _ganzi.codoc.problem.config.ProblemContentCacheInvalidationProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "app.cache.problem-content.invalidation",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
@Component
public class RedisProblemContentCacheInvalidationPublisher
        implements ProblemContentCacheInvalidationPublisher {

    private final StringRedisTemplate stringRedisTemplate;
    private final JsonMapper jsonMapper;
    private final ProblemContentCacheInvalidationProperties properties;

    @Override
    public void publish(Long problemId) {
        ProblemContentCacheInvalidationEvent event =
                ProblemContentCacheInvalidationEvent.of(properties.serverId(), problemId);
        try {
            String payload = jsonMapper.writeValueAsString(event);
            Long subscriberCount = stringRedisTemplate.convertAndSend(properties.channel(), payload);
            if (subscriberCount == null || subscriberCount == 0L) {
                log.warn(
                        "Problem content cache invalidation published with no subscribers. problemId={}",
                        problemId);
            }
        } catch (JacksonException e) {
            throw new IllegalStateException(
                    "Failed to publish problem content cache invalidation event.", e);
        }
    }
}
