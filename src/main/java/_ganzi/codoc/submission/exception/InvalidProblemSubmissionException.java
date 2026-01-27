package _ganzi.codoc.submission.exception;

import _ganzi.codoc.global.exception.BaseException;

public class InvalidProblemSubmissionException extends BaseException {

    public InvalidProblemSubmissionException() {
        super(SubmissionErrorCode.INVALID_PROBLEM_SUBMISSION);
    }
}
