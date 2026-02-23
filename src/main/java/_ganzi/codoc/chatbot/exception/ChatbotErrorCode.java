package _ganzi.codoc.chatbot.exception;

import _ganzi.codoc.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum ChatbotErrorCode implements ErrorCode {
    CHATBOT_CONVERSATION_NOT_FOUND(
            HttpStatus.NOT_FOUND, "CHATBOT_CONVERSATION_NOT_FOUND", "대화 내역을 찾을 수 없습니다."),
    CHATBOT_CONVERSATION_NO_PERMISSION(
            HttpStatus.FORBIDDEN, "CHATBOT_CONVERSATION_NO_PERMISSION", "대화 내역에 대한 권한이 없습니다."),
    CHATBOT_STREAM_RATE_LIMIT_EXCEEDED(
            HttpStatus.TOO_MANY_REQUESTS, "CHATBOT_STREAM_RATE_LIMIT_EXCEEDED", "챗봇 스트림 요청 횟수를 초과했습니다."),
    CHATBOT_STREAM_EVENT_FAILED(
            HttpStatus.INTERNAL_SERVER_ERROR, "CHATBOT_STREAM_EVENT_FAILED", "챗봇 스트림 요청에 실패했습니다."),
    CHATBOT_STREAM_CANCEL_FAILED(
            HttpStatus.INTERNAL_SERVER_ERROR, "CHATBOT_STREAM_CANCEL_FAILED", "챗봇 스트림 중단 요청에 실패했습니다."),
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
