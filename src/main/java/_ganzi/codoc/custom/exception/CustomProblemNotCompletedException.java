package _ganzi.codoc.custom.exception;

import _ganzi.codoc.global.exception.BaseException;

public class CustomProblemNotCompletedException extends BaseException {

    public CustomProblemNotCompletedException() {
        super(CustomProblemErrorCode.CUSTOM_PROBLEM_NOT_COMPLETED);
    }
}
