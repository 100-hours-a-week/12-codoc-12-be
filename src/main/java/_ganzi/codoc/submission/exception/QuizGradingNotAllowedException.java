package _ganzi.codoc.submission.exception;

import _ganzi.codoc.global.exception.BaseException;

public class QuizGradingNotAllowedException extends BaseException {

    public QuizGradingNotAllowedException() {
        super(SubmissionErrorCode.QUIZ_GRADING_NOT_ALLOWED);
    }
}
