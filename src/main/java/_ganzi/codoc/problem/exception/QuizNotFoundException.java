package _ganzi.codoc.problem.exception;

import _ganzi.codoc.global.exception.BaseException;

public class QuizNotFoundException extends BaseException {

    public QuizNotFoundException() {
        super(ProblemErrorCode.QUIZ_NOT_FOUND);
    }
}
