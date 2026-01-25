package _ganzi.codoc.problem.domain;

import _ganzi.codoc.problem.enums.ParagraphType;
import lombok.Builder;

@Builder
public record AnswerGuideItem(ParagraphType paragraphType, String originalText) {

    public static AnswerGuideItem of(ParagraphType paragraphType, String originalText) {
        return AnswerGuideItem.builder()
                .paragraphType(paragraphType)
                .originalText(originalText)
                .build();
    }
}
