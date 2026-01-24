package _ganzi.codoc.submission.exception;

import _ganzi.codoc.global.exception.BaseException;

public class QuizAlreadySubmittedException extends BaseException {

    public QuizAlreadySubmittedException() {
        super(SubmissionErrorCode.QUIZ_ALREADY_SUBMITTED);
    }
}
