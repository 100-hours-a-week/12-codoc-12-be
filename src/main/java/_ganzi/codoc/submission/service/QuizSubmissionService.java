package _ganzi.codoc.submission.service;

import _ganzi.codoc.problem.domain.Quiz;
import _ganzi.codoc.problem.exception.QuizNotFoundException;
import _ganzi.codoc.problem.repository.QuizRepository;
import _ganzi.codoc.submission.domain.UserProblemResult;
import _ganzi.codoc.submission.domain.UserQuizAttempt;
import _ganzi.codoc.submission.domain.UserQuizResult;
import _ganzi.codoc.submission.dto.QuizGradingRequest;
import _ganzi.codoc.submission.dto.QuizGradingResponse;
import _ganzi.codoc.submission.enums.ProblemSolvingStatus;
import _ganzi.codoc.submission.enums.QuizAttemptStatus;
import _ganzi.codoc.submission.exception.InvalidAnswerFormatException;
import _ganzi.codoc.submission.exception.InvalidQuizAttemptException;
import _ganzi.codoc.submission.exception.PrevQuizNotSubmittedException;
import _ganzi.codoc.submission.exception.QuizAlreadySubmittedException;
import _ganzi.codoc.submission.exception.QuizGradingNotAllowedException;
import _ganzi.codoc.submission.repository.UserProblemResultRepository;
import _ganzi.codoc.submission.repository.UserQuizAttemptRepository;
import _ganzi.codoc.submission.repository.UserQuizResultRepository;
import _ganzi.codoc.submission.util.AnswerChecker;
import _ganzi.codoc.user.domain.User;
import _ganzi.codoc.user.exception.UserNotFoundException;
import _ganzi.codoc.user.repository.UserRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class QuizSubmissionService {

    private final QuizRepository quizRepository;
    private final UserProblemResultRepository userProblemResultRepository;
    private final UserQuizAttemptRepository userQuizAttemptRepository;
    private final UserQuizResultRepository userQuizResultRepository;
    private final UserRepository userRepository;

    @Transactional
    public QuizGradingResponse gradeQuiz(Long userId, Long quizId, QuizGradingRequest request) {

        Quiz quiz = quizRepository.findById(quizId).orElseThrow(QuizNotFoundException::new);

        validateQuizGradingAvailable(userId, quiz);

        String idempotencyKey = request.idempotencyKey();

        if (request.attemptId() == null) {
            UserQuizResult existingResult =
                    userQuizResultRepository
                            .findByAttemptUserIdAndQuizIdAndIdempotencyKey(userId, quiz.getId(), idempotencyKey)
                            .orElse(null);

            if (existingResult != null) {
                return QuizGradingResponse.of(
                        existingResult.isCorrect(), existingResult.getAttempt().getId(), quiz.getExplanation());
            }
        }

        UserQuizAttempt attempt = resolveAttemptIfProvided(userId, quiz, request.attemptId());

        if (attempt != null) {
            UserQuizResult existingResult =
                    userQuizResultRepository
                            .findByAttemptIdAndQuizId(attempt.getId(), quiz.getId())
                            .orElse(null);

            if (existingResult != null) {
                if (!existingResult.isIdempotencyKeySame(idempotencyKey)) {
                    throw new QuizAlreadySubmittedException();
                }

                return QuizGradingResponse.of(
                        existingResult.isCorrect(), attempt.getId(), quiz.getExplanation());
            }
        }

        validateQuizSequenceOrder(attempt == null ? null : attempt.getId(), quiz);
        validateChoice(quiz, request.choiceId());

        if (attempt == null) {
            attempt = createNewAttempt(userId, quiz);
        }

        boolean result = AnswerChecker.check(quiz.getAnswerIndex(), request.choiceId());
        saveQuizResult(attempt, quiz, idempotencyKey, result);

        return QuizGradingResponse.of(result, attempt.getId(), quiz.getExplanation());
    }

    private void validateQuizGradingAvailable(Long userId, Quiz quiz) {
        ProblemSolvingStatus status =
                userProblemResultRepository
                        .findByUserIdAndProblemId(userId, quiz.getProblem().getId())
                        .map(UserProblemResult::getStatus)
                        .orElseThrow(QuizGradingNotAllowedException::new);

        if (!status.summaryCardPassed()) {
            throw new QuizGradingNotAllowedException();
        }
    }

    private UserQuizAttempt resolveAttemptIfProvided(Long userId, Quiz quiz, Long attemptId) {
        if (attemptId == null) {
            return null;
        }

        UserQuizAttempt attempt =
                userQuizAttemptRepository
                        .findByIdAndUserIdAndProblemId(attemptId, userId, quiz.getProblem().getId())
                        .orElseThrow(InvalidQuizAttemptException::new);

        if (!attempt.isInProgress()) {
            throw new InvalidQuizAttemptException();
        }

        return attempt;
    }

    private UserQuizAttempt createNewAttempt(Long userId, Quiz quiz) {
        abandonInProgressAttempts(userId, quiz.getProblem().getId());
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        UserQuizAttempt attempt = UserQuizAttempt.create(user, quiz.getProblem());
        return userQuizAttemptRepository.save(attempt);
    }

    private void abandonInProgressAttempts(Long userId, Long problemId) {
        userQuizAttemptRepository
                .findAllByUserIdAndProblemIdAndStatus(userId, problemId, QuizAttemptStatus.IN_PROGRESS)
                .forEach(UserQuizAttempt::abandon);
    }

    private void saveQuizResult(
            UserQuizAttempt attempt, Quiz quiz, String idempotencyKey, boolean result) {
        UserQuizResult userQuizResult = UserQuizResult.create(attempt, quiz, idempotencyKey, result);
        userQuizResultRepository.save(userQuizResult);
    }

    private void validateQuizSequenceOrder(Long attemptId, Quiz quiz) {
        List<Quiz> quizzes =
                quizRepository.findByProblemIdOrderBySequenceAsc(quiz.getProblem().getId());

        List<UserQuizResult> results =
                attemptId == null ? List.of() : userQuizResultRepository.findAllByAttemptId(attemptId);

        Set<Long> solvedQuizIds = new HashSet<>();

        for (UserQuizResult result : results) {
            solvedQuizIds.add(result.getQuiz().getId());
        }

        for (Quiz candidate : quizzes) {
            if (!solvedQuizIds.contains(candidate.getId())) {
                if (!candidate.getId().equals(quiz.getId())) {
                    throw new PrevQuizNotSubmittedException();
                }
                return;
            }
        }
    }

    private static void validateChoice(Quiz quiz, int choiceId) {
        int choiceSize = quiz.getChoices().size();

        if (choiceId < 0 || choiceId >= choiceSize) {
            throw new InvalidAnswerFormatException();
        }
    }
}
