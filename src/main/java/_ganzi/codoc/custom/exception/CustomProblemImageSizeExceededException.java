package _ganzi.codoc.custom.exception;

import _ganzi.codoc.global.exception.BaseException;

public class CustomProblemImageSizeExceededException extends BaseException {

    public CustomProblemImageSizeExceededException() {
        super(CustomProblemErrorCode.CUSTOM_PROBLEM_IMAGE_SIZE_EXCEEDED);
    }
}
