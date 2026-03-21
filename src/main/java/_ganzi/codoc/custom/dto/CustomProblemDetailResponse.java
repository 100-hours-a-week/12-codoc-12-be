package _ganzi.codoc.custom.dto;

import _ganzi.codoc.custom.domain.CustomProblem;
import java.time.Instant;
import java.util.List;

public record CustomProblemDetailResponse(
        Long customProblemId,
        String title,
        String content,
        Instant createdAt,
        List<CustomSummaryCardResponse> summaryCards,
        List<CustomQuizResponse> quizzes) {

    public static CustomProblemDetailResponse of(
            CustomProblem customProblem,
            List<CustomSummaryCardResponse> summaryCards,
            List<CustomQuizResponse> quizzes) {
        return new CustomProblemDetailResponse(
                customProblem.getId(),
                customProblem.getTitle(),
                customProblem.getContent(),
                customProblem.getCreatedAt(),
                summaryCards,
                quizzes);
    }
}
