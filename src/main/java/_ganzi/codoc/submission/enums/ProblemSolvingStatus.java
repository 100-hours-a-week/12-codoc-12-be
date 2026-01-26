package _ganzi.codoc.submission.enums;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ProblemSolvingStatus {
    NOT_ATTEMPTED,
    IN_PROGRESS,
    SUMMARY_CARD_PASSED,
    SOLVED,
    ;

    public ProblemSolvingStatus nextStatusForSummaryCard(boolean allCorrect) {
        if (this == SUMMARY_CARD_PASSED || this == SOLVED) {
            return this;
        }

        return allCorrect ? SUMMARY_CARD_PASSED : IN_PROGRESS;
    }

    public boolean summaryCardPassed() {
        return this == SUMMARY_CARD_PASSED || this == SOLVED;
    }
}
