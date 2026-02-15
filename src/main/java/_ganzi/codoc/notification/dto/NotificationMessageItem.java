package _ganzi.codoc.notification.dto;

import _ganzi.codoc.notification.enums.NotificationType;
import java.util.Map;

public record NotificationMessageItem(
        NotificationType type, String title, String body, Map<String, String> linkParams) {}
