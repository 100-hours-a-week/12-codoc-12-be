package _ganzi.codoc.problem.dto;

import _ganzi.codoc.problem.domain.SummaryCard;
import java.util.List;
import lombok.Builder;

@Builder
public record SummaryCardResponse(String paragraphType, List<String> choices) {

    public static SummaryCardResponse from(SummaryCard summaryCard) {
        return SummaryCardResponse.builder()
                .paragraphType(summaryCard.getSummaryCardTag().getName())
                .choices(summaryCard.getChoices())
                .build();
    }
}
