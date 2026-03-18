package _ganzi.codoc.custom.exception;

import _ganzi.codoc.global.exception.BaseException;

public class CustomProblemImageCountExceededException extends BaseException {

    public CustomProblemImageCountExceededException() {
        super(CustomProblemErrorCode.CUSTOM_PROBLEM_IMAGE_COUNT_EXCEEDED);
    }
}
