package _ganzi.codoc.surprise.exception;

import _ganzi.codoc.global.exception.BaseException;

public class SurpriseEventNotOpenException extends BaseException {

    public SurpriseEventNotOpenException() {
        super(SurpriseEventErrorCode.SURPRISE_EVENT_NOT_OPEN);
    }
}
