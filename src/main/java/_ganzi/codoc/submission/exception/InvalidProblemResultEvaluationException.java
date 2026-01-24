package _ganzi.codoc.submission.exception;

import _ganzi.codoc.global.exception.BaseException;

public class InvalidProblemResultEvaluationException extends BaseException {

    public InvalidProblemResultEvaluationException() {
        super(SubmissionErrorCode.INVALID_PROBLEM_RESULT_EVALUATION);
    }
}
