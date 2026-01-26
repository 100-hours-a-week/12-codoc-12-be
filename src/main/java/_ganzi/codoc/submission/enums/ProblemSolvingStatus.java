package _ganzi.codoc.submission.enums;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ProblemSolvingStatus {
    NOT_ATTEMPTED,
    IN_PROGRESS,
    SUMMARY_PASSED,
    SOLVED,
    ;

    public ProblemSolvingStatus nextStatusForSummaryCard(boolean allCorrect) {
        if (this == SUMMARY_PASSED || this == SOLVED) {
            return this;
        }

        return allCorrect ? SUMMARY_PASSED : IN_PROGRESS;
    }

    public boolean summaryCardPassed() {
        return this == SUMMARY_PASSED || this == SOLVED;
    }
}
