package _ganzi.codoc.user.exception;

import _ganzi.codoc.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum QuestErrorCode implements ErrorCode {
    QUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "QUEST_NOT_FOUND", "퀘스트가 존재하지 않습니다."),
    QUEST_IN_PROGRESS(HttpStatus.CONFLICT, "QUEST_IN_PROGRESS", "퀘스트가 아직 진행 중입니다."),
    QUEST_ALREADY_CLAIMED(HttpStatus.CONFLICT, "QUEST_ALREADY_CLAIMED", "이미 보상을 받은 퀘스트입니다.");

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
