package _ganzi.codoc.user.exception;

import _ganzi.codoc.global.exception.BaseException;

public class AvatarNotFoundException extends BaseException {

    public AvatarNotFoundException() {
        super(UserErrorCode.AVATAR_NOT_FOUND);
    }
}
