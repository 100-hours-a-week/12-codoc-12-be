package _ganzi.codoc.custom.exception;

import _ganzi.codoc.global.exception.BaseException;

public class CustomProblemDuplicateImageOrderException extends BaseException {

    public CustomProblemDuplicateImageOrderException() {
        super(CustomProblemErrorCode.CUSTOM_PROBLEM_DUPLICATE_IMAGE_ORDER);
    }
}
