package _ganzi.codoc.problem.enums;

import java.util.Arrays;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ParagraphType {
    BACKGROUND(1),
    GOAL(2),
    RULE(3),
    CONSTRAINT(4),
    ;

    private final int order;

    public static ParagraphType getInitialType() {
        return BACKGROUND;
    }

    public ParagraphType next() {
        int nextOrder = this.order + 1;

        return Arrays.stream(values()).filter(type -> type.order == nextOrder).findFirst().orElse(this);
    }
}
