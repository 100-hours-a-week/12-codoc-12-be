package _ganzi.codoc.problem.exception;

import _ganzi.codoc.global.exception.BaseException;

public class RecommendNotAvailableException extends BaseException {

    public RecommendNotAvailableException() {
        super(ProblemErrorCode.RECOMMEND_NOT_AVAILABLE);
    }
}
