package _ganzi.codoc.submission.exception;

import _ganzi.codoc.global.exception.BaseException;

public class PrevQuizNotSubmittedException extends BaseException {

    public PrevQuizNotSubmittedException() {
        super(SubmissionErrorCode.PREV_QUIZ_NOT_SUBMITTED);
    }
}
