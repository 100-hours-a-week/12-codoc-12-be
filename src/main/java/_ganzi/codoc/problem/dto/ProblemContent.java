package _ganzi.codoc.problem.dto;

import _ganzi.codoc.problem.domain.Problem;
import _ganzi.codoc.problem.domain.Quiz;
import _ganzi.codoc.problem.domain.SummaryCard;
import _ganzi.codoc.problem.enums.ProblemDifficulty;
import java.util.List;
import lombok.Builder;

@Builder
public record ProblemContent(
        Long problemId,
        String title,
        ProblemDifficulty difficulty,
        String content,
        List<SummaryCardResponse> summaryCards,
        List<QuizResponse> quizzes) {

    public static ProblemContent of(
            Problem problem, List<SummaryCard> summaryCards, List<Quiz> quizzes) {
        return ProblemContent.builder()
                .problemId(problem.getId())
                .title(problem.getTitle())
                .difficulty(problem.getDifficulty())
                .content(problem.getContent())
                .summaryCards(summaryCards.stream().map(SummaryCardResponse::from).toList())
                .quizzes(quizzes.stream().map(QuizResponse::from).toList())
                .build();
    }
}
