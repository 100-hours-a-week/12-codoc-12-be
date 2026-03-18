package _ganzi.codoc.custom.exception;

import _ganzi.codoc.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum CustomProblemErrorCode implements ErrorCode {
    // 400 Bad Request
    CUSTOM_PROBLEM_INVALID_FILE_KEY(
            HttpStatus.BAD_REQUEST, "CUSTOM_PROBLEM_INVALID_FILE_KEY", "잘못된 파일 키입니다."),
    CUSTOM_PROBLEM_INVALID_IMAGE_CONTENT_TYPE(
            HttpStatus.BAD_REQUEST, "CUSTOM_PROBLEM_INVALID_IMAGE_CONTENT_TYPE", "지원하지 않는 이미지 형식입니다."),
    CUSTOM_PROBLEM_IMAGE_COUNT_EXCEEDED(
            HttpStatus.BAD_REQUEST, "CUSTOM_PROBLEM_IMAGE_COUNT_EXCEEDED", "허용 가능한 업로드 이미지 수를 초과했습니다."),
    CUSTOM_PROBLEM_DUPLICATE_IMAGE_ORDER(
            HttpStatus.BAD_REQUEST, "CUSTOM_PROBLEM_DUPLICATE_IMAGE_ORDER", "이미지 순서는 중복될 수 없습니다."),

    // 403 Forbidden
    CUSTOM_PROBLEM_NO_PERMISSION(
            HttpStatus.FORBIDDEN, "CUSTOM_PROBLEM_NO_PERMISSION", "나만의 문제에 대한 권한이 없습니다."),

    // 404 Not Found
    CUSTOM_PROBLEM_NOT_FOUND(HttpStatus.NOT_FOUND, "CUSTOM_PROBLEM_NOT_FOUND", "나만의 문제를 찾을 수 없습니다."),

    // 409 Conflict
    CUSTOM_PROBLEM_NOT_COMPLETED(
            HttpStatus.CONFLICT, "CUSTOM_PROBLEM_NOT_COMPLETED", "생성이 완료된 나만의 문제만 조회할 수 있습니다."),

    // 429 Too Many Requests
    CUSTOM_PROBLEM_GENERATE_RATE_LIMIT_EXCEEDED(
            HttpStatus.TOO_MANY_REQUESTS,
            "CUSTOM_PROBLEM_GENERATE_RATE_LIMIT_EXCEEDED",
            "나만의 문제 생성 요청 횟수를 초과했습니다."),
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
