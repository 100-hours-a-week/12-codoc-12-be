package _ganzi.codoc.notification.service;

import _ganzi.codoc.notification.dto.NotificationMessageItem;

public interface PushNotificationSender {

    void send(NotificationMessageItem messageItem, String pushToken);
}
