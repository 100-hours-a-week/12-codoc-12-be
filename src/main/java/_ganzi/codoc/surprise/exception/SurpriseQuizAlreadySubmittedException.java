package _ganzi.codoc.surprise.exception;

import _ganzi.codoc.global.exception.BaseException;

public class SurpriseQuizAlreadySubmittedException extends BaseException {

    public SurpriseQuizAlreadySubmittedException() {
        super(SurpriseEventErrorCode.SURPRISE_QUIZ_ALREADY_SUBMITTED);
    }
}
