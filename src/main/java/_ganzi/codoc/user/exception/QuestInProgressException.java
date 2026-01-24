package _ganzi.codoc.user.exception;

import _ganzi.codoc.global.exception.BaseException;

public class QuestInProgressException extends BaseException {

    public QuestInProgressException() {
        super(QuestErrorCode.QUEST_IN_PROGRESS);
    }
}
