package _ganzi.codoc.problem.dto;

import _ganzi.codoc.problem.domain.Quiz;
import _ganzi.codoc.problem.domain.SummaryCard;
import _ganzi.codoc.submission.domain.ProblemSession;
import java.time.Instant;
import java.util.List;
import lombok.Builder;

@Builder
public record ProblemSessionResponse(
        Long sessionId,
        Long problemId,
        Instant expiresAt,
        List<SummaryCardResponse> summaryCards,
        List<QuizResponse> quizzes) {

    public static ProblemSessionResponse of(
            ProblemSession session, List<SummaryCard> summaryCards, List<Quiz> quizzes) {
        return ProblemSessionResponse.builder()
                .sessionId(session.getId())
                .problemId(session.getProblem().getId())
                .expiresAt(session.getExpiresAt())
                .summaryCards(summaryCards.stream().map(SummaryCardResponse::from).toList())
                .quizzes(quizzes.stream().map(QuizResponse::from).toList())
                .build();
    }
}
