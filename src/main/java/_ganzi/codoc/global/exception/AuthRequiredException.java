package _ganzi.codoc.global.exception;

public class AuthRequiredException extends BaseException {

    public AuthRequiredException() {
        super(GlobalErrorCode.AUTH_REQUIRED);
    }
}
