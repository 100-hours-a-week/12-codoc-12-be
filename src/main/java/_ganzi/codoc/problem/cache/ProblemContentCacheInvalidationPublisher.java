package _ganzi.codoc.problem.cache;

public interface ProblemContentCacheInvalidationPublisher {

    void publish(Long problemId);
}
