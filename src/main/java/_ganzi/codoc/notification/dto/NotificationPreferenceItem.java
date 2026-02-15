package _ganzi.codoc.notification.dto;

import _ganzi.codoc.notification.enums.NotificationType;

public record NotificationPreferenceItem(NotificationType type, boolean enabled) {}
