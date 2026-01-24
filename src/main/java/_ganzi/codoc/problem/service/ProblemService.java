package _ganzi.codoc.problem.service;

import _ganzi.codoc.global.dto.CursorPagingResponse;
import _ganzi.codoc.global.util.CursorPagingUtils;
import _ganzi.codoc.problem.domain.Problem;
import _ganzi.codoc.problem.domain.Quiz;
import _ganzi.codoc.problem.domain.SummaryCard;
import _ganzi.codoc.problem.dto.ProblemListCondition;
import _ganzi.codoc.problem.dto.ProblemListItem;
import _ganzi.codoc.problem.dto.ProblemResponse;
import _ganzi.codoc.problem.dto.ProblemSearchParam;
import _ganzi.codoc.problem.enums.ProblemLevel;
import _ganzi.codoc.problem.exception.ProblemNotFoundException;
import _ganzi.codoc.problem.repository.BookmarkRepository;
import _ganzi.codoc.problem.repository.ProblemRepository;
import _ganzi.codoc.problem.repository.QuizRepository;
import _ganzi.codoc.problem.repository.SummaryCardRepository;
import _ganzi.codoc.submission.domain.UserProblemResult;
import _ganzi.codoc.submission.enums.ProblemSolvingStatus;
import _ganzi.codoc.submission.repository.UserProblemResultRepository;
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
    private final QuizRepository quizRepository;
    private final SummaryCardRepository summaryCardRepository;
    private final UserProblemResultRepository userProblemResultRepository;
    private final BookmarkRepository bookmarkRepository;

    public CursorPagingResponse<ProblemListItem, Long> getProblemList(
            Long userId, ProblemListCondition condition) {

        ProblemSearchParam param = createProblemSearchParam(userId, condition);

        List<ProblemListItem> items = problemRepository.findProblemList(param);

        return CursorPagingUtils.apply(items, condition.limit(), ProblemListItem::problemId);
    }

    public CursorPagingResponse<ProblemListItem, Long> searchProblems(
            Long userId, ProblemListCondition condition) {
        ProblemSearchParam param = createProblemSearchParam(userId, condition);

        List<ProblemListItem> items = problemRepository.searchProblemList(param);

        return CursorPagingUtils.apply(items, condition.limit(), ProblemListItem::problemId);
    }

    public ProblemResponse getProblemDetail(Long userId, Long problemId) {
        Problem problem =
                problemRepository.findById(problemId).orElseThrow(ProblemNotFoundException::new);

        ProblemSolvingStatus status =
                userProblemResultRepository
                        .findByUserIdAndProblemId(userId, problemId)
                        .map(UserProblemResult::getStatus)
                        .orElse(ProblemSolvingStatus.NOT_ATTEMPTED);

        boolean bookmarked = bookmarkRepository.existsByUserIdAndProblemId(userId, problemId);

        List<SummaryCard> summaryCards =
                summaryCardRepository.findByProblemIdOrderByParagraphOrderAsc(problemId);

        List<Quiz> quizzes = quizRepository.findByProblemIdOrderBySequenceAsc(problemId);

        return ProblemResponse.of(problem, status, bookmarked, summaryCards, quizzes);
    }

    private ProblemSearchParam createProblemSearchParam(Long userId, ProblemListCondition condition) {
        Pageable pageable = CursorPagingUtils.createPageable(condition.limit());

        return new ProblemSearchParam(
                userId,
                condition.cursor(),
                condition.query(),
                normalizeLevels(condition.levels()),
                normalizeStatuses(condition.statuses()),
                ProblemSolvingStatus.NOT_ATTEMPTED,
                condition.bookmarked(),
                pageable);
    }

    private List<ProblemSolvingStatus> normalizeStatuses(List<ProblemSolvingStatus> statuses) {
        if (statuses == null || statuses.isEmpty()) {
            return List.of(ProblemSolvingStatus.values());
        }
        return statuses;
    }

    private List<ProblemLevel> normalizeLevels(List<ProblemLevel> levels) {
        if (levels == null || levels.isEmpty()) {
            return List.of(ProblemLevel.values());
        }
        return levels;
    }
}
