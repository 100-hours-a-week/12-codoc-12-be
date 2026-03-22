package _ganzi.codoc.custom.service;

import _ganzi.codoc.ai.dto.CustomProblemGenerationResponse;
import _ganzi.codoc.custom.domain.CustomProblem;
import _ganzi.codoc.custom.domain.CustomQuiz;
import _ganzi.codoc.custom.domain.CustomSummaryCard;
import _ganzi.codoc.custom.repository.CustomProblemRepository;
import _ganzi.codoc.custom.repository.CustomQuizRepository;
import _ganzi.codoc.custom.repository.CustomSummaryCardRepository;
import _ganzi.codoc.notification.dto.NotificationMessageItem;
import _ganzi.codoc.notification.enums.NotificationType;
import _ganzi.codoc.notification.service.NotificationDispatchService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class CustomProblemGenerationResultService {

    private final CustomProblemRepository customProblemRepository;
    private final CustomQuizRepository customQuizRepository;
    private final CustomSummaryCardRepository customSummaryCardRepository;
    private final NotificationDispatchService notificationDispatchService;

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

                            notificationDispatchService.dispatchAfterCommit(
                                    customProblem.getUserId(),
                                    new NotificationMessageItem(
                                            NotificationType.CUSTOM_PROBLEM_COMPLETED,
                                            "나만의 문제가 완성됐어요",
                                            "",
                                            Map.of("customProblemId", String.valueOf(customProblem.getId()))));
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
        customProblemRepository
                .findById(customProblemId)
                .ifPresent(
                        customProblem -> {
                            customProblem.fail();

                            notificationDispatchService.dispatchAfterCommit(
                                    customProblem.getUserId(),
                                    new NotificationMessageItem(
                                            NotificationType.CUSTOM_PROBLEM_COMPLETED, "나만의 문제 생성에 실패했어요", "", Map.of()));
                        });
    }
}
