package _ganzi.codoc.problem.enums;

import java.util.Arrays;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ParagraphType {
    BACKGROUND(1, "배경"),
    GOAL(2, "목표"),
    RULE(3, "규칙"),
    CONSTRAINT(4, "제약 사항"),
    ;

    private final int order;
    private final String description;

    public static ParagraphType getInitialType() {
        return BACKGROUND;
    }

    public ParagraphType next() {
        int nextOrder = this.order + 1;

        return Arrays.stream(values()).filter(type -> type.order == nextOrder).findFirst().orElse(this);
    }
}
