package _ganzi.codoc.notification.service;

import _ganzi.codoc.notification.dto.NotificationMessageItem;
import _ganzi.codoc.notification.enums.PushNotificationSendResult;

public interface PushNotificationSender {

    PushNotificationSendResult send(NotificationMessageItem messageItem, String pushToken);
}
