package _ganzi.codoc.submission.service;

import _ganzi.codoc.problem.domain.Problem;
import _ganzi.codoc.problem.domain.SummaryCard;
import _ganzi.codoc.problem.exception.ProblemNotFoundException;
import _ganzi.codoc.problem.exception.SummaryCardNotFoundException;
import _ganzi.codoc.problem.repository.ProblemRepository;
import _ganzi.codoc.problem.repository.SummaryCardRepository;
import _ganzi.codoc.submission.domain.UserProblemResult;
import _ganzi.codoc.submission.dto.SummaryCardGradingRequest;
import _ganzi.codoc.submission.dto.SummaryCardGradingResponse;
import _ganzi.codoc.submission.enums.ProblemSolvingStatus;
import _ganzi.codoc.submission.exception.InvalidAnswerFormatException;
import _ganzi.codoc.submission.repository.UserProblemResultRepository;
import _ganzi.codoc.submission.util.AnswerChecker;
import _ganzi.codoc.user.domain.User;
import _ganzi.codoc.user.exception.UserNotFoundException;
import _ganzi.codoc.user.repository.UserRepository;
import _ganzi.codoc.user.service.UserStatsService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
    private final UserStatsService userStatsService;

    @Transactional
    public SummaryCardGradingResponse gradeSummaryCards(
            Long userId, SummaryCardGradingRequest request) {

        Long problemId = request.problemId();

        Problem problem =
                problemRepository.findById(problemId).orElseThrow(ProblemNotFoundException::new);

        List<SummaryCard> summaryCards =
                summaryCardRepository.findByProblemIdOrderByParagraphOrderAsc(problemId);

        if (summaryCards.isEmpty()) {
            throw new SummaryCardNotFoundException();
        }

        List<Integer> choiceIds = request.choiceIds();

        validateChoice(choiceIds, summaryCards);

        List<Boolean> results = new ArrayList<>(summaryCards.size());
        boolean allCorrect = true;

        for (int i = 0; i < summaryCards.size(); i++) {
            boolean result = AnswerChecker.check(summaryCards.get(i).getAnswerIndex(), choiceIds.get(i));
            results.add(result);
            allCorrect &= result;
        }

        ProblemSolvingStatus updatedStatus = updateProblemSolvingStatus(userId, problem, allCorrect);

        return SummaryCardGradingResponse.of(results, updatedStatus);
    }

    private ProblemSolvingStatus updateProblemSolvingStatus(
            Long userId, Problem problem, boolean allCorrect) {

        Optional<UserProblemResult> existingResult =
                userProblemResultRepository.findByUserIdAndProblemId(userId, problem.getId());

        boolean isFirstAttempt = existingResult.isEmpty();

        UserProblemResult userProblemResult =
                existingResult.orElseGet(() -> createUserProblemResult(userId, problem, allCorrect));

        userProblemResult.applyNextStatusForSummaryCard(allCorrect);

        if (isFirstAttempt) {
            userStatsService.incrementSolvingCount(userId);
        }

        return userProblemResult.getStatus();
    }

    private UserProblemResult createUserProblemResult(
            Long userId, Problem problem, boolean allCorrect) {

        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        UserProblemResult userProblemResult =
                UserProblemResult.createForSummaryCard(user, problem, allCorrect);

        return userProblemResultRepository.save(userProblemResult);
    }

    private static void validateChoice(List<Integer> choiceIds, List<SummaryCard> summaryCards) {
        if (choiceIds.size() != summaryCards.size()) {
            throw new InvalidAnswerFormatException();
        }

        for (int i = 0; i < summaryCards.size(); i++) {
            int choiceIndex = choiceIds.get(i);
            int choiceSize = summaryCards.get(i).getChoices().size();

            if (choiceIndex < 0 || choiceIndex >= choiceSize) {
                throw new InvalidAnswerFormatException();
            }
        }
    }
}
