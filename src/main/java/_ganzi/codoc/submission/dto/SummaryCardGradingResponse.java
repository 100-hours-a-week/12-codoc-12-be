package _ganzi.codoc.submission.dto;

import _ganzi.codoc.submission.enums.ProblemSolvingStatus;
import java.util.List;
import lombok.Builder;

@Builder
public record SummaryCardGradingResponse(List<Boolean> results, ProblemSolvingStatus status) {

    public static SummaryCardGradingResponse of(List<Boolean> results, ProblemSolvingStatus status) {
        return SummaryCardGradingResponse.builder().results(results).status(status).build();
    }
}
