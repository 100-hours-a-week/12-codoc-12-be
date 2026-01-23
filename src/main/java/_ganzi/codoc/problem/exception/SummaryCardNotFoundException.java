package _ganzi.codoc.problem.exception;

import _ganzi.codoc.global.exception.BaseException;

public class SummaryCardNotFoundException extends BaseException {

    public SummaryCardNotFoundException() {
        super(ProblemErrorCode.SUMMARY_CARD_NOT_FOUND);
    }
}
