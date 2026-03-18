package _ganzi.codoc.custom.dto;

import _ganzi.codoc.custom.domain.CustomSummaryCard;
import _ganzi.codoc.problem.enums.ParagraphType;
import java.util.List;

public record CustomSummaryCardResponse(
        ParagraphType paragraphType, int paragraphOrder, List<String> choices, int answerIndex) {

    public static CustomSummaryCardResponse from(CustomSummaryCard summaryCard) {
        return new CustomSummaryCardResponse(
                summaryCard.getParagraphType(),
                summaryCard.getParagraphOrder(),
                summaryCard.getChoices(),
                summaryCard.getAnswerIndex());
    }
}
