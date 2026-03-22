package _ganzi.codoc.surprise.exception;

import _ganzi.codoc.global.exception.BaseException;

public class SurpriseInvalidChoiceNoException extends BaseException {

    public SurpriseInvalidChoiceNoException() {
        super(SurpriseEventErrorCode.SURPRISE_INVALID_CHOICE_NO);
    }
}
