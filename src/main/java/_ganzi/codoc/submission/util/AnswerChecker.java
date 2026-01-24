package _ganzi.codoc.submission.util;

public class AnswerChecker {

    public static boolean check(int answerId, int choiceId) {
        return choiceId == answerId;
    }
}
