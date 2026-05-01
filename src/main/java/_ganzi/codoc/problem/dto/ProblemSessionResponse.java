package _ganzi.codoc.problem.dto;

import _ganzi.codoc.submission.domain.ProblemSession;
import java.time.Instant;
import java.util.List;
import lombok.Builder;

@Builder
public record ProblemSessionResponse(
        Long sessionId,
        Long problemId,
        Instant expiresAt,
        Instant chatbotCompletedAt,
        List<SummaryCardResponse> summaryCards,
        List<QuizResponse> quizzes) {

    public static ProblemSessionResponse of(ProblemSession session, ProblemContent problemContent) {
        return ProblemSessionResponse.builder()
                .sessionId(session.getId())
                .problemId(session.getProblem().getId())
                .expiresAt(session.getExpiresAt())
                .chatbotCompletedAt(session.getChatbotCompletedAt())
                .summaryCards(problemContent.summaryCards())
                .quizzes(problemContent.quizzes())
                .build();
    }
}
