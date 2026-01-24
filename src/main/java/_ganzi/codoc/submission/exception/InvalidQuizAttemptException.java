package _ganzi.codoc.submission.exception;

import _ganzi.codoc.global.exception.BaseException;

public class InvalidQuizAttemptException extends BaseException {

    public InvalidQuizAttemptException() {
        super(SubmissionErrorCode.INVALID_QUIZ_ATTEMPT);
    }
}
