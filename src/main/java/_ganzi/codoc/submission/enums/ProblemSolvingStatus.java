package _ganzi.codoc.submission.enums;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ProblemSolvingStatus {
    NOT_ATTEMPTED(""),
    IN_PROGRESS("시도 중"),
    SUMMARY_PASSED("문제 요약 카드 완료"),
    SOLVED("해결"),
    ;

    private final String description;

    public String toDescription() {
        return description;
    }

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
