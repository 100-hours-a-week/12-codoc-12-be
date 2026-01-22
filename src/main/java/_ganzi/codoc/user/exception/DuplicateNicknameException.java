package _ganzi.codoc.user.exception;

import _ganzi.codoc.global.exception.BaseException;

public class DuplicateNicknameException extends BaseException {

    public DuplicateNicknameException() {
        super(UserErrorCode.DUPLICATE_NICKNAME);
    }
}
