package _ganzi.codoc.surprise.exception;

import _ganzi.codoc.global.exception.BaseException;

public class SurpriseQuizContentInvalidException extends BaseException {

    public SurpriseQuizContentInvalidException() {
        super(SurpriseEventErrorCode.SURPRISE_QUIZ_CONTENT_INVALID);
    }
}
