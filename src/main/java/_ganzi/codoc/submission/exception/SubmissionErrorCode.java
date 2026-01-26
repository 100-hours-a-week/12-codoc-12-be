package _ganzi.codoc.submission.exception;

import _ganzi.codoc.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum SubmissionErrorCode implements ErrorCode {
    INVALID_ANSWER_FORMAT(HttpStatus.BAD_REQUEST, "INVALID_ANSWER_FORMAT", "답변 형식이 올바르지 않습니다."),
    QUIZ_GRADING_NOT_ALLOWED(
            HttpStatus.BAD_REQUEST, "QUIZ_GRADING_NOT_ALLOWED", "문제 요약 카드 풀이가 완료되지 않았습니다."),
    INVALID_QUIZ_ATTEMPT(HttpStatus.BAD_REQUEST, "INVALID_QUIZ_ATTEMPT", "유효하지 않은 퀴즈 시도입니다."),
    QUIZ_ALREADY_SUBMITTED(HttpStatus.BAD_REQUEST, "QUIZ_ALREADY_SUBMITTED", "이미 제출된 퀴즈입니다."),
    INVALID_PROBLEM_RESULT_EVALUATION(
            HttpStatus.BAD_REQUEST, "INVALID_PROBLEM_RESULT_EVALUATION", "문제 풀이 결과를 평가할 수 없는 상태입니다."),
    PREV_QUIZ_NOT_SUBMITTED(HttpStatus.BAD_REQUEST, "PREV_QUIZ_NOT_SUBMITTED", "이전 퀴즈를 먼저 풀어야 합니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;

    @Override
    public HttpStatus status() {
        return status;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }
}
