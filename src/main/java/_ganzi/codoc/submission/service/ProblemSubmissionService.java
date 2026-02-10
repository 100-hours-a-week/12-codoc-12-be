package _ganzi.codoc.submission.service;

import _ganzi.codoc.problem.domain.Problem;
import _ganzi.codoc.problem.exception.ProblemNotFoundException;
import _ganzi.codoc.problem.repository.ProblemRepository;
import _ganzi.codoc.problem.repository.QuizRepository;
import _ganzi.codoc.problem.service.RecommendedProblemService;
import _ganzi.codoc.submission.domain.UserProblemResult;
import _ganzi.codoc.submission.domain.UserQuizAttempt;
import _ganzi.codoc.submission.dto.ProblemSubmissionResponse;
import _ganzi.codoc.submission.enums.ProblemSolvingStatus;
import _ganzi.codoc.submission.enums.QuizAttemptStatus;
import _ganzi.codoc.submission.exception.InvalidProblemSubmissionException;
import _ganzi.codoc.submission.repository.UserProblemResultRepository;
import _ganzi.codoc.submission.repository.UserQuizAttemptRepository;
import _ganzi.codoc.submission.repository.UserQuizResultRepository;
import _ganzi.codoc.user.service.QuestService;
import _ganzi.codoc.user.service.UserStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ProblemSubmissionService {

    private static final int PROBLEM_SOLVED_XP = 50;

    private final ProblemRepository problemRepository;
    private final QuizRepository quizRepository;
    private final UserProblemResultRepository userProblemResultRepository;
    private final UserQuizAttemptRepository userQuizAttemptRepository;
    private final UserQuizResultRepository userQuizResultRepository;
    private final UserStatsService userStatsService;
    private final QuestService questService;
    private final RecommendedProblemService recommendedProblemService;

    @Transactional
    public ProblemSubmissionResponse submissionProblem(Long userId, Long problemId) {
        Problem problem =
                problemRepository.findById(problemId).orElseThrow(ProblemNotFoundException::new);

        UserProblemResult userProblemResult =
                userProblemResultRepository
                        .findByUserIdAndProblemId(userId, problemId)
                        .orElseThrow(InvalidProblemSubmissionException::new);

        userProblemResult.validateCanEvaluateProblemResult();

        UserQuizAttempt attempt = findLatestAttempt(userId, problemId);

        int totalQuizCount = quizRepository.countByProblemId(problem.getId());
        int solvedCount = userQuizResultRepository.countByAttemptId(attempt.getId());

        if (solvedCount != totalQuizCount) {
            throw new InvalidProblemSubmissionException();
        }

        int correctCount = userQuizResultRepository.countByAttemptIdAndCorrectTrue(attempt.getId());

        attempt.complete();

        boolean xpGranted = false;
        ProblemSolvingStatus nextStatus = userProblemResult.getStatus();

        if (correctCount == totalQuizCount && !userProblemResult.isSolved()) {
            userProblemResult.markSolved();
            userStatsService.applyProblemSolved(userId, PROBLEM_SOLVED_XP);
            recommendedProblemService.markProblemSolved(userId, problemId);
            questService.refreshUserQuestStatuses(userId);
            xpGranted = true;
            nextStatus = ProblemSolvingStatus.SOLVED;
        }

        return ProblemSubmissionResponse.of(correctCount, nextStatus, xpGranted);
    }

    private UserQuizAttempt findLatestAttempt(Long userId, Long problemId) {
        return userQuizAttemptRepository
                .findFirstByUserIdAndProblemIdAndStatusOrderByIdDesc(
                        userId, problemId, QuizAttemptStatus.IN_PROGRESS)
                .orElseGet(
                        () ->
                                userQuizAttemptRepository
                                        .findFirstByUserIdAndProblemIdAndStatusOrderByIdDesc(
                                                userId, problemId, QuizAttemptStatus.COMPLETED)
                                        .orElseThrow(InvalidProblemSubmissionException::new));
    }
}
