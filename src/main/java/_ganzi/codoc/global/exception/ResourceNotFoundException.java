package _ganzi.codoc.global.exception;

public class ResourceNotFoundException extends BaseException {

    public ResourceNotFoundException() {
        super(GlobalErrorCode.RESOURCE_NOT_FOUND);
    }
}
