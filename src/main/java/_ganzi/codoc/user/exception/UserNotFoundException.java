package _ganzi.codoc.user.exception;

import _ganzi.codoc.global.exception.BaseException;

public class UserNotFoundException extends BaseException {

    public UserNotFoundException() {
        super(UserErrorCode.USER_NOT_FOUND);
    }
}
