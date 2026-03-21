package _ganzi.codoc.custom.exception;

import _ganzi.codoc.global.exception.BaseException;

public class CustomProblemNoPermissionException extends BaseException {

    public CustomProblemNoPermissionException() {
        super(CustomProblemErrorCode.CUSTOM_PROBLEM_NO_PERMISSION);
    }
}
