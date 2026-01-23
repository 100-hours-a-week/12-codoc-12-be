package _ganzi.codoc.submission.service;

import _ganzi.codoc.problem.domain.Problem;
import _ganzi.codoc.problem.domain.SummaryCard;
import _ganzi.codoc.problem.exception.ProblemNotFoundException;
import _ganzi.codoc.problem.repository.ProblemRepository;
import _ganzi.codoc.problem.repository.SummaryCardRepository;
import _ganzi.codoc.submission.domain.UserProblemResult;
import _ganzi.codoc.submission.dto.SummaryCardGradingRequest;
import _ganzi.codoc.submission.dto.SummaryCardGradingResponse;
import _ganzi.codoc.submission.enums.ProblemSolvingStatus;
import _ganzi.codoc.submission.exception.InvalidAnswerFormatException;
import _ganzi.codoc.submission.repository.UserProblemResultRepository;
import _ganzi.codoc.user.domain.User;
import _ganzi.codoc.user.exception.UserNotFoundException;
import _ganzi.codoc.user.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class SummaryCardSubmissionService {

    private final ProblemRepository problemRepository;
    private final SummaryCardRepository summaryCardRepository;
    private final UserProblemResultRepository userProblemResultRepository;
    private final UserRepository userRepository;

    @Transactional
    public SummaryCardGradingResponse gradeSummaryCards(
            Long userId, Long problemId, SummaryCardGradingRequest request) {

        Problem problem =
                problemRepository.findById(problemId).orElseThrow(ProblemNotFoundException::new);

        List<SummaryCard> summaryCards =
                summaryCardRepository.findByProblemIdOrderBySummaryCardTagSequenceAsc(problemId);

        List<Integer> choiceIds = request.choiceIds();

        if (choiceIds == null || choiceIds.size() != summaryCards.size()) {
            throw new InvalidAnswerFormatException();
        }

        List<Boolean> results = new ArrayList<>(summaryCards.size());
        boolean allCorrect = true;

        for (int i = 0; i < summaryCards.size(); i++) {
            boolean result = checkAnswer(summaryCards.get(i), choiceIds.get(i));
            results.add(result);
            allCorrect &= result;
        }

        ProblemSolvingStatus updatedStatus = updateProblemSolvingStatus(userId, problem, allCorrect);

        return SummaryCardGradingResponse.of(results, updatedStatus);
    }

    private static boolean checkAnswer(SummaryCard summaryCard, Integer choiceId) {
        return choiceId == summaryCard.getAnswerIndex();
    }

    private ProblemSolvingStatus updateProblemSolvingStatus(
            Long userId, Problem problem, boolean allCorrect) {

        UserProblemResult userProblemResult =
                userProblemResultRepository
                        .findByUserIdAndProblemId(userId, problem.getId())
                        .orElseGet(() -> createUserProblemResult(userId, problem, allCorrect));

        userProblemResult.applyNextStatusForSummaryCard(allCorrect);

        return userProblemResult.getStatus();
    }

    private UserProblemResult createUserProblemResult(
            Long userId, Problem problem, boolean allCorrect) {

        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        UserProblemResult userProblemResult =
                UserProblemResult.createForSummaryCard(user, problem, allCorrect);

        return userProblemResultRepository.save(userProblemResult);
    }
}
