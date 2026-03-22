package _ganzi.codoc.surprise.exception;

import _ganzi.codoc.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum SurpriseEventErrorCode implements ErrorCode {
    SURPRISE_EVENT_NOT_FOUND(HttpStatus.NOT_FOUND, "SURPRISE_EVENT_NOT_FOUND", "기습 이벤트를 찾을 수 없습니다."),
    SURPRISE_EVENT_NOT_OPEN(HttpStatus.BAD_REQUEST, "SURPRISE_EVENT_NOT_OPEN", "진행 중인 기습 이벤트가 아닙니다."),
    SURPRISE_EVENT_SUBMISSION_CLOSED(
            HttpStatus.BAD_REQUEST, "SURPRISE_EVENT_SUBMISSION_CLOSED", "제출 가능한 시간이 아닙니다."),
    SURPRISE_QUIZ_ALREADY_SUBMITTED(
            HttpStatus.BAD_REQUEST, "SURPRISE_QUIZ_ALREADY_SUBMITTED", "이미 기습 퀴즈를 제출했습니다."),
    SURPRISE_INVALID_CHOICE_NO(
            HttpStatus.BAD_REQUEST, "SURPRISE_INVALID_CHOICE_NO", "선지 번호는 1부터 4까지만 가능합니다."),
    SURPRISE_QUIZ_CONTENT_INVALID(
            HttpStatus.INTERNAL_SERVER_ERROR, "SURPRISE_QUIZ_CONTENT_INVALID", "기습 퀴즈 데이터가 올바르지 않습니다."),
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
