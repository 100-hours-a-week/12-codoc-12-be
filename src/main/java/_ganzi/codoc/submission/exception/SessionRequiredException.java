package _ganzi.codoc.submission.exception;

import _ganzi.codoc.global.exception.BaseException;

public class SessionRequiredException extends BaseException {

    public SessionRequiredException() {
        super(SubmissionErrorCode.SESSION_REQUIRED);
    }
}
