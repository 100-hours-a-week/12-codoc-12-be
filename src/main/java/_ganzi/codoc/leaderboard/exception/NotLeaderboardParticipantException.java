package _ganzi.codoc.leaderboard.exception;

import _ganzi.codoc.global.exception.BaseException;

public class NotLeaderboardParticipantException extends BaseException {

    public NotLeaderboardParticipantException() {
        super(LeaderboardErrorCode.NOT_LEADERBOARD_PARTICIPANT);
    }
}
