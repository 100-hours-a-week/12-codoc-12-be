package _ganzi.codoc.leaderboard.exception;

import _ganzi.codoc.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum LeaderboardErrorCode implements ErrorCode {
    NOT_LEADERBOARD_PARTICIPANT(
            HttpStatus.FORBIDDEN, "NOT_LEADERBOARD_PARTICIPANT", "리더보드에 참여 중이 아닙니다."),
    INVALID_START_RANK(HttpStatus.BAD_REQUEST, "INVALID_START_RANK", "유효하지 않은 시작 순위입니다.");

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
