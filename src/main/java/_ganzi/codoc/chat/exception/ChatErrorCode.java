package _ganzi.codoc.chat.exception;

import _ganzi.codoc.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum ChatErrorCode implements ErrorCode {
    NO_CHAT_ROOM_PARTICIPANT(
            HttpStatus.FORBIDDEN, "NO_CHAT_ROOM_PARTICIPANT", "채팅방에 참여하지 않은 사용자입니다.");

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
