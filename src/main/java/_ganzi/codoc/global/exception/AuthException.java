package _ganzi.codoc.global.exception;

public class AuthException extends BaseException {

    public AuthException() {
        super(GlobalErrorCode.UNAUTHORIZED);
    }
}
