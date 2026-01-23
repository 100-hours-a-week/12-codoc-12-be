package _ganzi.codoc.submission.exception;

import _ganzi.codoc.global.exception.BaseException;

public class InvalidAnswerFormatException extends BaseException {

    public InvalidAnswerFormatException() {
        super(SubmissionErrorCode.INVALID_ANSWER_FORMAT);
    }
}
