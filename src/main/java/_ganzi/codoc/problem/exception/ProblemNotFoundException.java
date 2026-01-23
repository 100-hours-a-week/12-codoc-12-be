package _ganzi.codoc.problem.exception;

import _ganzi.codoc.global.exception.BaseException;

public class ProblemNotFoundException extends BaseException {

    public ProblemNotFoundException() {
        super(ProblemErrorCode.PROBLEM_NOT_FOUND);
    }
}
