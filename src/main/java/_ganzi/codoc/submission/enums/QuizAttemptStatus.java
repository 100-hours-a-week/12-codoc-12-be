package _ganzi.codoc.submission.enums;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum QuizAttemptStatus {
    IN_PROGRESS("잔행 중"),
    COMPLETED("완료"),
    ABANDONED("만료"),
    ;

    private final String description;
}
