package _ganzi.codoc.problem.service;

import _ganzi.codoc.problem.cache.ProblemContentCacheInvalidationPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProblemContentCacheInvalidationService {

    private final ProblemContentCacheService problemContentCacheService;
    private final ProblemContentCacheInvalidationPublisher publisher;

    public void invalidateAndPublish(Long problemId) {
        invalidateLocalCache(problemId);
        publishInvalidation(problemId);
    }

    public void invalidateLocalCache(Long problemId) {
        problemContentCacheService.evict(problemId);
    }

    private void publishInvalidation(Long problemId) {
        try {
            publisher.publish(problemId);
        } catch (Exception exception) {
            log.warn(
                    "Problem content cache invalidation publish failed. problemId={}",
                    problemId,
                    exception);
        }
    }
}
