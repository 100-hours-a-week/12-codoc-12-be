package _ganzi.codoc.user.exception;

import _ganzi.codoc.global.exception.BaseException;

public class QuestAlreadyClaimedException extends BaseException {

    public QuestAlreadyClaimedException() {
        super(QuestErrorCode.QUEST_ALREADY_CLAIMED);
    }
}
