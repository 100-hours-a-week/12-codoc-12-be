package _ganzi.codoc.problem.dto;

import _ganzi.codoc.problem.domain.Problem;
import _ganzi.codoc.problem.domain.Quiz;
import _ganzi.codoc.problem.domain.SummaryCard;
import _ganzi.codoc.problem.enums.ProblemLevel;
import _ganzi.codoc.submission.enums.ProblemSolvingStatus;
import java.util.List;
import lombok.Builder;

@Builder
public record ProblemResponse(
        Long problemId,
        String title,
        ProblemLevel level,
        String content,
        String status,
        boolean bookmarked,
        List<SummaryCardResponse> summaryCards,
        List<QuizResponse> quizzes) {

    public static ProblemResponse of(
            Problem problem,
            ProblemSolvingStatus status,
            boolean bookmarked,
            List<SummaryCard> summaryCards,
            List<Quiz> quizzes) {

        return ProblemResponse.builder()
                .problemId(problem.getId())
                .title(problem.getTitle())
                .level(problem.getLevel())
                .content(problem.getContent())
                .status(status.toDescription())
                .bookmarked(bookmarked)
                .summaryCards(summaryCards.stream().map(SummaryCardResponse::from).toList())
                .quizzes(quizzes.stream().map(QuizResponse::from).toList())
                .build();
    }
}
