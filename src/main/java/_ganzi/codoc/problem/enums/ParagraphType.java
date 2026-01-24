package _ganzi.codoc.problem.enums;

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
}
