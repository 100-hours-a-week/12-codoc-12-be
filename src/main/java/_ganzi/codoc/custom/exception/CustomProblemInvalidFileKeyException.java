package _ganzi.codoc.custom.exception;

import _ganzi.codoc.global.exception.BaseException;

public class CustomProblemInvalidFileKeyException extends BaseException {

    public CustomProblemInvalidFileKeyException() {
        super(CustomProblemErrorCode.CUSTOM_PROBLEM_INVALID_FILE_KEY);
    }
}
