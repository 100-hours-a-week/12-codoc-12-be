package _ganzi.codoc.problem.service;

import _ganzi.codoc.global.constants.CacheNames;
import _ganzi.codoc.problem.domain.Problem;
import _ganzi.codoc.problem.domain.Quiz;
import _ganzi.codoc.problem.domain.SummaryCard;
import _ganzi.codoc.problem.exception.ProblemNotFoundException;
import _ganzi.codoc.problem.repository.ProblemRepository;
import _ganzi.codoc.problem.repository.QuizRepository;
import _ganzi.codoc.problem.repository.SummaryCardRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ProblemContentCacheService {

    private final ProblemRepository problemRepository;
    private final SummaryCardRepository summaryCardRepository;
    private final QuizRepository quizRepository;

    @Cacheable(
            cacheManager = CacheNames.CAFFEINE_CACHE_MANAGER,
            cacheNames = CacheNames.PROBLEM_DETAIL,
            key = "#problemId")
    public Problem getProblem(Long problemId) {
        return problemRepository.findById(problemId).orElseThrow(ProblemNotFoundException::new);
    }

    @Cacheable(
            cacheManager = CacheNames.CAFFEINE_CACHE_MANAGER,
            cacheNames = CacheNames.PROBLEM_SUMMARY_CARDS,
            key = "#problemId")
    public List<SummaryCard> getSummaryCards(Long problemId) {
        return List.copyOf(summaryCardRepository.findByProblemIdOrderByParagraphOrderAsc(problemId));
    }

    @Cacheable(
            cacheManager = CacheNames.CAFFEINE_CACHE_MANAGER,
            cacheNames = CacheNames.PROBLEM_QUIZZES,
            key = "#problemId")
    public List<Quiz> getQuizzes(Long problemId) {
        return List.copyOf(quizRepository.findByProblemIdOrderBySequenceAsc(problemId));
    }
}
