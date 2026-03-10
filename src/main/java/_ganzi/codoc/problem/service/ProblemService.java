package _ganzi.codoc.problem.service;

import _ganzi.codoc.global.cursor.CursorPageFetcher;
import _ganzi.codoc.global.dto.CursorPagingResponse;
import _ganzi.codoc.global.exception.ResourceNotFoundException;
import _ganzi.codoc.problem.domain.Problem;
import _ganzi.codoc.problem.domain.Quiz;
import _ganzi.codoc.problem.domain.RecommendedProblem;
import _ganzi.codoc.problem.domain.SummaryCard;
import _ganzi.codoc.problem.dto.ProblemListCondition;
import _ganzi.codoc.problem.dto.ProblemListItem;
import _ganzi.codoc.problem.dto.ProblemResponse;
import _ganzi.codoc.problem.dto.ProblemSearchParam;
import _ganzi.codoc.problem.dto.ProblemSessionResponse;
import _ganzi.codoc.problem.dto.RecommendedProblemResponse;
import _ganzi.codoc.problem.exception.ProblemNotFoundException;
import _ganzi.codoc.problem.exception.RecommendNotAvailableException;
import _ganzi.codoc.problem.repository.BookmarkRepository;
import _ganzi.codoc.problem.repository.ProblemRepository;
import _ganzi.codoc.problem.repository.RecommendedProblemRepository;
import _ganzi.codoc.submission.domain.UserProblemResult;
import _ganzi.codoc.submission.enums.ProblemSolvingStatus;
import _ganzi.codoc.submission.repository.UserProblemResultRepository;
import _ganzi.codoc.submission.service.ProblemSessionService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ProblemService {

    private final ProblemRepository problemRepository;
    private final ProblemSessionService problemSessionService;
    private final UserProblemResultRepository userProblemResultRepository;
    private final BookmarkRepository bookmarkRepository;
    private final RecommendedProblemRepository recommendedProblemRepository;
    private final ProblemContentCacheService problemContentCacheService;
    private final CursorPageFetcher cursorPageFetcher;

    public CursorPagingResponse<ProblemListItem, Long> getProblemList(
            Long userId, ProblemListCondition condition) {

        return cursorPageFetcher.fetchPlain(
                condition.limit(),
                pageable ->
                        problemRepository.findProblemList(
                                createProblemSearchParam(userId, condition, pageable)),
                items -> items,
                ProblemListItem::problemId);
    }

    public CursorPagingResponse<ProblemListItem, Long> searchProblems(
            Long userId, ProblemListCondition condition) {

        return cursorPageFetcher.fetchPlain(
                condition.limit(),
                pageable ->
                        problemRepository.searchProblemList(
                                createProblemSearchParam(userId, condition, pageable)),
                items -> items,
                ProblemListItem::problemId);
    }

    public ProblemResponse getProblemDetail(Long userId, Long problemId) {
        if (problemContentCacheService.isNegativeProblem(problemId)) {
            throw new ProblemNotFoundException();
        }
        Problem problem;
        try {
            problem = problemContentCacheService.getProblem(problemId);
        } catch (ProblemNotFoundException exception) {
            problemContentCacheService.cacheNegativeProblem(problemId);
            throw exception;
        }

        ProblemSolvingStatus status =
                userProblemResultRepository
                        .findByUserIdAndProblemId(userId, problemId)
                        .map(UserProblemResult::getStatus)
                        .orElse(ProblemSolvingStatus.NOT_ATTEMPTED);

        boolean bookmarked = bookmarkRepository.existsByUserIdAndProblemId(userId, problemId);

        return ProblemResponse.of(problem, status, bookmarked);
    }

    @Transactional
    public ProblemSessionResponse startProblemSession(Long userId, Long problemId) {
        var session = problemSessionService.resolveOrCreate(userId, problemId);
        Long sessionProblemId = session.getProblem().getId();

        List<SummaryCard> summaryCards = problemContentCacheService.getSummaryCards(sessionProblemId);
        List<Quiz> quizzes = problemContentCacheService.getQuizzes(sessionProblemId);

        return ProblemSessionResponse.of(session, summaryCards, quizzes);
    }

    public ProblemSessionResponse getActiveProblemSession(Long userId) {
        var session = problemSessionService.findActiveByUser(userId);
        if (session == null) {
            throw new ResourceNotFoundException();
        }

        Long sessionProblemId = session.getProblem().getId();

        List<SummaryCard> summaryCards = problemContentCacheService.getSummaryCards(sessionProblemId);
        List<Quiz> quizzes = problemContentCacheService.getQuizzes(sessionProblemId);

        return ProblemSessionResponse.of(session, summaryCards, quizzes);
    }

    @Transactional
    public void closeActiveProblemSession(Long userId) {
        var session = problemSessionService.closeActiveSession(userId);
        if (session == null) {
            throw new ResourceNotFoundException();
        }
    }

    public RecommendedProblemResponse getRecommendedProblem(Long userId) {
        RecommendedProblem recommendedProblem =
                recommendedProblemRepository
                        .findFirstByUserIdAndIsDoneFalseOrderByRecommendedAtAsc(userId)
                        .orElseThrow(RecommendNotAvailableException::new);
        Long problemId = recommendedProblem.getProblem().getId();
        ProblemSolvingStatus status =
                userProblemResultRepository
                        .findByUserIdAndProblemId(userId, problemId)
                        .map(UserProblemResult::getStatus)
                        .orElse(ProblemSolvingStatus.NOT_ATTEMPTED);
        boolean bookmarked = bookmarkRepository.existsByUserIdAndProblemId(userId, problemId);
        return RecommendedProblemResponse.of(recommendedProblem, status, bookmarked);
    }

    private ProblemSearchParam createProblemSearchParam(
            Long userId, ProblemListCondition condition, Pageable pageable) {
        return new ProblemSearchParam(
                userId,
                condition.cursor(),
                condition.query(),
                condition.difficulties(),
                condition.statuses(),
                ProblemSolvingStatus.NOT_ATTEMPTED,
                condition.bookmarked(),
                pageable);
    }
}
