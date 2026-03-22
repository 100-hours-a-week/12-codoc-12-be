package _ganzi.codoc.surprise.exception;

import _ganzi.codoc.global.exception.BaseException;

public class SurpriseEventNotFoundException extends BaseException {

    public SurpriseEventNotFoundException() {
        super(SurpriseEventErrorCode.SURPRISE_EVENT_NOT_FOUND);
    }
}
