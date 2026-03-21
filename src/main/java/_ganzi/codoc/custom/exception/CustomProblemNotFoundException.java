package _ganzi.codoc.custom.exception;

import _ganzi.codoc.global.exception.BaseException;

public class CustomProblemNotFoundException extends BaseException {

    public CustomProblemNotFoundException() {
        super(CustomProblemErrorCode.CUSTOM_PROBLEM_NOT_FOUND);
    }
}
