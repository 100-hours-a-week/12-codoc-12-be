package _ganzi.codoc.custom.exception;

import _ganzi.codoc.global.exception.BaseException;

public class CustomProblemGenerateRateLimitExceededException extends BaseException {

    public CustomProblemGenerateRateLimitExceededException() {
        super(CustomProblemErrorCode.CUSTOM_PROBLEM_GENERATE_RATE_LIMIT_EXCEEDED);
    }
}
