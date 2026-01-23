package _ganzi.codoc.problem.exception;

import _ganzi.codoc.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum ProblemErrorCode implements ErrorCode {
    PROBLEM_NOT_FOUND(HttpStatus.NOT_FOUND, "PROBLEM_NOT_FOUND", "문제를 찾을 수 없습니다."),
    SUMMARY_CARD_NOT_FOUND(HttpStatus.NOT_FOUND, "SUMMARY_CARD_NOT_FOUND", "문제 요약 카드를 찾을 수 없습니다."),
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
