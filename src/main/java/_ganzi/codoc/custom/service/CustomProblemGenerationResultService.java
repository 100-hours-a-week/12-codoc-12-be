package _ganzi.codoc.custom.service;

import _ganzi.codoc.ai.dto.CustomProblemGenerationResponse;
import _ganzi.codoc.custom.domain.CustomProblem;
import _ganzi.codoc.custom.domain.CustomQuiz;
import _ganzi.codoc.custom.domain.CustomSummaryCard;
import _ganzi.codoc.custom.repository.CustomProblemRepository;
import _ganzi.codoc.custom.repository.CustomQuizRepository;
import _ganzi.codoc.custom.repository.CustomSummaryCardRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class CustomProblemGenerationResultService {

    private final CustomProblemRepository customProblemRepository;
    private final CustomQuizRepository customQuizRepository;
    private final CustomSummaryCardRepository customSummaryCardRepository;

    @Transactional
    public void complete(Long customProblemId, CustomProblemGenerationResponse response) {
        customProblemRepository
                .findById(customProblemId)
                .ifPresent(
                        customProblem -> {
                            customProblem.complete(
                                    response.problemDetail().title(), response.problemDetail().content());

                            saveSummaryCards(customProblem, response.summaryCard());
                            saveQuizzes(customProblem, response.quiz());
                        });
    }

    private void saveSummaryCards(
            CustomProblem customProblem,
            List<CustomProblemGenerationResponse.SummaryCardItem> summaryCardItems) {
        List<CustomSummaryCard> summaryCards =
                summaryCardItems.stream().map(item -> item.toEntity(customProblem)).toList();

        customSummaryCardRepository.saveAll(summaryCards);
    }

    private void saveQuizzes(
            CustomProblem customProblem, List<CustomProblemGenerationResponse.QuizItem> quizItems) {
        List<CustomQuiz> quizzes =
                quizItems.stream().map(item -> item.toEntity(customProblem)).toList();
        customQuizRepository.saveAll(quizzes);
    }

    @Transactional
    public void fail(Long customProblemId) {
        customProblemRepository.findById(customProblemId).ifPresent(CustomProblem::fail);
    }
}
