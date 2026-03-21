package _ganzi.codoc.custom.exception;

import _ganzi.codoc.global.exception.BaseException;

public class CustomProblemInvalidImageContentTypeException extends BaseException {

    public CustomProblemInvalidImageContentTypeException() {
        super(CustomProblemErrorCode.CUSTOM_PROBLEM_INVALID_IMAGE_CONTENT_TYPE);
    }
}
