package _ganzi.codoc.global.exception;

public class AuthStateMismatchException extends BaseException {

    public AuthStateMismatchException() {
        super(GlobalErrorCode.AUTH_STATE_MISMATCH);
    }
}
