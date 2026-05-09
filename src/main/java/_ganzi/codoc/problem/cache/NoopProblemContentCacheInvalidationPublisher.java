package _ganzi.codoc.problem.cache;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@ConditionalOnProperty(
        prefix = "app.cache.problem-content.invalidation",
        name = "enabled",
        havingValue = "false")
@Component
public class NoopProblemContentCacheInvalidationPublisher
        implements ProblemContentCacheInvalidationPublisher {

    @Override
    public void publish(Long problemId) {}
}
