package _ganzi.codoc.problem.enums;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ProblemLevel {
    ONE(1),
    TWO(2),
    THREE(3),
    FOUR(4),
    FIVE(5),
    ;

    private final int number;

    public int toNumber() {
        return number;
    }
}
