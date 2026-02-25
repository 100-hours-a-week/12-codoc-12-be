package _ganzi.codoc.global.exception;

public class PageLimitOutOfRangeException extends BaseException {

    public PageLimitOutOfRangeException() {
        super(GlobalErrorCode.LIMIT_OUT_OF_RANGE);
    }
}
