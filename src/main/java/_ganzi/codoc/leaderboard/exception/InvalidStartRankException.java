package _ganzi.codoc.leaderboard.exception;

import _ganzi.codoc.global.exception.BaseException;

public class InvalidStartRankException extends BaseException {

    public InvalidStartRankException() {
        super(LeaderboardErrorCode.INVALID_START_RANK);
    }
}
