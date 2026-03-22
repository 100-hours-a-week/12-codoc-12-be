package _ganzi.codoc.surprise.dto;

import java.time.Instant;

public record SurpriseQuizViewResponse(
        SurpriseQuizSubmissionStatus submissionStatus,
        Integer rank,
        Long elapsedMs,
        Instant eventEndsAt,
        SurpriseQuizPayload quiz) {

    public static SurpriseQuizViewResponse submitted(
            Integer rank, Long elapsedMs, Instant eventEndsAt) {
        return new SurpriseQuizViewResponse(
                SurpriseQuizSubmissionStatus.SUBMITTED, rank, elapsedMs, eventEndsAt, null);
    }

    public static SurpriseQuizViewResponse notSubmitted(
            SurpriseQuizPayload quiz, Instant eventEndsAt) {
        return new SurpriseQuizViewResponse(
                SurpriseQuizSubmissionStatus.NOT_SUBMITTED, null, null, eventEndsAt, quiz);
    }
}
