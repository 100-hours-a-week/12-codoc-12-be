package _ganzi.codoc.global.exception;

public class ConflictException extends BaseException {
    public ConflictException() {
        super(GlobalErrorCode.CONFLICT);
    }
}
