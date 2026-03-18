package _ganzi.codoc.custom.enums;

public enum CustomProblemStatus {
    PROCESSING,
    COMPLETED,
    FAILED,
    ;

    public boolean isCompleted() {
        return this == COMPLETED;
    }
}
