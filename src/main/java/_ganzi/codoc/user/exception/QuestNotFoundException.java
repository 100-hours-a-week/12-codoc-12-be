package _ganzi.codoc.user.exception;

import _ganzi.codoc.global.exception.BaseException;

public class QuestNotFoundException extends BaseException {

    public QuestNotFoundException() {
        super(QuestErrorCode.QUEST_NOT_FOUND);
    }
}
