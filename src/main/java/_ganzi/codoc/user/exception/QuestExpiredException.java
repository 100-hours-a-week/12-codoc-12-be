package _ganzi.codoc.user.exception;

import _ganzi.codoc.global.exception.BaseException;

public class QuestExpiredException extends BaseException {

    public QuestExpiredException() {
        super(QuestErrorCode.QUEST_EXPIRED);
    }
}
