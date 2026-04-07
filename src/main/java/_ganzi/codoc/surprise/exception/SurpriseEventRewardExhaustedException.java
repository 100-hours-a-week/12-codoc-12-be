package _ganzi.codoc.surprise.exception;

import _ganzi.codoc.global.exception.BaseException;

public class SurpriseEventRewardExhaustedException extends BaseException {

    public SurpriseEventRewardExhaustedException() {
        super(SurpriseEventErrorCode.SURPRISE_EVENT_REWARD_EXHAUSTED);
    }
}
