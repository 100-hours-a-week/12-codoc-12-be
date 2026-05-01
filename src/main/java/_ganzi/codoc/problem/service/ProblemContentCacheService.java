package _ganzi.codoc.problem.service;

import _ganzi.codoc.problem.config.ProblemCacheConfig;
import _ganzi.codoc.problem.domain.Problem;
import _ganzi.codoc.problem.domain.Quiz;
import _ganzi.codoc.problem.domain.SummaryCard;
import _ganzi.codoc.problem.dto.ProblemContent;
import _ganzi.codoc.problem.exception.ProblemNotFoundException;
import _ganzi.codoc.problem.repository.ProblemRepository;
import _ganzi.codoc.problem.repository.QuizRepository;
import _ganzi.codoc.problem.repository.SummaryCardRepository;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ProblemContentCacheService {

    private final CacheManager problemCacheManager;
    private final ProblemRepository problemRepository;
    private final SummaryCardRepository summaryCardRepository;
    private final QuizRepository quizRepository;

    public ProblemContent getProblemContent(Long problemId) {
        Cache nullCache = Objects.requireNonNull(problemCacheManager.getCache(ProblemCacheConfig.PROBLEM_NULL));
        if (nullCache.get(problemId) != null) {
            throw new ProblemNotFoundException();
        }

        CaffeineCache contentCache = (CaffeineCache) Objects.requireNonNull(
                problemCacheManager.getCache(ProblemCacheConfig.PROBLEM_CONTENT));

        ProblemContent content = (ProblemContent) contentCache.getNativeCache().get(problemId, key -> {
            Problem problem = problemRepository.findById((Long) key).orElse(null);
            if (problem == null) {
                return null;
            }
            List<SummaryCard> summaryCards = summaryCardRepository.findByProblemIdOrderByParagraphOrderAsc((Long) key);
            List<Quiz> quizzes = quizRepository.findByProblemIdOrderBySequenceAsc((Long) key);
            return ProblemContent.of(problem, summaryCards, quizzes);
        });

        if (content == null) {
            nullCache.put(problemId, Boolean.TRUE);
            throw new ProblemNotFoundException();
        }

        return content;
    }
}
