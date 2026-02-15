package _ganzi.codoc.notification.exception;

import _ganzi.codoc.global.exception.BaseException;

public class UserDeviceNotFoundException extends BaseException {

    public UserDeviceNotFoundException() {
        super(NotificationErrorCode.USER_DEVICE_NOT_FOUND);
    }
}
