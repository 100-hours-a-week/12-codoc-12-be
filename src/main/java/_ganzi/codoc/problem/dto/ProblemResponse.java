package _ganzi.codoc.problem.dto;

import _ganzi.codoc.problem.enums.ProblemDifficulty;
import _ganzi.codoc.submission.enums.ProblemSolvingStatus;
import java.util.List;
import lombok.Builder;

@Builder
public record ProblemResponse(
        Long problemId,
        String title,
        ProblemDifficulty difficulty,
        String content,
        ProblemSolvingStatus status,
        boolean bookmarked,
        List<SummaryCardResponse> summaryCards,
        List<QuizResponse> quizzes) {

    public static ProblemResponse of(
            ProblemContent problemContent,
            ProblemSolvingStatus status,
            boolean bookmarked,
            boolean hasActiveSession) {

        return ProblemResponse.builder()
                .problemId(problemContent.problemId())
                .title(problemContent.title())
                .difficulty(problemContent.difficulty())
                .content(problemContent.content())
                .status(status)
                .bookmarked(bookmarked)
                .summaryCards(hasActiveSession ? problemContent.summaryCards() : null)
                .quizzes(hasActiveSession && status.summaryCardPassed() ? problemContent.quizzes() : null)
                .build();
    }
}
