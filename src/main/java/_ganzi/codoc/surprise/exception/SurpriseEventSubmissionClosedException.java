package _ganzi.codoc.surprise.exception;

import _ganzi.codoc.global.exception.BaseException;

public class SurpriseEventSubmissionClosedException extends BaseException {

    public SurpriseEventSubmissionClosedException() {
        super(SurpriseEventErrorCode.SURPRISE_EVENT_SUBMISSION_CLOSED);
    }
}
