package _ganzi.codoc.chat.exception;

import _ganzi.codoc.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum ChatErrorCode implements ErrorCode {
    NO_CHAT_ROOM_PARTICIPANT(
            HttpStatus.FORBIDDEN, "NO_CHAT_ROOM_PARTICIPANT", "채팅방에 참여하지 않은 사용자입니다."),
    CHAT_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAT_ROOM_NOT_FOUND", "채팅방을 찾을 수 없습니다."),
    CHAT_ROOM_FULL(HttpStatus.CONFLICT, "CHAT_ROOM_FULL", "채팅방 정원이 초과되었습니다."),
    CHAT_ROOM_INVALID_PASSWORD(
            HttpStatus.FORBIDDEN, "CHAT_ROOM_INVALID_PASSWORD", "채팅방 비밀번호가 일치하지 않습니다."),
    CHAT_ROOM_ALREADY_JOINED(HttpStatus.CONFLICT, "CHAT_ROOM_ALREADY_JOINED", "이미 참여 중인 채팅방입니다.");

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
