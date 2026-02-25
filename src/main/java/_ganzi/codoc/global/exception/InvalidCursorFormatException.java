package _ganzi.codoc.global.exception;

public class InvalidCursorFormatException extends BaseException {

    public InvalidCursorFormatException() {
        super(GlobalErrorCode.INVALID_CURSOR_FORMAT);
    }

    public InvalidCursorFormatException(Throwable cause) {
        super(GlobalErrorCode.INVALID_CURSOR_FORMAT);
        initCause(cause);
    }
}
