package _ganzi.codoc.notification.exception;

import _ganzi.codoc.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum NotificationErrorCode implements ErrorCode {
    USER_DEVICE_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_DEVICE_NOT_FOUND", "디바이스를 찾을 수 없습니다."),
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
